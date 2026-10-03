package za.co.agentkit

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class OSMAddressSelection(
    val title: String,
    val address: String,
    val suburb: String,
    val city: String,
    val province: String,
    val postalCode: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String
)

object OpenStreetMapService {
    fun openMap(context: Context, property: PropertyItem) {
        val url = when {
            property.latitude != 0.0 || property.longitude != 0.0 ->
                "https://www.openstreetmap.org/?mlat=" + property.latitude +
                    "&mlon=" + property.longitude +
                    "#map=18/" + property.latitude + "/" + property.longitude
            property.address.isNotBlank() ->
                "https://www.openstreetmap.org/search?query=" + Uri.encode(property.address)
            else ->
                "https://www.openstreetmap.org/search?query=" + Uri.encode(property.title + " " + property.suburb)
        }
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    suspend fun search(query: String): List<OSMAddressSelection> = withContext(Dispatchers.IO) {
        if (query.trim().length < 3) return@withContext emptyList()

        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val url = URL(
            "https://nominatim.openstreetmap.org/search" +
                "?format=jsonv2&addressdetails=1&limit=5&countrycodes=za&q=" + encoded
        )
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "AgentKitSA/0.5 Android")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Accept-Language", "en-ZA,en;q=0.9")

            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("OpenStreetMap search returned " + connection.responseCode)
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(body)

            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                val address = item.optJSONObject("address")

                fun a(vararg names: String): String {
                    if (address == null) return ""
                    for (name in names) {
                        val value = address.optString(name)
                        if (value.isNotBlank()) return value
                    }
                    return ""
                }

                val road = a("road", "pedestrian", "residential", "path")
                val house = a("house_number")
                val primary = listOf(house, road).filter { it.isNotBlank() }.joinToString(" ")
                    .ifBlank { item.optString("name").ifBlank { item.optString("display_name").substringBefore(",") } }

                OSMAddressSelection(
                    title = primary,
                    address = item.optString("display_name"),
                    suburb = a("suburb", "neighbourhood", "quarter", "city_district", "village"),
                    city = a("city", "town", "municipality", "village"),
                    province = a("state", "province"),
                    postalCode = a("postcode"),
                    latitude = item.optString("lat").toDoubleOrNull() ?: 0.0,
                    longitude = item.optString("lon").toDoubleOrNull() ?: 0.0,
                    placeId = item.optString("osm_type") + ":" + item.optString("osm_id")
                )
            }
        } finally {
            connection.disconnect()
        }
    }
}

@Composable
fun OSMAddressPicker(
    currentQuery: String,
    onSelected: (OSMAddressSelection) -> Unit
) {
    var query by remember(currentQuery) { mutableStateOf(currentQuery) }
    var results by remember { mutableStateOf<List<OSMAddressSelection>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        label = { Text("Search OpenStreetMap") },
        placeholder = { Text("Street, suburb, city") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )

    Button(
        onClick = {
            loading = true
            searched = true
            error = null
            scope.launch {
                try {
                    results = OpenStreetMapService.search(query)
                } catch (t: Throwable) {
                    results = emptyList()
                    error = t.message ?: "Address search failed."
                } finally {
                    loading = false
                }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        enabled = !loading && query.trim().length >= 3
    ) {
        Icon(Icons.Default.Map, null)
        Spacer(Modifier.width(8.dp))
        Text(if (loading) "Searching…" else "Search OpenStreetMap")
    }

    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    if (!loading && searched && results.isEmpty() && error == null) {
        Text(
            "No matching South African address found. You can still enter the address manually.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }

    results.forEach { item ->
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                onSelected(item)
                query = item.address
                results = emptyList()
                searched = false
            },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        item.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    Text(
        "Address data © OpenStreetMap contributors",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 5.dp)
    )
}
