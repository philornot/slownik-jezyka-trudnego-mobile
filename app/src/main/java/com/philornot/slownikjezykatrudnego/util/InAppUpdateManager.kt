package com.philornot.slownikjezykatrudnego.util

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Manages Google Play In-App Updates.
 *
 * Checks if a newer version of the application is available in the Google
 * Play Store and initiates the native Google Play update flow (Immediate
 * update).
 *
 * @property activity The hosting Activity instance.
 */
class InAppUpdateManager(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    /**
     * Checks if an update is available and triggers the native update flow if
     * allowed.
     *
     * @param updateLauncher Launcher used to start the update IntentSender.
     */
    fun checkForAppUpdate(updateLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    val updateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateLauncher,
                        updateOptions
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start immediate in-app update flow", e)
                }
            } else {
                Log.d(
                    TAG,
                    "No update available or immediate update not allowed: ${appUpdateInfo.updateAvailability()}"
                )
            }
        }.addOnFailureListener { e ->
            // ERROR_APP_NOT_OWNED (-10) is expected on sideloaded / debug builds
            // where the app was not acquired from the Play Store.
            val isNotOwned = e is com.google.android.play.core.install.InstallException &&
                    e.errorCode == com.google.android.play.core.install.model.InstallErrorCode.ERROR_APP_NOT_OWNED
            if (isNotOwned) {
                Log.d(TAG, "Skipping in-app update check: app not installed from Play Store")
            } else {
                Log.e(TAG, "Failed to check for in-app updates", e)
            }
        }
    }

    /**
     * Resumes an in-progress immediate update if the app returns to
     * foreground.
     *
     * Should be called from [Activity.onResume].
     *
     * @param updateLauncher Launcher used to start the update IntentSender.
     */
    fun onResume(updateLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    val updateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateLauncher,
                        updateOptions
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to resume immediate in-app update flow", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "InAppUpdateManager"
    }
}
