package com.test_progect.mvno_linphone_demo

import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager

fun View.hideKeyboard() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    } else {
        windowInsetsController?.hide(WindowInsets.Type.ime())
    }
}

private inline fun <reified T> Context.getSystemManager(name: String): T =
    getSystemService(name) as T

private inline val Context.inputMethodManager: InputMethodManager
    get() = applicationContext.getSystemManager(Context.INPUT_METHOD_SERVICE)
