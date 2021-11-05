package com.igao.locationsample.permission

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * @author Igor Gonçalves
 * @since 2021
 * https://www.linkedin.com/in/igao/
 */
abstract class PermissionHelperActivity : AppCompatActivity() {

    abstract fun permissionsToRequest(): Array<String>

    abstract fun onPermissionsGranted()

    abstract fun onPermissionsRejected()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchIfHavePermissions()
    }

    /**
     * If the requested permissions are already accepted just continue the flow.
     */
    private fun searchIfHavePermissions() {
        if (checkIfAllPermissionsAreGranted().isNotEmpty()) {
            requestPermissions()
        } else {
            onPermissionsGranted()
        }
    }

    /**
     * Request the permissions
     */
    private fun requestPermissions() {
        val locationPermission = permissionsCallback()
        locationPermission.launch(permissionsToRequest())
    }

    /**
     * Retrieve the requested permissions callback and verify that have granted all
     */
    private fun permissionsCallback(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isGranted = permissions.entries.filter { it.value == false }.isNullOrEmpty()

            if (isGranted) {
                onPermissionsGranted()
            } else {
                onPermissionsRejected()
            }
        }
    }

    /**
     * Check if the permission have granted
     */
    private fun checkIfAllPermissionsAreGranted(): List<String> {
        return permissionsToRequest().filter {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_DENIED
        }
    }
}