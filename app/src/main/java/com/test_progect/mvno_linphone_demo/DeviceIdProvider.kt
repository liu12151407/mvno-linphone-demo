package com.test_progect.mvno_linphone_demo

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

interface DeviceIdProvider {

    fun getDeviceId(): String

}

class DeviceIdProviderImpl(
    private val activity: AppCompatActivity,
) : DeviceIdProvider {

    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String =
        Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)

}