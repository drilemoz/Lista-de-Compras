package com.listadecompras.app.ui

import org.junit.Assert.*
import org.junit.Test

class MapResourcePolicyTest {
    @Test fun acceptsOnlyExplicitMapResources() {
        assertTrue(MapResourcePolicy.isAllowedResource(MapResourcePolicy.LOCAL_MAP))
        assertTrue(MapResourcePolicy.isAllowedResource("https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"))
        assertTrue(MapResourcePolicy.isAllowedResource("https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"))
        assertTrue(MapResourcePolicy.isAllowedResource("https://tile.openstreetmap.org/13/1234/5678.png"))
    }

    @Test fun blocksUntrustedSourcesAndNavigations() {
        listOf(null, "https://example.com/script.js", "http://unpkg.com/leaflet@1.9.4/dist/leaflet.js",
            "https://unpkg.com/leaflet@1.9.5/dist/leaflet.js", "https://unpkg.com/evil.js",
            "https://tile.openstreetmap.org.evil.tld/13/1234/5678.png",
            "https://tile.openstreetmap.org/13/1234/5678.png?token=abc",
            "https://tile.openstreetmap.org/a/b/c.png", "javascript:alert(1)",
            "file:///sdcard/Downloads/a.html", "file:///android_asset/some_other.html"
        ).forEach { assertFalse("Expected blocked: $it", MapResourcePolicy.isAllowedResource(it)) }
    }
}
