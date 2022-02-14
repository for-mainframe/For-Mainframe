package eu.ibagroup.formainframe.dataops

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ComponentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vfs.VirtualFile
import eu.ibagroup.formainframe.dataops.attributes.AttributesService
import eu.ibagroup.formainframe.dataops.attributes.FileAttributes
import eu.ibagroup.formainframe.dataops.fetch.FileFetchProvider
import eu.ibagroup.formainframe.dataops.operations.OperationRunner
import eu.ibagroup.formainframe.dataops.synchronizer.ContentSynchronizer
import eu.ibagroup.formainframe.utils.associateListedBy
import eu.ibagroup.formainframe.utils.findAnyNullable
import com.intellij.openapi.util.Disposer
import eu.ibagroup.formainframe.dataops.log.AbstractMFLoggerBase
import eu.ibagroup.formainframe.dataops.log.LogInfo
import eu.ibagroup.formainframe.dataops.log.LogFetcher
import eu.ibagroup.formainframe.dataops.log.MFLogger

class DataOpsManagerImpl : DataOpsManager {

  private fun <Component> List<DataOpsComponentFactory<Component>>.buildComponents(): MutableList<Component> {
    return buildComponents(this@DataOpsManagerImpl)
  }

  override val componentManager: ComponentManager
    get() = ApplicationManager.getApplication()

  private val attributesServiceDelegate = lazy {
    @Suppress("UNCHECKED_CAST")
    AttributesService.EP.extensionList.buildComponents() as MutableList<AttributesService<FileAttributes, VirtualFile>>
  }
  private val attributesServices by attributesServiceDelegate

  override fun <A : FileAttributes, F : VirtualFile> getAttributesService(
    attributesClass: Class<out A>,
    vFileClass: Class<out F>
  ): AttributesService<A, F> {
    @Suppress("UNCHECKED_CAST")
    return attributesServices.find {
      it.attributesClass.isAssignableFrom(attributesClass) && it.vFileClass.isAssignableFrom(vFileClass)
    } as AttributesService<A, F>? ?: throw IllegalArgumentException(
      "Cannot find AttributesService for attributeClass=${attributesClass.name} and vFileClass=${vFileClass.name}"
    )
  }

  override fun tryToGetAttributes(file: VirtualFile): FileAttributes? {
    return attributesServices.stream()
      .filter { it.vFileClass.isAssignableFrom(file::class.java) }
      .map { it.getAttributes(file) }
      .filter { it != null }
      .findAnyNullable()
  }

  override fun tryToGetFile(attributes: FileAttributes): VirtualFile? {
    return attributesServices.stream()
      .map { it.getVirtualFile(attributes) }
      .filter { it != null }
      .findAnyNullable()
  }

  private val fileFetchProvidersDelegate = lazy {
    FileFetchProvider.EP.extensionList.buildComponents()
  }
  private val fileFetchProviders by fileFetchProvidersDelegate

  override fun <R : Any, Q : Query<R, Unit>, File : VirtualFile> getFileFetchProvider(
    requestClass: Class<out R>,
    queryClass: Class<out Query<*, *>>,
    vFileClass: Class<out File>
  ): FileFetchProvider<R, Q, File> {
    @Suppress("UNCHECKED_CAST")
    return fileFetchProviders.find {
      it.requestClass.isAssignableFrom(requestClass)
        && it.queryClass.isAssignableFrom(queryClass)
        && it.vFileClass.isAssignableFrom(vFileClass)
    } as FileFetchProvider<R, Q, File>? ?: throw IllegalArgumentException(
      "Cannot find FileFetchProvider for " +
              "requestClass=${requestClass.name}; queryClass=${queryClass.name}; vFileClass=${vFileClass.name}"
    )
  }

  private val contentSynchronizersDelegate = lazy {
    ContentSynchronizer.EP.extensionList.buildComponents()
  }
  private val contentSynchronizers by contentSynchronizersDelegate

  override fun isSyncSupported(file: VirtualFile): Boolean {
    return contentSynchronizers.stream()
      .filter { it.accepts(file) }
      .findAnyNullable() != null
  }

  override fun getContentSynchronizer(file: VirtualFile): ContentSynchronizer? {
    return contentSynchronizers.stream()
      .filter { it.accepts(file) }
      .findAnyNullable()
  }

  private val operationRunners by lazy {
    @Suppress("UNCHECKED_CAST")
    val operationRunnersList = OperationRunner.EP.extensionList.buildComponents() as MutableList<OperationRunner<Operation<*>, *>>
    operationRunnersList.associateListedBy { it.operationClass }
  }

  private val logFetchers by lazy {
    val logFetcherList = LogFetcher.EP.extensionList.buildComponents()
    logFetcherList.associateBy { it.logInfoClass }
  }

  override fun isOperationSupported(operation: Operation<*>): Boolean {
    return operationRunners[operation::class.java]?.any { it.canRun(operation) } == true
  }

  override fun <R : Any> performOperation(
    operation: Operation<R>,
    progressIndicator: ProgressIndicator
  ): R {
    val result = operationRunners[operation::class.java]
      ?.find { it.canRun(operation) }
      ?.run(operation, progressIndicator)
      ?: throw IllegalArgumentException("Unsupported Operation $operation")
    @Suppress("UNCHECKED_CAST")
    return result as R
  }

  override fun <LInfo : LogInfo, LFetcher : LogFetcher<LInfo>> createMFLogger(
    logInfo: LInfo,
    consoleView: ConsoleView
  ): MFLogger<LFetcher> {
    val logFetcher = logFetchers[logInfo::class.java]
      ?: throw IllegalArgumentException("Unsupported Log Information $logInfo")
    @Suppress("UNCHECKED_CAST")
    val resultFetcher: LFetcher = logFetcher as LFetcher
    return object: AbstractMFLoggerBase<LInfo, LFetcher>(logInfo, consoleView) {
      override val logFetcher: LFetcher = resultFetcher
    }.also {
      Disposer.register(this, it)
    }
  }

  override fun dispose() {
    if (attributesServiceDelegate.isInitialized()) attributesServices.clear()
    if (fileFetchProvidersDelegate.isInitialized()) fileFetchProviders.clear()
    if (contentSynchronizersDelegate.isInitialized()) contentSynchronizers.clear()
    Disposer.dispose(this)
  }
}
