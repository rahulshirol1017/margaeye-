package com.mplads.geotrack.data.repository

import android.util.Log
import com.mplads.geotrack.data.local.GeoPhotoDao

import com.mplads.geotrack.data.model.GeoPhoto
import com.mplads.geotrack.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URI

class PhotoRepository(
    private val photoDao: GeoPhotoDao
) {
    val allPhotos: Flow<List<GeoPhoto>> = photoDao.getAllPhotos()
    val savedPhotosCount: Flow<Int> = photoDao.getPhotoCount()

    suspend fun savePhoto(photo: GeoPhoto) {
        // Save locally to Room database first (offline capability)
        photoDao.insertPhoto(photo)

        // Attempt background upload to MongoDB server
        withContext(Dispatchers.IO) {
            try {
                uploadPhotoToServer(photo)
            } catch (e: Exception) {
                Log.w("PhotoRepository", "Could not sync photo to MongoDB backend: ${e.message}")
            }
        }
    }

    suspend fun deletePhoto(id: String) {
        photoDao.deletePhotoById(id)
    }

    suspend fun uploadPhotoToServer(photo: GeoPhoto): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val targetPath = photo.watermarkedImageUrl ?: photo.imageUrl
                val file = if (targetPath.startsWith("file://")) {
                    File(URI.create(targetPath))
                } else {
                    File(targetPath)
                }

                if (!file.exists()) {
                    Log.e("PhotoRepository", "File does not exist for upload: ${file.absolutePath}")
                    return@withContext false
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                fun createPart(value: String?): okhttp3.RequestBody {
                    return (value ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                }

                val fields = mapOf(
                    "photoId" to createPart(photo.id),
                    "latitude" to createPart(photo.latitude.toString()),
                    "longitude" to createPart(photo.longitude.toString()),
                    "accuracy" to createPart(photo.accuracy.toString()),
                    "altitude" to createPart(photo.altitude?.toString() ?: "0.0"),
                    "placeName" to createPart(photo.placeName),
                    "workerName" to createPart(photo.workerName),
                    "workId" to createPart(photo.workId),
                    "description" to createPart(photo.description),
                    "dateFormatted" to createPart(photo.dateFormatted),
                    "timeFormatted" to createPart(photo.timeFormatted),
                    "capturedAt" to createPart(photo.capturedAt),
                    "hasVisibleOverlay" to createPart(photo.hasVisibleOverlay.toString())
                )

                val response = NetworkClient.apiService.uploadPhoto(body, fields)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.i("PhotoRepository", "Successfully uploaded photo ${photo.id} to MongoDB")
                    true
                } else {
                    Log.e("PhotoRepository", "Failed to upload photo: ${response.code()} ${response.message()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("PhotoRepository", "Error uploading photo to MongoDB server: ${e.message}", e)
                false
            }
        }
    }
}

