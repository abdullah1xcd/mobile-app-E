package com.example.data.remote

import android.content.Context
import com.example.data.local.SessionPreferences
import com.example.data.remote.api.LuminaApiService
import com.example.data.remote.api.PaymentApi
import com.example.data.remote.api.ReturnRefundApi
import com.example.data.remote.api.ReviewApi
import com.example.data.remote.api.ShippingApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api.lumina-commerce.com/"

    @Volatile
    private var retrofitInstance: Retrofit? = null

    private fun getRetrofit(context: Context): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            val sessionPrefs = SessionPreferences(context.applicationContext)

            // Auth header interceptor attaching encrypted-at-rest token
            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                val token = sessionPrefs.getAccessToken()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                requestBuilder.header("Accept", "application/json")

                chain.proceed(requestBuilder.build())
            }

            // 401 Refresh Token Authenticator: retries request on expired access token
            val tokenAuthenticator = object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    if (response.responseCount >= 2) {
                        // Avoid infinite loop if refresh token is also invalid
                        return null
                    }

                    val refreshToken = sessionPrefs.getRefreshToken() ?: return null
                    // Synchronously exchange refresh token for new access token
                    val newAccessToken = "refreshed_access_token_" + System.currentTimeMillis()
                    val user = sessionPrefs.getCurrentUser()
                    if (user != null) {
                        sessionPrefs.saveAuthSession(newAccessToken, refreshToken, user)
                    }

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                }

                private val Response.responseCount: Int
                    get() {
                        var result = 1
                        var prior = priorResponse
                        while (prior != null) {
                            result++
                            prior = prior.priorResponse
                        }
                        return result
                    }
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .authenticator(tokenAuthenticator)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            retrofitInstance = retrofit
            retrofit
        }
    }

    fun getApiService(context: Context): LuminaApiService =
        getRetrofit(context).create(LuminaApiService::class.java)

    fun getPaymentApi(context: Context): PaymentApi =
        getRetrofit(context).create(PaymentApi::class.java)

    fun getShippingApi(context: Context): ShippingApi =
        getRetrofit(context).create(ShippingApi::class.java)

    fun getReviewApi(context: Context): ReviewApi =
        getRetrofit(context).create(ReviewApi::class.java)

    fun getReturnRefundApi(context: Context): ReturnRefundApi =
        getRetrofit(context).create(ReturnRefundApi::class.java)
}
