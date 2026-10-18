package com.news4live.app.data.api

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.news4live.app.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.ConnectionPool

class NetworkHeaderInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val isConnected = isNetworkAvailable(context)
        
        val builder = originalRequest.newBuilder()
            .header("User-Agent", "News4LiveApp/1.0 (Android; Mobile; Hindi News)")
            .header("Accept", "application/json, text/plain, */*")
            
        if (!isConnected && originalRequest.method == "GET") {
            builder.header("Cache-Control", "public, only-if-cached, max-stale=86400")
        }
        
        return chain.proceed(builder.build())
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(network) ?: return false
            actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }
}

class CacheControlResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        if (chain.request().method == "GET" && originalResponse.isSuccessful) {
            return originalResponse.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", "public, max-age=180") // Cache HTTP response on disk for 3 mins
                .build()
        }
        return originalResponse
    }
}

object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun createOkHttpClient(context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cacheSize = 100L * 1024L * 1024L // 100 MB disk cache

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .cache(Cache(cacheDir, cacheSize))
            .addInterceptor(NetworkHeaderInterceptor(context))
            .addNetworkInterceptor(CacheControlResponseInterceptor())
            .addInterceptor(logging)
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun createWordPressService(context: Context): WordPressApiService {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(createOkHttpClient(context))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(WordPressApiService::class.java)
    }

    fun createPluginService(context: Context): NewsAppPluginApiService {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(createOkHttpClient(context))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(NewsAppPluginApiService::class.java)
    }
}
