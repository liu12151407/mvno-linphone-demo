package com.test_progect.mvno_linphone_demo

import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly

private const val VALID_PHONE_NUMBER_LENGTH = 12

fun validatePhoneNumber(phoneNumber: String): Boolean =
    phoneNumber.startsWith("+7") &&
            phoneNumber.length == VALID_PHONE_NUMBER_LENGTH &&
            phoneNumber.removeRange(0, 1).isDigitsOnly()

fun tryToFormatPhoneNumber(phoneNumber: String, onInvalidPhoneNumber: () -> Unit): String? {
    val formattedPhone = phoneNumber.filterIndexed { index, ch ->
        ch.isDigit() || if (index == 0) ch == '+' else false
    }
    val validateSize = formattedPhone.length in 11..14
    val startsWithPlus = formattedPhone.startsWith("+")
    return when {
        validateSize.not() -> {
            onInvalidPhoneNumber.invoke()
            null
        }
        startsWithPlus.not() -> "+" + formattedPhone.replaceFirstChar {
            it.digitToInt().minus(1).digitToChar()
        }
        else -> phoneNumber
    }
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