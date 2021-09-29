package com.test_progect.mvno_linphone_demo

import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

private const val PHONE_NUMBER_LENGTH = 12

fun validatePhoneNumber(phoneNumber: String, context: Context): Boolean {
    val isValid = phoneNumber.startsWith("+7") && phoneNumber.length == PHONE_NUMBER_LENGTH
    if (!isValid) {
        Toast.makeText(context, R.string.invalid_phone_number_format, Toast.LENGTH_LONG).show()
    }
    return isValid
}

fun View.hideKeyboard() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } else {
        windowInsetsController?.hide(WindowInsets.Type.ime())
    }
}

@ColorInt
fun Context.getColorCompat(@ColorRes colorResId: Int): Int =
    ContextCompat.getColor(this, colorResId)

private inline fun <reified T> Context.getSystemManager(name: String): T =
    getSystemService(name) as T

private inline val Context.inputMethodManager: InputMethodManager
    get() = applicationContext.getSystemManager(Context.INPUT_METHOD_SERVICE)