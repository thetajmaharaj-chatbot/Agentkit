package za.co.agentkit

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("agentkit_local", Context.MODE_PRIVATE)

    fun loadProperties(): List<PropertyItem> = runCatching {
        val array = JSONArray(prefs.getString("properties", "[]") ?: "[]")
        List(array.length()) { index ->
            val o = array.getJSONObject(index)
            PropertyItem(
                id = o.optString("id"),
                title = o.optString("title"),
                suburb = o.optString("suburb"),
                price = o.optDouble("price"),
                bedrooms = o.optInt("bedrooms"),
                bathrooms = o.optInt("bathrooms"),
                propertyType = o.optString("propertyType"),
                status = o.optString("status", "Active"),
                sellerName = o.optString("sellerName"),
                sellerPhone = o.optString("sellerPhone"),
                notes = o.optString("notes")
            )
        }
    }.getOrDefault(emptyList())

    fun saveProperties(items: List<PropertyItem>) {
        val array = JSONArray()
        items.forEach { p ->
            array.put(
                JSONObject()
                    .put("id", p.id)
                    .put("title", p.title)
                    .put("suburb", p.suburb)
                    .put("price", p.price)
                    .put("bedrooms", p.bedrooms)
                    .put("bathrooms", p.bathrooms)
                    .put("propertyType", p.propertyType)
                    .put("status", p.status)
                    .put("sellerName", p.sellerName)
                    .put("sellerPhone", p.sellerPhone)
                    .put("notes", p.notes)
            )
        }
        prefs.edit().putString("properties", array.toString()).apply()
    }

    fun loadContacts(): List<ContactItem> = runCatching {
        val array = JSONArray(prefs.getString("contacts", "[]") ?: "[]")
        List(array.length()) { index ->
            val o = array.getJSONObject(index)
            ContactItem(
                id = o.optString("id"),
                name = o.optString("name"),
                phone = o.optString("phone"),
                email = o.optString("email"),
                role = o.optString("role", "Buyer"),
                budget = o.optDouble("budget"),
                area = o.optString("area"),
                notes = o.optString("notes")
            )
        }
    }.getOrDefault(emptyList())

    fun saveContacts(items: List<ContactItem>) {
        val array = JSONArray()
        items.forEach { c ->
            array.put(
                JSONObject()
                    .put("id", c.id)
                    .put("name", c.name)
                    .put("phone", c.phone)
                    .put("email", c.email)
                    .put("role", c.role)
                    .put("budget", c.budget)
                    .put("area", c.area)
                    .put("notes", c.notes)
            )
        }
        prefs.edit().putString("contacts", array.toString()).apply()
    }

    fun loadViewings(): List<ViewingItem> = runCatching {
        val array = JSONArray(prefs.getString("viewings", "[]") ?: "[]")
        List(array.length()) { index ->
            val o = array.getJSONObject(index)
            ViewingItem(
                id = o.optString("id"),
                propertyName = o.optString("propertyName"),
                contactName = o.optString("contactName"),
                date = o.optString("date"),
                time = o.optString("time"),
                notes = o.optString("notes"),
                status = o.optString("status", "Scheduled")
            )
        }
    }.getOrDefault(emptyList())

    fun saveViewings(items: List<ViewingItem>) {
        val array = JSONArray()
        items.forEach { v ->
            array.put(
                JSONObject()
                    .put("id", v.id)
                    .put("propertyName", v.propertyName)
                    .put("contactName", v.contactName)
                    .put("date", v.date)
                    .put("time", v.time)
                    .put("notes", v.notes)
                    .put("status", v.status)
            )
        }
        prefs.edit().putString("viewings", array.toString()).apply()
    }
}
