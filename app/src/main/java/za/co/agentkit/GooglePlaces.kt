package za.co.agentkit

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity

data class GoogleAddressSelection(
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

object GooglePlacesService {
    fun configured(): Boolean = BuildConfig.GOOGLE_MAPS_API_KEY.isNotBlank()

    fun initialize(context: Context): Boolean {
        if (!configured()) return false
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context.applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        }
        return true
    }

    fun openMaps(context: Context, property: PropertyItem) {
        val uri = when {
            property.latitude != 0.0 || property.longitude != 0.0 ->
                Uri.parse("geo:${property.latitude},${property.longitude}?q=${property.latitude},${property.longitude}(" + Uri.encode(property.title) + ")")
            property.address.isNotBlank() ->
                Uri.parse("geo:0,0?q=" + Uri.encode(property.address))
            else -> Uri.parse("geo:0,0?q=" + Uri.encode(property.title + " " + property.suburb))
        }
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

@Composable
fun GoogleAddressPicker(
    currentQuery: String,
    onSelected: (GoogleAddressSelection) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (data != null && result.resultCode == PlaceAutocompleteActivity.RESULT_OK) {
            val prediction = PlaceAutocomplete.getPredictionFromIntent(data)
            val sessionToken = PlaceAutocomplete.getSessionTokenFromIntent(data)
            if (prediction != null && GooglePlacesService.initialize(context)) {
                loading = true
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.FORMATTED_ADDRESS,
                    Place.Field.LOCATION,
                    Place.Field.ADDRESS_COMPONENTS
                )
                val request = FetchPlaceRequest.builder(prediction.placeId, fields)
                    .setSessionToken(sessionToken)
                    .build()

                Places.createClient(context).fetchPlace(request)
                    .addOnSuccessListener { response ->
                        val place = response.place
                        val components = place.addressComponents?.asList().orEmpty()
                        fun component(vararg types: String): String =
                            components.firstOrNull { c -> types.any { it in c.types } }?.name.orEmpty()

                        val suburb = component(
                            "sublocality_level_1",
                            "sublocality",
                            "neighborhood",
                            "locality"
                        )
                        val city = component("locality", "postal_town")
                        val province = component("administrative_area_level_1")
                        val postal = component("postal_code")
                        val location = place.location
                        onSelected(
                            GoogleAddressSelection(
                                title = prediction.getPrimaryText(null).toString(),
                                address = place.formattedAddress.orEmpty(),
                                suburb = suburb,
                                city = city,
                                province = province,
                                postalCode = postal,
                                latitude = location?.latitude ?: 0.0,
                                longitude = location?.longitude ?: 0.0,
                                placeId = prediction.placeId
                            )
                        )
                        loading = false
                        error = null
                    }
                    .addOnFailureListener {
                        loading = false
                        error = it.message ?: "Google could not load this address."
                    }
            }
        } else if (data != null) {
            val status = PlaceAutocomplete.getResultStatusFromIntent(data)
            if (status != null && !status.isSuccess) {
                error = status.statusMessage ?: "Address search was not completed."
            }
        }
    }

    if (GooglePlacesService.configured()) {
        Button(
            onClick = {
                if (GooglePlacesService.initialize(context)) {
                    val intent = PlaceAutocomplete.IntentBuilder()
                        .setInitialQuery(currentQuery)
                        .build(context)
                    launcher.launch(intent)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            enabled = !loading
        ) {
            Icon(Icons.Default.Search, null)
            Spacer(Modifier.width(8.dp))
            Text(if (loading) "Loading address…" else "Search address with Google")
        }
    } else {
        OutlinedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(Modifier.padding(14.dp)) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Google Places is ready but needs the GOOGLE_MAPS_API_KEY build secret. Manual address entry still works.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}
