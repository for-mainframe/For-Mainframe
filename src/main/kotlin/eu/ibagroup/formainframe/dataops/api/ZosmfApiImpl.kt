package eu.ibagroup.formainframe.dataops.api

import com.google.gson.GsonBuilder
import eu.ibagroup.formainframe.config.configCrudable
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.connect.UrlConnection
import eu.ibagroup.formainframe.utils.crudable.getByForeignKey
import eu.ibagroup.r2z.buildApi
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ZosmfApiImpl : ZosmfApi {

  private val apis = hashMapOf<Class<out Any>, MutableMap<ConnectionConfig, Any>>()

  @Suppress("UNCHECKED_CAST")
  override fun <Api : Any> getApi(apiClass: Class<out Api>, connectionConfig: ConnectionConfig): Api {
    if (!apis.containsKey(apiClass)) {
      synchronized(apis) {
        if (!apis.containsKey(apiClass)) {
          apis[apiClass] = hashMapOf()
        }
      }
    }
    val apiClassMap = apis[apiClass]!!
    if (!apiClassMap.containsKey(connectionConfig)) {
      synchronized(apiClassMap) {
        if (!apiClassMap.containsKey(connectionConfig)) {
          val zosmfUrlConnection = configCrudable
            .getByForeignKey<ConnectionConfig, UrlConnection>(connectionConfig)
            ?: throw RuntimeException("Cannot find url for connection ${connectionConfig.name}")
          val baseUrl = zosmfUrlConnection.url
          val client = getOkHttpClient(zosmfUrlConnection)
          apiClassMap[connectionConfig] = buildApi(baseUrl, client, apiClass)
        }
      }
    }
    return apiClassMap[connectionConfig] as Api
  }

}

private val gsonFactory = GsonConverterFactory.create(GsonBuilder().create())
private val scalarsConverterFactory = ScalarsConverterFactory.create()

private fun OkHttpClient.Builder.addThreadPool(): OkHttpClient.Builder {
  readTimeout(5, TimeUnit.MINUTES)
  connectTimeout(5, TimeUnit.MINUTES)
  connectionPool(ConnectionPool(100, 5, TimeUnit.MINUTES))
  dispatcher(Dispatcher().apply {
    maxRequests = 100
    maxRequestsPerHost = 100
  })
  return this
}

private val unsafeOkHttpClient by lazy { buildUnsafeClient() }
private val safeOkHttpClient by lazy {
  OkHttpClient.Builder()
    .addThreadPool()
    .build()
}

private fun getOkHttpClient(urlConnection: UrlConnection): OkHttpClient {
  return if (urlConnection.isAllowSelfSigned) {
    unsafeOkHttpClient
  } else {
    safeOkHttpClient
  }
}

private fun buildUnsafeClient(): OkHttpClient {
  return try {
    val trustAllCerts: Array<TrustManager> = arrayOf(
      object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(
          chain: Array<X509Certificate?>?,
          authType: String?
        ) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(
          chain: Array<X509Certificate?>?,
          authType: String?
        ) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
          return arrayOf()
        }
      }
    )
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    val sslSocketFactory = sslContext.socketFactory
    val builder = OkHttpClient.Builder()
    builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
    builder.hostnameVerifier { _, _ -> true }
    builder.addThreadPool()
    builder.build()
  } catch (e: Exception) {
    throw RuntimeException(e)
  }
}