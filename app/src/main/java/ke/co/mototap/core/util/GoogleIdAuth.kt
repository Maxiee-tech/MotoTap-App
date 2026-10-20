package ke.co.mototap.core.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import ke.co.mototap.R

object GoogleIdAuth {
    suspend fun requestIdToken(context: Context): Result<String> {
        return try {
            val serverClientId = context.getString(R.string.google_web_client_id)
            val googleOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()
            val result = CredentialManager.create(context).getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdToken.idToken
                if (idToken.isBlank()) {
                    Result.failure(Exception("Google did not return a sign-in token."))
                } else {
                    Result.success(idToken)
                }
            } else {
                Result.failure(Exception("Google sign-in is not available on this device."))
            }
        } catch (_: GetCredentialCancellationException) {
            Result.failure(GoogleAuthCancelled())
        } catch (_: NoCredentialException) {
            Result.failure(Exception("No Google account found on this device."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GoogleAuthCancelled : Exception("cancelled")
