package za.co.agentkit

import androidx.compose.runtime.mutableStateListOf

class AgentState(private val store: LocalStore) {
    val properties = mutableStateListOf<PropertyItem>()
    val contacts = mutableStateListOf<ContactItem>()
    val viewings = mutableStateListOf<ViewingItem>()

    init {
        properties.addAll(store.loadProperties())
        contacts.addAll(store.loadContacts())
        viewings.addAll(store.loadViewings())
    }

    fun addProperty(item: PropertyItem) {
        properties.add(0, item)
        store.saveProperties(properties)
    }

    fun updateProperty(item: PropertyItem) {
        val index = properties.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            properties[index] = item
            store.saveProperties(properties)
        }
    }

    fun deleteProperty(id: String) {
        properties.removeAll { it.id == id }
        store.saveProperties(properties)
    }

    fun addContact(item: ContactItem) {
        contacts.add(0, item)
        store.saveContacts(contacts)
    }

    fun addViewing(item: ViewingItem) {
        viewings.add(0, item)
        store.saveViewings(viewings)
    }

    fun completeViewing(id: String) {
        val index = viewings.indexOfFirst { it.id == id }
        if (index >= 0) {
            viewings[index] = viewings[index].copy(status = "Completed")
            store.saveViewings(viewings)
        }
    }
}
