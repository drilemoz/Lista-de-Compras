package com.listadecompras.app.ui

import java.net.URI

/** Restricts resources that can execute or render inside the map's JavaScript bridge. */
internal object MapResourcePolicy {
    const val LOCAL_MAP = "file:///android_asset/nearby_markets_map.html"

    fun isAllowedResource(url: String?): Boolean {
        if (url == LOCAL_MAP) return true
        val uri = runCatching { URI(url ?: return false) }.getOrNull() ?: return false
        if (uri.scheme != "https" || uri.userInfo != null || uri.port != -1 || uri.rawQuery != null || uri.rawFragment != null) return false
        return when (uri.host?.lowercase()) {
            "unpkg.com" -> uri.path == "/leaflet@1.9.4/dist/leaflet.js" ||
                uri.path == "/leaflet@1.9.4/dist/leaflet.css"
            "tile.openstreetmap.org" -> uri.path.matches(Regex("/[0-9]{1,2}/[0-9]+/[0-9]+\\.png"))
            else -> false
        }
    }
}
