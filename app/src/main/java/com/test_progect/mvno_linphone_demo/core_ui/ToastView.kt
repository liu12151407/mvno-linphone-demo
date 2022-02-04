package com.test_progect.mvno_linphone_demo.core_ui

import android.content.Context
import android.widget.Toast
import com.test_progect.mvno_linphone_demo.R

interface ToastView {

    fun showInvalidPhoneNumberToast()
    fun showInvalidPhoneNumberCommonToast()
    fun showRemoteAddressErrorToast()
    fun showLocationPermissionDenied()

}

class ToastViewImpl(private val context: Context) : ToastView {

    override fun showInvalidPhoneNumberToast() {
        Toast.makeText(context, R.string.toast_invalid_phone_format, Toast.LENGTH_LONG).show()
    }

    override fun showInvalidPhoneNumberCommonToast() {
        Toast.makeText(
            context,
            R.string.toast_invalid_phone_number_format_common,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showRemoteAddressErrorToast() {
        Toast.makeText(context, R.string.toast_remote_address_error, Toast.LENGTH_LONG).show()
    }

    override fun showLocationPermissionDenied() {
        Toast.makeText(context, "No location permission. Go to settings", Toast.LENGTH_LONG).show()
    }


}