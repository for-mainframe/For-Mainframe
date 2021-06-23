package eu.ibagroup.formainframe.api

import com.intellij.openapi.application.ApplicationManager
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ZoweApi {

  companion object {

    val unsafeOkHttpClient by lazy { buildUnsafeClient() }

    val safeOkHttpClient: OkHttpClient //by lazy  {
      get() = OkHttpClient.Builder()
        .addThreadPool()
        .build()

    @JvmStatic
    val instance: ZoweApi
      get() = ApplicationManager.getApplication().getService(ZoweApi::class.java)

    fun OkHttpClient.Builder.addThreadPool(): OkHttpClient.Builder {
      readTimeout(5, TimeUnit.MINUTES)
      connectTimeout(5, TimeUnit.MINUTES)
      connectionPool(ConnectionPool(100, 5, TimeUnit.MINUTES))
      dispatcher(Dispatcher().apply {
        maxRequests = 100
        maxRequestsPerHost = 100
      })
      return this
    }

    fun getOkHttpClient(isAllowSelfSigned: Boolean): OkHttpClient {
      return if (isAllowSelfSigned) {
        ZoweApi.unsafeOkHttpClient
      } else {
        ZoweApi.safeOkHttpClient
      }
    }

    fun buildUnsafeClient(): OkHttpClient {
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
  }
  fun <Api : Any> getApi(apiClass: Class<out Api>, connectionConfig: ConnectionConfig): Api

  fun <Api : Any> getApi(apiClass: Class<out Api>, url: String, isAllowSelfSigned: Boolean): Api
}


inline fun <reified Api : Any> api(connectionConfig: ConnectionConfig): Api {
  return ZoweApi.instance.getApi(Api::class.java, connectionConfig)
}

inline fun <reified Api : Any> api(url: String, isAllowSelfSigned: Boolean): Api {
  return ZoweApi.instance.getApi(Api::class.java, url, isAllowSelfSigned)
}