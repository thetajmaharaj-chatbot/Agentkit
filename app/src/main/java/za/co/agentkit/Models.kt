package za.co.agentkit

data class PropertyItem(
    val id: String,
    val title: String,
    val suburb: String,
    val price: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val propertyType: String,
    val status: String,
    val sellerName: String,
    val sellerPhone: String,
    val notes: String
)

data class ContactItem(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String,
    val budget: Double,
    val area: String,
    val notes: String
)

data class ViewingItem(
    val id: String,
    val propertyName: String,
    val contactName: String,
    val date: String,
    val time: String,
    val notes: String,
    val status: String
)
