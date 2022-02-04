package com.test_progect.mvno_linphone_demo

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.test_progect.mvno_linphone_demo.core_ui.checkPermissionGranted
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface LocationProvider {

    suspend fun getLocation(): Location?

}

class LocationProviderImpl(
    private val activity: AppCompatActivity,
) : LocationProvider {

    override suspend fun getLocation(): Location? =
        if (!activity.checkPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            null
        } else {
            findLocation()
        }

    @SuppressLint("MissingPermission")
    private suspend fun findLocation(): Location? =
        suspendCoroutine { continuation ->
            LocationServices.getFusedLocationProviderClient(activity).lastLocation.apply {
                addOnSuccessListener { result -> continuation.resume(result) }
                addOnFailureListener { continuation.resume(null) }
            }
        }

}