package com.example.safehome.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.safehome.data.local.AppDatabase
import com.example.safehome.data.local.dao.HomeDao
import com.example.safehome.data.network.NetworkHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HintModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi {
        return retrofit.create(HomeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSensorApi(retrofit: Retrofit): SensorApi {
        return retrofit.create(SensorApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeDao(appDatabase: AppDatabase): HomeDao {
        return appDatabase.homeDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "safehome_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBiometricSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            BIOMETRIC_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    private const val BIOMETRIC_PREFS_NAME = "biometric_secure_prefs"

    @Provides
    @Singleton
    fun provideNetworkHandler(@ApplicationContext context: Context): NetworkHandler {
        return NetworkHandler(context)
    }

    @Provides
    @Singleton
    fun provideFaceDetector(): FaceDetector {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        return FaceDetection.getClient(options)
    }

    @Provides
    @Singleton
    fun provideCameraExecutor(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }
}