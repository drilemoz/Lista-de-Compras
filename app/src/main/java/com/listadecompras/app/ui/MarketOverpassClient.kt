package com.listadecompras.app.ui

import android.location.Location
import com.listadecompras.app.data.MarketPreferencesRepository
import com.listadecompras.app.model.NearbyMarket
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/** Fetches OSM business locations without retaining Activity/UI state. */
internal object MarketOverpassClient {
    fun queryMarkets(location: Location, radiusMeters: Int): List<NearbyMarket> {
        val query = """
            [out:json][timeout:40];
            (
              nwr["shop"~"^(supermarket|convenience|grocery|greengrocer|wholesale|department_store)$"](around:$radiusMeters,${location.latitude},${location.longitude});
              nwr["amenity"="marketplace"](around:$radiusMeters,${location.latitude},${location.longitude});
              nwr["name"~"mercado|supermercado|mercearia|minimercado|atacado|sacolao|sacolão|hortifruti|hortifrúti|conveniencia|conveniência|emporio|empório|armazem|armazém",i](around:$radiusMeters,${location.latitude},${location.longitude});
              nwr["brand"~"mercado|supermercado|mercearia|minimercado|atacado|sacolao|sacolão|hortifruti|hortifrúti|conveniencia|conveniência|emporio|empório|armazem|armazém",i](around:$radiusMeters,${location.latitude},${location.longitude});
            );
            out center tags;
        """.trimIndent()

        val elements = JSONObject(executeOverpass(query)).optJSONArray("elements") ?: JSONArray()
        val markets = mutableListOf<NearbyMarket>()
        for (i in 0 until elements.length()) {
            val item = elements.optJSONObject(i) ?: continue
            val tags = item.optJSONObject("tags") ?: JSONObject()
            val center = item.optJSONObject("center")
            val lat = if (item.has("lat")) item.optDouble("lat") else center?.optDouble("lat") ?: Double.NaN
            val lon = if (item.has("lon")) item.optDouble("lon") else center?.optDouble("lon") ?: Double.NaN
            if (lat.isNaN() || lon.isNaN()) continue

            val name = tags.optString("name").ifBlank { tags.optString("brand").ifBlank { "Mercado" } }
            val type = item.optString("type")
            val id = item.optLong("id", 0L)
            val key = if (type.isNotBlank() && id > 0L) "osm:$type:$id" else MarketPreferencesRepository.fallbackKey(name, lat, lon)
            val distance = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, lat, lon, distance)
            markets += NearbyMarket(
                key = key,
                name = name,
                lat = lat,
                lon = lon,
                distanceMeters = distance[0].toDouble(),
                address = buildAddress(tags),
                kind = parseKind(tags, name)
            )
        }
        return markets.distinctBy { it.key }.sortedBy { it.distanceMeters }.take(MAX_RESULTS)
    }

    private fun parseKind(tags: JSONObject, fallbackName: String): String {
        val shop = tags.optString("shop")
        val amenity = tags.optString("amenity")
        val text = (fallbackName + " " + tags.optString("brand")).lowercase(Locale("pt", "BR"))
        return when {
            amenity == "marketplace" -> "Feira / mercado"
            shop == "supermarket" -> "Supermercado"
            shop == "convenience" -> "Conveniência"
            shop == "grocery" -> "Mercearia"
            shop == "greengrocer" -> "Hortifruti"
            shop == "wholesale" -> "Atacado"
            shop == "department_store" -> "Loja de departamentos"
            text.contains("hort") -> "Hortifruti"
            text.contains("atacad") -> "Atacado"
            text.contains("conven") -> "Conveniência"
            text.contains("merc") || text.contains("mercer") || text.contains("armaz") -> "Mercado"
            else -> "Mercado"
        }
    }

    private fun executeOverpass(query: String): String {
        var lastError: Throwable? = null
        for (endpoint in OVERPASS_URLS) {
            val result = runCatching {
                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    doOutput = true
                    setRequestProperty("User-Agent", "ListaDeCompras/${com.listadecompras.app.BuildConfig.VERSION_NAME} Android")
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                }
                try {
                    val body = "data=" + URLEncoder.encode(query, Charsets.UTF_8.name())
                    connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                    if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
                    connection.inputStream.bufferedReader().use { it.readText() }
                } finally {
                    connection.disconnect()
                }
            }
            if (result.isSuccess) return result.getOrThrow()
            lastError = result.exceptionOrNull()
        }
        throw lastError ?: IllegalStateException("Falha ao consultar Overpass")
    }

    private fun buildAddress(tags: JSONObject): String {
        val street = tags.optString("addr:street")
        val number = tags.optString("addr:housenumber")
        val neighborhood = tags.optString("addr:suburb").ifBlank { tags.optString("addr:neighbourhood") }
        val city = tags.optString("addr:city")
        return listOf(
            listOf(street, number).filter { it.isNotBlank() }.joinToString(", "),
            neighborhood,
            city
        ).filter { it.isNotBlank() }.distinct().joinToString(" • ")
    }

    private const val MAX_RESULTS = 120
    private val OVERPASS_URLS = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter"
    )
}
