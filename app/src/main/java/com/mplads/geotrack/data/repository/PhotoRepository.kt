package com.mplads.geotrack.data.repository

import com.mplads.geotrack.data.local.GeoPhotoDao
import com.mplads.geotrack.data.model.GeoPhoto
import kotlinx.coroutines.flow.Flow

class PhotoRepository(
    private val photoDao: GeoPhotoDao
) {
    val allPhotos: Flow<List<GeoPhoto>> = photoDao.getAllPhotos()
    val savedPhotosCount: Flow<Int> = photoDao.getPhotoCount()

    suspend fun savePhoto(photo: GeoPhoto) {
        photoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(id: String) {
        photoDao.deletePhotoById(id)
    }
}
