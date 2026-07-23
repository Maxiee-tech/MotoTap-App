package com.example.mototap.core.data.cloudinary

import com.example.mototap.BuildConfig

object CloudinaryConfig {
    val cloudName: String = BuildConfig.CLOUDINARY_CLOUD_NAME

    fun presetForCategory(category: String): String = when (category) {
        "profile_photos" -> BuildConfig.CLOUDINARY_PRESET_PROFILE_PHOTOS
        "signup_documents" -> BuildConfig.CLOUDINARY_PRESET_SIGNUP_DOCUMENTS
        "vehicles" -> BuildConfig.CLOUDINARY_PRESET_VEHICLES
        "user_uploads" -> BuildConfig.CLOUDINARY_PRESET_USER_UPLOADS
        else -> BuildConfig.CLOUDINARY_PRESET_PROFILE_PHOTOS
    }

    /** App folder key → validation category (matches web uploadValidation.js). */
    fun categoryForFolder(folder: String): String = when (folder) {
        "profile" -> "profile_photos"
        "id_front", "certificate", "garage" -> "signup_documents"
        "vehicle" -> "vehicles"
        else -> "user_uploads"
    }

    fun storagePath(userId: String, folder: String): String = when (folder) {
        "profile" -> "firebase-backend/profile_photos/$userId"
        "id_front" -> "firebase-backend/signup_documents/$userId/id_front"
        "vehicle" -> "firebase-backend/vehicles/$userId"
        "certificate" -> "firebase-backend/signup_documents/$userId/certificate"
        "garage" -> "firebase-backend/signup_documents/$userId/garage"
        else -> "firebase-backend/user_uploads/$userId"
    }

    fun resourceType(category: String): String =
        if (category == "signup_documents" || category == "user_uploads") "auto" else "image"
}
