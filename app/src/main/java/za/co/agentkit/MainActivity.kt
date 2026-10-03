package za.co.agentkit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val Ink = Color(0xFF102A43)
private val Brand = Color(0xFF0A7A67)
private val BrandSoft = Color(0xFFE8F5F1)
private val Bg = Color(0xFFF6F8FA)
private val Muted = Color(0xFF64748B)
private val Border = Color(0xFFE2E8F0)

data class NavItem(val title: String, val icon: ImageVector)
data class DrawerNav(val title: String, val icon: ImageVector, val tab: Int? = null, val route: String? = null)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Brand,
                    onPrimary = Color.White,
                    primaryContainer = BrandSoft,
                    onPrimaryContainer = Ink,
                    secondary = Navy,
                    onSecondary = Color.White,
                    tertiary = Accent,
                    background = Bg,
                    surface = Color.White,
                    surfaceVariant = SoftSurface,
                    outline = Border
                )
            ) {
                val context = LocalContext.current
                val state = remember { AgentState(LocalStore(context.applicationContext)) }
                AgentKit(state)
            }
        }
    }
}

@Composable
fun AgentKit(state: AgentState) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var route by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPropertyId by rememberSaveable { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val primaryItems = listOf(
        DrawerNav("Dashboard", Icons.Default.SpaceDashboard, tab = 0),
        DrawerNav("Properties", Icons.Default.Apartment, tab = 1),
        DrawerNav("Leads & CRM", Icons.Default.Groups, tab = 2),
        DrawerNav("Calendar", Icons.Default.CalendarMonth, tab = 3)
    )
    val toolItems = listOf(
        DrawerNav("Marketing Studio", Icons.Default.AutoAwesome, route = "marketing"),
        DrawerNav("Documents", Icons.Default.Folder, route = "documents"),
        DrawerNav("Voice Notes", Icons.Default.Mic, route = "voice_notes"),
        DrawerNav("Deal Pipeline", Icons.Default.AccountTree, route = "pipeline"),
        DrawerNav("Commission", Icons.Default.Calculate, route = "commission"),
        DrawerNav("Seller Reports", Icons.Default.PictureAsPdf, route = "seller_report")
    )

    fun openRootTab(index: Int) {
        tab = index
        route = null
    }

    val openProperty: (String) -> Unit = {
        selectedPropertyId = it
        route = "property_detail"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = route == null,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(312.dp),
                drawerContainerColor = Color.White,
                drawerContentColor = Ink
            ) {
                CorporateDrawerHeader()
                Text(
                    "WORKSPACE",
                    color = Muted,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 22.dp, top = 18.dp, bottom = 8.dp)
                )
                primaryItems.forEach { item ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                item.title,
                                fontWeight = if (route == null && tab == item.tab) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        selected = route == null && tab == item.tab,
                        onClick = {
                            item.tab?.let(::openRootTab)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(item.icon, null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandSoft,
                            selectedIconColor = Brand,
                            selectedTextColor = Ink,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Ink
                        )
                    )
                }

                HorizontalDivider(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), color = Border)

                Text(
                    "AGENT TOOLS",
                    color = Muted,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 22.dp, bottom = 8.dp)
                )
                toolItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title, fontWeight = FontWeight.Medium) },
                        selected = route == item.route,
                        onClick = {
                            route = item.route
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(item.icon, null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandSoft,
                            selectedIconColor = Brand,
                            selectedTextColor = Ink,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Ink
                        )
                    )
                }

                Spacer(Modifier.weight(1f))
                Surface(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    color = Navy,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(34.dp).background(Accent, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AK", color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                        Column(Modifier.padding(start = 10.dp)) {
                            Text("AgentKit SA", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Offline-ready workspace", color = Color.White.copy(alpha = .70f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Bg,
            topBar = {
                if (route == null) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    when (tab) {
                                        0 -> "AgentKit SA"
                                        1 -> "Properties"
                                        2 -> "Leads & CRM"
                                        else -> "Calendar"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    "Independent Agent Workspace",
                                    color = Color.White.copy(alpha = .68f),
                                    fontSize = 10.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Open menu", tint = Color.White)
                            }
                        },
                        actions = {
                            Surface(
                                color = Color.White.copy(alpha = .10f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    "v0.6",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
                    )
                }
            },
            floatingActionButton = {
                if (route == null) {
                    ExtendedFloatingActionButton(
                        onClick = { route = "quick_add" },
                        containerColor = Brand,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text("Quick add", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (route) {
                    "quick_add" -> QuickAddScreen({ route = null }) { route = it }
                    "add_property" -> AddPropertyScreen(
                        onBack = { route = null },
                        onSave = {
                            state.addProperty(it)
                            route = null
                            tab = 1
                        }
                    )
                    "property_detail" -> {
                        val property = state.properties.firstOrNull { it.id == selectedPropertyId }
                        if (property == null) {
                            EmptyScreen("Property not found") { route = null }
                        } else {
                            PropertyDetailScreen(
                                property = property,
                                onBack = { route = null },
                                onStatus = { status -> state.updateProperty(property.copy(status = status)) },
                                onDelete = {
                                    state.deleteProperty(property.id)
                                    route = null
                                },
                                onMarketing = { route = "marketing" },
                                onSellerReport = { route = "seller_report" }
                            )
                        }
                    }
                    "add_contact" -> AddContactScreen(
                        onBack = { route = null },
                        onSave = {
                            state.addContact(it)
                            route = null
                            tab = 2
                        }
                    )
                    "schedule_viewing" -> ScheduleViewingScreen(
                        state = state,
                        onBack = { route = null },
                        onSave = {
                            state.addViewing(it)
                            route = null
                            tab = 3
                        }
                    )
                    "commission" -> CommissionScreen { route = null }
                    "documents" -> DocumentVaultScreen { route = null }
                    "voice_notes" -> VoiceNotesScreen { route = null }
                    "marketing" -> MarketingStudioScreen(state.properties, selectedPropertyId) { route = null }
                    "seller_report" -> SellerReportScreen(state.properties, state.viewings, selectedPropertyId) { route = null }
                    "pipeline" -> PipelineScreen(state.properties) { route = null }
                    else -> when (tab) {
                        0 -> HomeScreen(state, { route = it }, openProperty)
                        1 -> PropertiesScreen(state, { route = "add_property" }, openProperty)
                        2 -> LeadsScreen(state) { route = "add_contact" }
                        else -> CalendarScreen(state) { route = "schedule_viewing" }
                    }
                }
            }
        }
    }
}

@Composable
private fun CorporateDrawerHeader() {
    Surface(color = Navy) {
        Column(Modifier.fillMaxWidth().padding(22.dp, 28.dp, 22.dp, 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(Accent, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AK", color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("AGENTKIT", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = 1.sp)
                    Text("SOUTH AFRICA", color = Color.White.copy(alpha = .60f), fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.6.sp)
                }
            }
            Text(
                "Your property business in one place.",
                color = Color.White.copy(alpha = .80f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun HomeScreen(state: AgentState, open: (String) -> Unit, openProperty: (String) -> Unit) {
    val greeting = when (LocalTime.now().hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
    val active = state.properties.count { it.status == "Active" }
    val pipeline = state.properties.filter { it.status != "Sold" }.sumOf { it.price }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Header(greeting, date)
        CardBlock("YOUR PIPELINE", money(pipeline), "$active active properties") { open("pipeline") }
        SectionTitle("Today")
        Row(Modifier.padding(horizontal = 14.dp)) {
            Metric("${state.viewings.count { it.status == "Scheduled" }}", "Viewings", Icons.Default.Visibility) { open("schedule_viewing") }
            Metric("${state.contacts.size}", "Contacts", Icons.Default.Groups) { }
        }
        SectionTitle("Quick actions")
        ActionTile("Add property", "Capture a new listing", Icons.Default.AddHome) { open("add_property") }
        ActionTile("Add lead", "Save a buyer or seller", Icons.Default.PersonAdd) { open("add_contact") }
        ActionTile("Schedule viewing", "Book a property viewing", Icons.Default.EventAvailable) { open("schedule_viewing") }
        ActionTile("Marketing Studio", "Create listing copy ready to share", Icons.Default.AutoAwesome) { open("marketing") }

        if (state.properties.isNotEmpty()) {
            SectionTitle("Recent properties")
            state.properties.take(3).forEach { PropertyCard(it) { openProperty(it.id) } }
        } else {
            EmptyCard("No properties yet", "Add your first property to start building your portfolio.")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PropertiesScreen(state: AgentState, openAdd: () -> Unit, openProperty: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = state.properties.filter {
        query.isBlank() ||
            it.title.contains(query, true) ||
            it.suburb.contains(query, true) ||
            it.address.contains(query, true) ||
            it.city.contains(query, true) ||
            it.province.contains(query, true) ||
            it.postalCode.contains(query, true) ||
            it.status.contains(query, true)
    }
    Column {
        Header("Properties", "${state.properties.size} properties")
        SearchField(query, { query = it }, "Search address, suburb or status")
        Button(
            onClick = openAdd,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add property")
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            if (filtered.isEmpty()) EmptyCard("No matching properties", "Add a listing or change your search.")
            else filtered.forEach { PropertyCard(it) { openProperty(it.id) } }
        }
    }
}

@Composable
private fun LeadsScreen(state: AgentState, openAdd: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("All") }
    val context = LocalContext.current
    val filtered = state.contacts.filter {
        (role == "All" || it.role == role) &&
            (query.isBlank() || it.name.contains(query, true) || it.area.contains(query, true) || it.phone.contains(query))
    }

    Column {
        Header("Leads & CRM", "${state.contacts.size} contacts")
        SearchField(query, { query = it }, "Search name, area or phone")
        ChoiceRow(listOf("All", "Buyer", "Seller"), role) { role = it }
        Button(
            onClick = openAdd,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.PersonAdd, null)
            Spacer(Modifier.width(8.dp))
            Text("Add contact")
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            if (filtered.isEmpty()) EmptyCard("No contacts found", "Capture buyers and sellers so every follow-up stays in one place.")
            filtered.forEach { contact ->
                Card(
                    Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(44.dp).background(BrandSoft, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(if (contact.role == "Buyer") Icons.Default.PersonSearch else Icons.Default.RealEstateAgent, null, tint = Brand)
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(contact.name, fontWeight = FontWeight.Bold, color = Ink, fontSize = 17.sp)
                                Text(contact.role + " • " + contact.area.ifBlank { "Area not set" }, color = Muted, fontSize = 13.sp)
                            }
                            Text(if (contact.budget > 0) money(contact.budget) else "", color = Brand, fontWeight = FontWeight.SemiBold)
                        }
                        if (contact.notes.isNotBlank()) Text(contact.notes, Modifier.padding(top = 12.dp), color = Muted, fontSize = 13.sp)
                        Row(Modifier.padding(top = 12.dp)) {
                            OutlinedButton(onClick = { dial(context, contact.phone) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Phone, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Call")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { whatsapp(context, contact.phone) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Chat, null)
                                Spacer(Modifier.width(6.dp))
                                Text("WhatsApp")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarScreen(state: AgentState, openAdd: () -> Unit) {
    Column {
        Header("Calendar", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")))
        Button(
            onClick = openAdd,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.EventAvailable, null)
            Spacer(Modifier.width(8.dp))
            Text("Schedule viewing")
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            if (state.viewings.isEmpty()) EmptyCard("No viewings scheduled", "Create a viewing and it will appear here.")
            state.viewings.forEach { viewing ->
                Card(
                    Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).background(BrandSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Visibility, null, tint = Brand)
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(viewing.propertyName, fontWeight = FontWeight.Bold, color = Ink)
                                Text(viewing.date + " • " + viewing.time, color = Muted, fontSize = 13.sp)
                                Text(viewing.contactName, color = Muted, fontSize = 13.sp)
                            }
                            StatusPill(viewing.status)
                        }
                        if (viewing.notes.isNotBlank()) Text(viewing.notes, Modifier.padding(top = 10.dp), color = Muted, fontSize = 13.sp)
                        if (viewing.status != "Completed") {
                            TextButton(onClick = { state.completeViewing(viewing.id) }, modifier = Modifier.align(Alignment.End)) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Mark completed")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreScreen(open: (String) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Header("AgentKit SA", "Your professional toolkit")
        SectionTitle("Business tools")
        ActionTile("Deal pipeline", "See draft, active, offer and sold stages", Icons.Default.AccountTree) { open("pipeline") }
        ActionTile("Commission calculator", "Calculate commission and VAT", Icons.Default.Calculate) { open("commission") }
        ActionTile("Marketing Studio", "Generate property listing copy", Icons.Default.AutoAwesome) { open("marketing") }
        ActionTile("Seller report", "Build a seller activity summary", Icons.Default.PictureAsPdf) { open("seller_report") }
        ActionTile("Documents", "View PDFs, DOCX, XLSX, images, text and CSV files", Icons.Default.Folder) { open("documents") }
        ActionTile("Voice notes", "Record and replay private agent voice memos", Icons.Default.Mic) { open("voice_notes") }
        ActionTile("Agent branding", "Agency logo and agent profile", Icons.Default.Palette) { }
        ActionTile("Settings", "Security, backup and preferences", Icons.Default.Settings) { }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun QuickAddScreen(onBack: () -> Unit, open: (String) -> Unit) {
    PageScaffold("Quick add", "Choose what you want to create", onBack) {
        ActionTile("Property", "Capture a listing", Icons.Default.AddHome) { open("add_property") }
        ActionTile("Contact", "Add a buyer or seller", Icons.Default.PersonAdd) { open("add_contact") }
        ActionTile("Viewing", "Schedule a property viewing", Icons.Default.EventAvailable) { open("schedule_viewing") }
        ActionTile("Document", "Import a property or client document", Icons.Default.UploadFile) { open("documents") }
        ActionTile("Voice note", "Record a quick private memo", Icons.Default.Mic) { open("voice_notes") }
    }
}

@Composable
private fun AddPropertyScreen(onBack: () -> Unit, onSave: (PropertyItem) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var title by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var suburb by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var province by rememberSaveable { mutableStateOf("") }
    var postalCode by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableDoubleStateOf(0.0) }
    var longitude by rememberSaveable { mutableDoubleStateOf(0.0) }
    var googlePlaceId by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var bedrooms by rememberSaveable { mutableStateOf("") }
    var bathrooms by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("House") }
    var status by rememberSaveable { mutableStateOf("Active") }
    var sellerName by rememberSaveable { mutableStateOf("") }
    var sellerPhone by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    PageScaffold("Add property", "Step ${step + 1} of 4", onBack) {
        LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))
        when (step) {
            0 -> {
                FormTitle("Location & price", "Find the address with OpenStreetMap or enter it manually.")
                OSMAddressPicker(
                    currentQuery = address.ifBlank { title },
                    onSelected = { selected ->
                        title = selected.title.ifBlank { title }
                        address = selected.address
                        if (selected.suburb.isNotBlank()) suburb = selected.suburb
                        if (selected.city.isNotBlank()) city = selected.city
                        if (selected.province.isNotBlank()) province = selected.province
                        if (selected.postalCode.isNotBlank()) postalCode = selected.postalCode
                        latitude = selected.latitude
                        longitude = selected.longitude
                        googlePlaceId = selected.placeId
                    }
                )
                Field(title, { title = it }, "Property title / street")
                Field(address, { address = it }, "Full address")
                Field(suburb, { suburb = it }, "Suburb / area")
                Field(city, { city = it }, "City / town")
                Field(province, { province = it }, "Province")
                Field(postalCode, { postalCode = it }, "Postal code")
                if (latitude != 0.0 || longitude != 0.0) {
                    Text(
                        "OpenStreetMap location: %.5f, %.5f".format(latitude, longitude),
                        color = Muted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Field(price, { price = it }, "Asking price", KeyboardType.Number)
            }
            1 -> {
                FormTitle("Property details", "Add the key details buyers scan first.")
                ChoiceRow(listOf("House", "Apartment", "Townhouse", "Land"), type) { type = it }
                Field(bedrooms, { bedrooms = it }, "Bedrooms", KeyboardType.Number)
                Field(bathrooms, { bathrooms = it }, "Bathrooms", KeyboardType.Number)
                ChoiceRow(listOf("Active", "Draft"), status) { status = it }
            }
            2 -> {
                FormTitle("Seller", "Keep the owner linked to the listing.")
                Field(sellerName, { sellerName = it }, "Seller name")
                Field(sellerPhone, { sellerPhone = it }, "Seller phone", KeyboardType.Phone)
                Field(notes, { notes = it }, "Notes", minLines = 4)
            }
            else -> {
                FormTitle("Review", "Check the listing before saving.")
                ReviewLine("Property", title)
                ReviewLine("Address", address.ifBlank { "Not added" })
                ReviewLine("Area", listOf(suburb, city, province, postalCode).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Not added" })
                if (latitude != 0.0 || longitude != 0.0) ReviewLine("GPS", "%.5f, %.5f".format(latitude, longitude))
                ReviewLine("Price", money(price.toDoubleOrNull() ?: 0.0))
                ReviewLine("Type", type)
                ReviewLine("Bedrooms", bedrooms.ifBlank { "0" })
                ReviewLine("Bathrooms", bathrooms.ifBlank { "0" })
                ReviewLine("Status", status)
                ReviewLine("Seller", sellerName.ifBlank { "Not added" })
            }
        }
        Spacer(Modifier.height(18.dp))
        Row {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) { Text("Back") }
                Spacer(Modifier.width(10.dp))
            }
            if (step < 3) {
                Button(
                    onClick = { step++ },
                    enabled = if (step == 0) title.isNotBlank() && suburb.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0 else true,
                    modifier = Modifier.weight(1f)
                ) { Text("Continue") }
            } else {
                Button(
                    onClick = {
                        onSave(PropertyItem(
                            id = System.currentTimeMillis().toString(),
                            title = title.trim(),
                            suburb = suburb.trim(),
                            price = price.toDoubleOrNull() ?: 0.0,
                            bedrooms = bedrooms.toIntOrNull() ?: 0,
                            bathrooms = bathrooms.toIntOrNull() ?: 0,
                            propertyType = type,
                            status = status,
                            sellerName = sellerName.trim(),
                            sellerPhone = sellerPhone.trim(),
                            notes = notes.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            province = province.trim(),
                            postalCode = postalCode.trim(),
                            latitude = latitude,
                            longitude = longitude,
                            googlePlaceId = googlePlaceId
                        ))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save property")
                }
            }
        }
    }
}

@Composable
private fun AddContactScreen(onBack: () -> Unit, onSave: (ContactItem) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("Buyer") }
    var budget by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    PageScaffold("Add contact", "Capture a buyer or seller", onBack) {
        FormTitle("Contact details", "Save enough information to make the next follow-up easy.")
        ChoiceRow(listOf("Buyer", "Seller"), role) { role = it }
        Field(name, { name = it }, "Full name")
        Field(phone, { phone = it }, "Phone / WhatsApp", KeyboardType.Phone)
        Field(email, { email = it }, "Email", KeyboardType.Email)
        Field(area, { area = it }, if (role == "Buyer") "Preferred area" else "Property area")
        if (role == "Buyer") Field(budget, { budget = it }, "Budget", KeyboardType.Number)
        Field(notes, { notes = it }, "Notes", minLines = 4)
        Button(
            onClick = {
                onSave(ContactItem(
                    id = System.currentTimeMillis().toString(),
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    role = role,
                    budget = budget.toDoubleOrNull() ?: 0.0,
                    area = area.trim(),
                    notes = notes.trim()
                ))
            },
            enabled = name.isNotBlank() && phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Icon(Icons.Default.Save, null)
            Spacer(Modifier.width(8.dp))
            Text("Save contact")
        }
    }
}

@Composable
private fun ScheduleViewingScreen(state: AgentState, onBack: () -> Unit, onSave: (ViewingItem) -> Unit) {
    var propertyName by rememberSaveable { mutableStateOf(state.properties.firstOrNull()?.title ?: "") }
    var contactName by rememberSaveable { mutableStateOf(state.contacts.firstOrNull()?.name ?: "") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var time by rememberSaveable { mutableStateOf("10:00") }
    var notes by rememberSaveable { mutableStateOf("") }

    PageScaffold("Schedule viewing", "Book the appointment and keep it visible", onBack) {
        if (state.properties.isEmpty()) {
            EmptyCard("Add a property first", "A viewing needs a property.")
        } else {
            FormTitle("Viewing details", "Use your saved listing and contact information.")
            Field(propertyName, { propertyName = it }, "Property")
            if (state.properties.size > 1) ChoiceRow(state.properties.take(3).map { it.title }, propertyName) { propertyName = it }
            Field(contactName, { contactName = it }, "Buyer / contact")
            Field(date, { date = it }, "Date (YYYY-MM-DD)")
            Field(time, { time = it }, "Time (HH:MM)")
            Field(notes, { notes = it }, "Notes", minLines = 3)
            Button(
                onClick = {
                    onSave(ViewingItem(
                        id = System.currentTimeMillis().toString(),
                        propertyName = propertyName.trim(),
                        contactName = contactName.trim(),
                        date = date.trim(),
                        time = time.trim(),
                        notes = notes.trim(),
                        status = "Scheduled"
                    ))
                },
                enabled = propertyName.isNotBlank() && date.isNotBlank() && time.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Icon(Icons.Default.EventAvailable, null)
                Spacer(Modifier.width(8.dp))
                Text("Schedule viewing")
            }
        }
    }
}

@Composable
private fun PropertyDetailScreen(
    property: PropertyItem,
    onBack: () -> Unit,
    onStatus: (String) -> Unit,
    onDelete: () -> Unit,
    onMarketing: () -> Unit,
    onSellerReport: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    PageScaffold(property.title, property.suburb, onBack) {
        Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                StatusPill(property.status, true)
                Text(money(property.price), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 12.dp))
                Text(property.bedrooms.toString() + " bed • " + property.bathrooms + " bath • " + property.propertyType, color = Color.White.copy(alpha = .78f))
            }
        }
        SectionTitle("Property")
        ReviewLine("Status", property.status)
        if (property.address.isNotBlank()) ReviewLine("Address", property.address)
        ReviewLine("Area", listOf(property.suburb, property.city, property.province, property.postalCode).filter { it.isNotBlank() }.joinToString(", ").ifBlank { property.suburb })
        if (property.latitude != 0.0 || property.longitude != 0.0) ReviewLine("GPS", "%.5f, %.5f".format(property.latitude, property.longitude))
        ReviewLine("Seller", property.sellerName.ifBlank { "Not added" })
        ReviewLine("Phone", property.sellerPhone.ifBlank { "Not added" })
        if (property.notes.isNotBlank()) ReviewLine("Notes", property.notes)
        if (property.address.isNotBlank() || property.latitude != 0.0 || property.longitude != 0.0) {
            OutlinedButton(
                onClick = { OpenStreetMapService.openMap(context, property) },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Icon(Icons.Default.Map, null)
                Spacer(Modifier.width(8.dp))
                Text("Open in OpenStreetMap")
            }
        }
        Row(Modifier.padding(top = 12.dp)) {
            Button(onClick = onMarketing, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(6.dp)); Text("Marketing")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onSellerReport, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.PictureAsPdf, null); Spacer(Modifier.width(6.dp)); Text("Report")
            }
        }
        Button(
            onClick = {
                runCatching {
                    val file = PdfExporter.propertyProfile(context, property)
                    PdfExporter.share(context, file, "Property profile - " + property.title)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, null)
            Spacer(Modifier.width(8.dp))
            Text("Export property PDF")
        }
        if (property.sellerPhone.isNotBlank()) {
            Button(
                onClick = { whatsapp(context, property.sellerPhone) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F8B4C))
            ) {
                Icon(Icons.Default.Chat, null); Spacer(Modifier.width(8.dp)); Text("WhatsApp seller")
            }
        }
        SectionTitle("Listing status")
        ChoiceRow(listOf("Active", "Draft", "Offer", "Sold"), property.status, onStatus)
        OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Icon(Icons.Default.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text("Delete property")
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete property?") },
            text = { Text("This removes the property from this device.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CommissionScreen(onBack: () -> Unit) {
    var price by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("5") }
    var addVat by rememberSaveable { mutableStateOf(true) }
    val p = price.toDoubleOrNull() ?: 0.0
    val r = rate.toDoubleOrNull() ?: 0.0
    val commission = p * r / 100
    val vat = if (addVat) commission * 0.15 else 0.0
    val total = commission + vat

    PageScaffold("Commission calculator", "Fast deal calculation", onBack) {
        Field(price, { price = it }, "Sale price", KeyboardType.Number)
        Field(rate, { rate = it }, "Commission %", KeyboardType.Decimal)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Switch(checked = addVat, onCheckedChange = { addVat = it })
            Text("Add 15% VAT to commission", Modifier.padding(start = 10.dp), color = Ink)
        }
        Card(colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(Modifier.padding(22.dp)) {
                Text("COMMISSION", color = Color.White.copy(alpha = .65f), fontWeight = FontWeight.Bold)
                Text(money(commission), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                if (addVat) Text("VAT " + money(vat), color = Color.White.copy(alpha = .75f))
                Text("Total " + money(total), color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun MarketingStudioScreen(properties: List<PropertyItem>, selectedPropertyId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    var selected by rememberSaveable { mutableStateOf(selectedPropertyId ?: properties.firstOrNull()?.id) }
    val property = properties.firstOrNull { it.id == selected }
    val copy = property?.let {
        it.title + ", " + it.suburb + "\n" +
            money(it.price) + "\n" +
            it.bedrooms + " bedrooms • " + it.bathrooms + " bathrooms • " + it.propertyType + "\n\n" +
            it.notes.ifBlank { "A well-positioned property ready for its next owner." } +
            "\n\nContact me to arrange a viewing."
    } ?: ""

    PageScaffold("Marketing Studio", "Create clean listing copy in seconds", onBack) {
        if (properties.isEmpty()) {
            EmptyCard("No properties yet", "Add a property before creating marketing copy.")
        } else {
            FormTitle("Choose property", "Tap a listing to generate its marketing text.")
            properties.take(6).forEach { item ->
                OutlinedButton(
                    onClick = { selected = item.id },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = if (selected == item.id) ButtonDefaults.outlinedButtonColors(containerColor = BrandSoft) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(item.title + " • " + item.suburb, modifier = Modifier.weight(1f))
                    if (selected == item.id) Icon(Icons.Default.Check, null)
                }
            }
            Card(Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Text(copy, Modifier.padding(18.dp), color = Ink)
            }
            Row(Modifier.padding(top = 12.dp)) {
                OutlinedButton(onClick = { copyText(context, copy) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(6.dp)); Text("Copy")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { shareText(context, "Property listing", copy) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("Share")
                }
            }
        }
    }
}

@Composable
private fun SellerReportScreen(properties: List<PropertyItem>, viewings: List<ViewingItem>, selectedPropertyId: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    var selected by rememberSaveable { mutableStateOf(selectedPropertyId ?: properties.firstOrNull()?.id) }
    val property = properties.firstOrNull { it.id == selected }

    PageScaffold("Seller report", "A clear property activity summary", onBack) {
        if (properties.isEmpty()) {
            EmptyCard("No properties yet", "Add a property before generating a seller summary.")
        } else {
            properties.take(6).forEach { item ->
                OutlinedButton(
                    onClick = { selected = item.id },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = if (selected == item.id) ButtonDefaults.outlinedButtonColors(containerColor = BrandSoft) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(item.title, modifier = Modifier.weight(1f))
                    if (selected == item.id) Icon(Icons.Default.Check, null)
                }
            }
            property?.let { p ->
                val related = viewings.filter { it.propertyName.equals(p.title, true) }
                val completed = related.count { it.status == "Completed" }
                val report = "SELLER PROPERTY UPDATE\n\nProperty: " + p.title +
                    "\nArea: " + p.suburb +
                    "\nAsking price: " + money(p.price) +
                    "\nStatus: " + p.status +
                    "\n\nViewings scheduled: " + related.size +
                    "\nViewings completed: " + completed +
                    "\n\nAgent note: " + p.notes.ifBlank { "No additional notes recorded." }

                Card(Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text(report, Modifier.padding(18.dp), color = Ink)
                }
                Row(Modifier.padding(top = 12.dp)) {
                    OutlinedButton(onClick = { shareText(context, "Seller report - " + p.title, report) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("Share text")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            runCatching {
                                val file = PdfExporter.sellerReport(context, p, related)
                                PdfExporter.share(context, file, "Seller report - " + p.title)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null); Spacer(Modifier.width(6.dp)); Text("PDF")
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineScreen(properties: List<PropertyItem>, onBack: () -> Unit) {
    PageScaffold("Deal pipeline", "Track every listing from draft to sold", onBack) {
        listOf("Draft", "Active", "Offer", "Sold").forEach { stage ->
            val items = properties.filter { it.status == stage }
            Text(stage.uppercase(), color = Muted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
            if (items.isEmpty()) {
                Text("No properties", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            } else {
                items.forEach {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(it.title, fontWeight = FontWeight.SemiBold, color = Ink)
                                Text(it.suburb, color = Muted, fontSize = 12.sp)
                            }
                            Text(money(it.price), color = Brand, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp)) {
        Text(
            "AGENT WORKSPACE",
            color = Brand,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp
        )
        Text(
            title,
            fontSize = 27.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(subtitle, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun PageScaffold(title: String, subtitle: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.padding(8.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Column {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), content = content)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EmptyScreen(message: String, onBack: () -> Unit) {
    PageScaffold("AgentKit", "", onBack) { EmptyCard(message, "Return and try again.") }
}

@Composable
private fun PropertyCard(item: PropertyItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(BrandSoft, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HomeWork, null, tint = Brand)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(item.title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
                    Text(item.suburb, color = Muted, fontSize = 12.sp)
                }
                StatusPill(item.status)
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Border)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(money(item.price), fontWeight = FontWeight.ExtraBold, color = Navy, fontSize = 19.sp, modifier = Modifier.weight(1f))
                Text(item.bedrooms.toString() + " bed • " + item.bathrooms + " bath", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ActionTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(BrandSoft, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Brand, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 15.sp)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Muted.copy(alpha = .65f))
        }
    }
}

@Composable
private fun CardBlock(label: String, value: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Navy),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).height(34.dp).background(Accent, RoundedCornerShape(4.dp)))
                Column(Modifier.padding(start = 12.dp)) {
                    Text(label, color = Color.White.copy(alpha = .62f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                    Text(value, color = Color.White, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Text(subtitle, color = Color.White.copy(alpha = .72f), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, start = 16.dp))
        }
    }
}

@Composable
private fun RowScope.Metric(number: String, label: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.weight(1f).padding(6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = Brand, modifier = Modifier.size(21.dp))
            Text(number, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Navy, modifier = Modifier.padding(top = 8.dp))
            Text(label, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        placeholder = { Text(placeholder, color = Muted) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Brand) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Brand,
            unfocusedBorderColor = Border,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun ChoiceRow(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { option ->
                    val active = option == selected
                    OutlinedButton(
                        onClick = { onSelected(option) },
                        modifier = Modifier.weight(1f).padding(3.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (active) BrandSoft else Color.Transparent)
                    ) {
                        if (active) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(option, maxLines = 1)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text, minLines: Int = 1) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        singleLine = minLines == 1,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun FormTitle(title: String, subtitle: String) {
    Text(title, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text(subtitle, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
private fun ReviewLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Text(label, color = Muted, modifier = Modifier.width(105.dp), fontSize = 13.sp)
        Text(value, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
    HorizontalDivider(color = Border)
}

@Composable
private fun SectionTitle(text: String) {
    Text(text.uppercase(), color = Muted, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 8.dp), fontSize = 10.sp, letterSpacing = 1.1.sp)
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Column(
        Modifier.padding(20.dp, 10.dp).fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inbox, null, tint = Brand, modifier = Modifier.size(36.dp))
        Text(title, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 8.dp))
        Text(subtitle, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun StatusPill(status: String, light: Boolean = false) {
    val bg = if (light) Color.White.copy(alpha = .14f) else BrandSoft
    val fg = if (light) Color.White else Brand
    Box(Modifier.background(bg, RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 5.dp)) {
        Text(status, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun money(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    format.maximumFractionDigits = 0
    return format.format(value)
}

private fun dial(context: Context, phone: String) {
    if (phone.isBlank()) return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phone))))
}

private fun whatsapp(context: Context, phone: String) {
    val digits = phone.filter { it.isDigit() }
    val normalized = when {
        digits.startsWith("0") -> "27" + digits.drop(1)
        digits.startsWith("27") -> digits
        else -> digits
    }
    if (normalized.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + normalized)))
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("AgentKit", text))
}

private fun shareText(context: Context, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share with"))
}
