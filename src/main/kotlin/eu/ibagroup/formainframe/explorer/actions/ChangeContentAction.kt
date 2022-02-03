package eu.ibagroup.formainframe.explorer.actions

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.utils.`is`
import eu.ibagroup.formainframe.utils.debounce
import eu.ibagroup.formainframe.utils.runWriteActionInEdt
import eu.ibagroup.formainframe.vfs.MFVirtualFile

class ChangeContentAction: TypedHandlerDelegate() {
  private val dataOpsManager = service<DataOpsManager>()
  private var adaptContentFunc: (() -> Unit)? = null

  override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {

    val vFile = file.virtualFile
    if (vFile.`is`<MFVirtualFile>()) {
      if (adaptContentFunc == null) {
        adaptContentFunc = debounce(500) {
          adaptContentFunc = null
          val contentAdapter = dataOpsManager.getMFContentAdapter(file.virtualFile)
          val currentContent = editor.document.text.toByteArray(vFile.charset)
          val adaptedContent = contentAdapter.performAdaptingToMainframe(currentContent, vFile)
          runWriteActionInEdt { editor.document.setText(adaptedContent.toString(vFile.charset)) }
        }
      }
      adaptContentFunc?.let { it() }
    }
    return super.charTyped(c, project, editor, file)
  }
}
