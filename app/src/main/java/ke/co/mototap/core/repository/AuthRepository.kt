package ke.co.mototap.core.repository

import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.model.Review
import kotlinx.coroutines.flow.Flow

sealed class PhoneSignInStart {
    data class CodeSent(val verificationId: String) : PhoneSignInStart()
    data object AutoVerified : PhoneSignInStart()
}

interface AuthRepository {
    val currentUserId: Flow<String?>

    suspend fun ensureSignedIn()
    
    suspend fun signIn(email: String, password: String): Result<Unit>
    
    suspend fun sendPasswordReset(email: String): Result<Unit>
    
    suspend fun signUp(email: String, password: String, name: String, role: String, phoneNumber: String? = null): Result<Unit>

    suspend fun signInWithGoogleIdToken(
        idToken: String,
        roleIfNew: String,
        phoneIfNew: String? = null,
    ): Result<Unit>

    suspend fun startPhoneSignIn(
        activity: android.app.Activity,
        phoneE164: String,
        roleIfNew: String,
    ): Result<PhoneSignInStart>

    suspend fun confirmPhoneSignIn(
        verificationId: String,
        smsCode: String,
        roleIfNew: String,
        phoneE164: String,
    ): Result<Unit>

    suspend fun signOut()
    
    suspend fun getUserRole(userId: String): String?

    suspend fun getUserProfile(userId: String): UserProfile?

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    suspend fun updateGarageProfilePhotos(userId: String, photos: List<String>): Result<List<String>>

    suspend fun updateGarageLocationName(userId: String, locationName: String): Result<String>

    suspend fun uploadSignupImage(
        userId: String,
        folder: String,
        uri: android.net.Uri,
        context: android.content.Context,
    ): Result<String>

    suspend fun completeSignupStep2(
        userId: String,
        profilePhotoUrl: String,
        idPhotoUrl: String,
        idNumber: String,
        role: String,
    ): Result<Unit>

    suspend fun completeSignupStep3Driver(
        userId: String,
        vehicleMake: String,
        vehicleModel: String,
        numberPlate: String,
        vehiclePhotoUrl: String,
    ): Result<Unit>

    suspend fun completeSignupStep3Mechanic(
        userId: String,
        institutionName: String,
        experienceYears: String,
        certificatePhotoUrl: String,
        garagePhotos: List<String>,
        latitude: Double,
        longitude: Double,
        address: String,
        locationName: String = "",
        garageMode: String = "own",
        inviteCode: String = "",
        workingHours: ke.co.mototap.core.util.WorkingHours? = null,
    ): Result<Unit>

    suspend fun completeSignupStep3PartsDealer(
        userId: String,
        institutionName: String,
        experienceYears: String,
        certificatePhotoUrl: String,
        garagePhotos: List<String>,
        latitude: Double,
        longitude: Double,
        address: String,
        locationName: String = "",
        workingHours: ke.co.mototap.core.util.WorkingHours? = null,
    ): Result<Unit>

    suspend fun saveWorkingHours(
        userId: String,
        workingHours: ke.co.mototap.core.util.WorkingHours,
    ): Result<Unit>

    suspend fun getAllMechanics(): List<UserProfile>
    fun observeAllMechanics(): Flow<List<UserProfile>>

    suspend fun getAllPartsDealers(): List<UserProfile>
    fun observeAllPartsDealers(): Flow<List<UserProfile>>

    suspend fun deleteAccount(currentPassword: String): Result<Unit>

    suspend fun updateMechanicSkills(
        userId: String,
        skills: List<String>,
        servicePrices: Map<String, Map<String, Long>> = emptyMap(),
        replacePrices: Boolean = false,
    ): Result<Unit>

    suspend fun awardLoyaltyPoints(userId: String, points: Int): Result<Unit>

    suspend fun addReview(review: Review): Result<Unit>
    fun observeMechanicReviews(mechanicId: String): Flow<List<Review>>
}
