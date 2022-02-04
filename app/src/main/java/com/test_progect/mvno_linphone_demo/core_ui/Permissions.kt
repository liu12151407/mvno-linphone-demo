package com.test_progect.mvno_linphone_demo.core_ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.registerPermissionRequestLauncher(
    onPermissionsGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
): ActivityResultLauncher<Array<String>> =
    registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val allPermissionsGranted = permissionsResult.values.all { it }
        if (allPermissionsGranted) {
            onPermissionsGranted.invoke()
        } else {
            onPermissionDenied.invoke()
        }
    }

fun Context.checkPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED