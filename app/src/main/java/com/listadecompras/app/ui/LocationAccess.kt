package com.listadecompras.app.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat

/** Each location API call checks permissions and catches permission revocation races. */
internal class LocationAccess(
    private val activity: Activity,
    private val locationManager: LocationManager,
    private val listener: LocationListener
) {
    fun lastKnownLocationSafely(provider: String): Location? {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return null
        return try {
            locationManager.getLastKnownLocation(provider)
        } catch (_: SecurityException) {
            // Uma permissão pode ser revogada após a verificação.
            null
        } catch (_: IllegalArgumentException) {
            // Provider removido/desativado pelo sistema.
            null
        }
    }

    fun requestProviderUpdatesSafely(provider: String): Boolean {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return false
        return try {
            locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            true
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

}
