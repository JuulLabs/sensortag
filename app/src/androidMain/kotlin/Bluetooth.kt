package com.juul.sensortag

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.R
import com.juul.kable.Bluetooth

val Bluetooth.permissionsNeeded: List<String> by lazy {
    when {
        // If your app targets Android 9 (API level 28) or lower, you can declare the ACCESS_COARSE_LOCATION permission
        // instead of the ACCESS_FINE_LOCATION permission.
        // https://developer.android.com/guide/topics/connectivity/bluetooth/permissions#declare-android11-or-lower
        SDK_INT <= P -> listOf(ACCESS_COARSE_LOCATION)

        // ACCESS_FINE_LOCATION is necessary because, on Android 11 (API level 30) and lower, a Bluetooth scan could
        // potentially be used to gather information about the location of the user.
        // https://developer.android.com/guide/topics/connectivity/bluetooth/permissions#declare-android11-or-lower
        SDK_INT <= R -> listOf(ACCESS_FINE_LOCATION)

        // On Android 12 (API level 31) or higher, users can request that your app retrieve only approximate location
        // information, even when your app requests the ACCESS_FINE_LOCATION runtime permission.
        // To handle this potential user behavior, don't request the ACCESS_FINE_LOCATION permission by itself. Instead,
        // request both the ACCESS_FINE_LOCATION permission and the ACCESS_COARSE_LOCATION permission in a single
        // runtime request. If you try to request only ACCESS_FINE_LOCATION, the system ignores the request on some
        // releases of Android 12.
        // https://developer.android.com/training/location/permissions#approximate-request
        else /* SDK_INT >= S */ -> listOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
    }
}
