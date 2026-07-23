package com.example.mototap.core.util

object SignupValidation {
    fun isNameValid(name: String): Boolean =
        name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size >= 2

    fun validateDriverStep3(
        vehicleMake: String,
        vehicleModel: String,
        numberPlate: String,
        vehiclePhotoUrl: String,
    ): String? {
        if (vehicleMake.isBlank()) return "Please select your vehicle make."
        if (vehicleModel.isBlank()) return "Please select your vehicle model."
        if (numberPlate.isBlank()) return "Please enter your number plate."
        if (vehiclePhotoUrl.isBlank()) return "Please upload a vehicle photo."
        return null
    }

    /**
     * Mechanic step 3. When joining a garage, only experience, certificate and a
     * valid invite code are required (institution/photo/map come from the garage).
     */
    fun validateMechanicStep3(
        garageMode: String,
        inviteCode: String,
        institutionName: String,
        experienceYears: String,
        certificatePhotoUrl: String,
        garagePhotos: List<String>,
        latitude: Double?,
        longitude: Double?,
        address: String,
    ): String? {
        if (certificatePhotoUrl.isBlank()) return "Please upload your certification photo."
        if (experienceYears.isBlank()) return "Please select your experience."

        val joinMode = garageMode.trim() == "join"
        if (joinMode) {
            val code = inviteCode.trim().uppercase().filter { it.isLetterOrDigit() }
            if (code.length < 4) return "Enter a valid garage invite code."
            return null
        }

        return validateProviderStep3(
            institutionName = institutionName,
            experienceYears = experienceYears,
            certificatePhotoUrl = certificatePhotoUrl,
            garagePhotos = garagePhotos,
            latitude = latitude,
            longitude = longitude,
            address = address,
            locationLabel = "garage",
        )
    }

    fun validateProviderStep3(
        institutionName: String,
        experienceYears: String,
        certificatePhotoUrl: String,
        garagePhotos: List<String>,
        latitude: Double?,
        longitude: Double?,
        address: String,
        locationLabel: String,
    ): String? {
        if (institutionName.isBlank()) return "Please enter your $locationLabel name."
        if (certificatePhotoUrl.isBlank()) return "Please upload your certification or license photo."
        if (experienceYears.isBlank()) return "Please select your experience."
        if (address.isBlank()) return "Please enter your $locationLabel address."
        if (garagePhotos.isEmpty()) return "Please upload a front photo of your $locationLabel."
        if (latitude == null || longitude == null) {
            return "Please pin your $locationLabel location on the map."
        }
        return null
    }

    fun isProviderRole(role: String?): Boolean =
        role?.lowercase()?.trim() in setOf("mechanic", "parts_dealer", "parts dealer")

    fun isPartsDealerRole(role: String?): Boolean =
        role?.lowercase()?.trim() in setOf("parts_dealer", "parts dealer")
}
