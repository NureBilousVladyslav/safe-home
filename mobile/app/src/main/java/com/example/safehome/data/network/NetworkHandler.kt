package com.example.safehome.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class NetworkHandler(private val context: Context) {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            if (!isNetworkAvailable()) {
                Timber.Forest.e("No network connection")
                return Result.Error(ErrorType.NetworkError("No internet connection"))
            }
            val response = apiCall()
            Timber.Forest.d("API call successful: $response")
            Result.Success(response)
        } catch (e: HttpException) {
            val code = e.code()
            val message = when (code) {
                400 -> "Bad Request"
                401 -> "Unauthorized - Invalid token"
                403 -> "Forbidden - Access denied"
                404 -> "Not Found"
                500 -> "Server Error"
                else -> "Unknown Error ${e.code()}: ${e.message}"
            }
            Timber.Forest.e(e, "Server error: $message, code: $code")
            Result.Error(ErrorType.ServerError(message, code))
        } catch (e: IOException) {
            Timber.Forest.e(e, "Network error: ${e.message}")
            Result.Error(ErrorType.NetworkError("Failed to connect to server"))
        } catch (e: Exception) {
            Timber.Forest.e(e, "Internal error: ${e.message}")
            Result.Error(ErrorType.InternalError("Unexpected error: ${e.message}"))
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}