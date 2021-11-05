package com.igao.locationsample.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.igao.locationsample.permission.PermissionHelperActivity

/**
 * @author Igor Gonçalves
 * @since 2021
 * https://www.linkedin.com/in/igao/
 */
abstract class LocationHelperActivity : PermissionHelperActivity() {

    val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val cancellationToken: CancellationTokenSource by lazy {
        CancellationTokenSource()
    }

    private val locationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onPermissionsGranted()
        }

    val dialog: AlertDialog by lazy {
        AlertDialog.Builder(this).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog.dismiss()
    }

    /**
     * Required permissions to flow
     */
    override fun permissionsToRequest(): Array<String> {
        return arrayOf(
            fineLocation,
            coarseLocation
        )
    }

    /**
     * Granted Location permissions
     */
    override fun onPermissionsGranted() {
        searchForLocationIfCan()
    }

    /**
     * Rejected Location permissions
     */
    override fun onPermissionsRejected() {
        buildInformationDialog()
    }

    /**
     * If have the location activated on device, continue the flow to findLocation.
     * And if not have the location activated on device, show the information dialog.
     */
    private fun searchForLocationIfCan() {
        if (hasLocationActivated())
            findLocation()
        else
            buildLocationInformationDialog()
    }

    /**
     * Check if have permissions to get current location.
     * If have the permissions look for them.
     */
    private fun findLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionsRejected()
            return
        }


        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener {
            onLocationFound(it)
        }.addOnFailureListener {
            onLocationFailure(it)
        }
    }

    /**
     * Called on have founded location giving a next step.
     */
    private fun onLocationFound(location: Location) {
        Toast.makeText(this, "${location.latitude}/${location.longitude}", Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Called if have a failure on finding location
     */
    private fun onLocationFailure(it: Exception) {
        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Verify if location has activated on the device
     */
    private fun hasLocationActivated(): Boolean {
        return try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Build a dialog to inform the necessity of activation of location on the device
     */
    private fun buildLocationInformationDialog() {
        dialog.setTitle("Ops")
        dialog.setMessage("Precisamos que ative seu gps para continuar")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, which ->
            redirectToLocationSettings()
        }

        dialog.show()
    }

    /**
     * Build a dialog to inform the necessity of user location
     */
    private fun buildInformationDialog() {
        dialog.setTitle("Ops")
        dialog.setMessage("Precisamos da sua localização para prosseguirmos com a entrega")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, which ->
            redirectToSettings()
            this.finish()
        }

        dialog.show()
    }

    /**
     * Calls the device location settings
     */
    private fun redirectToLocationSettings() {
        val viewIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        locationLauncher.launch(viewIntent)
    }

    /**
     * Build the intent to redirect to device app settings
     */
    private fun buildAppSettingsIntent(): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri

        return intent
    }

    /**
     * Calls the device app settings
     */
    private fun redirectToSettings() {
        val intent = buildAppSettingsIntent()
        startActivity(intent)
    }
}