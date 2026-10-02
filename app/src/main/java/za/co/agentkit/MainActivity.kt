package za.co.agentkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink=Color(0xFF102A43); private val Brand=Color(0xFF0A7A67); private val Bg=Color(0xFFF6F8FA); private val Muted=Color(0xFF64748B)
data class Page(val title:String,val subtitle:String,val icon:ImageVector)

class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MaterialTheme(colorScheme=lightColorScheme(primary=Brand,background=Bg,surface=Color.White)){AgentKit()}}}
}

@Composable fun AgentKit(){
 var tab by remember{mutableIntStateOf(0)}; var page by remember{mutableStateOf<Page?>(null)}
 val tabs=listOf(Page("Home","Your business at a glance",Icons.Default.Home),Page("Properties","Manage your portfolio",Icons.Default.Apartment),Page("Leads","Buyers, sellers & follow-ups",Icons.Default.Groups),Page("Calendar","Viewings, tasks & reminders",Icons.Default.CalendarMonth),Page("More","Everything else",Icons.Default.Menu))
 Scaffold(containerColor=Bg,bottomBar={NavigationBar(containerColor=Color.White){tabs.forEachIndexed{i,p->NavigationBarItem(selected=tab==i&&page==null,onClick={tab=i;page=null},icon={Icon(p.icon,null)},label={Text(p.title)})}}},floatingActionButton={if(page==null) FloatingActionButton(onClick={page=Page("Quick add","Create something new",Icons.Default.Add)},containerColor=Brand,contentColor=Color.White){Icon(Icons.Default.Add,null)}}}){pad->
  Box(Modifier.padding(pad)){if(page!=null) Detail(page!!){page=null}else when(tab){0->Home{page=it};1->Properties{page=it};2->Leads{page=it};3->Calendar{page=it};else->More{page=it}}}
 }
}
@Composable fun Header(title:String,sub:String){Column(Modifier.padding(20.dp,22.dp,20.dp,12.dp)){Text(title,fontSize=30.sp,fontWeight=FontWeight.Bold,color=Ink);Text(sub,color=Muted,fontSize=14.sp)}}
@Composable fun Home(open:(Page)->Unit){Column(Modifier.verticalScroll(rememberScrollState())){Header("Good morning","Your property business, simplified");CardBlock("PIPELINE","R 8.45m","12 active properties"){open(Page("Pipeline","Track every deal from lead to sold",Icons.Default.TrendingUp))};Text("TODAY",Modifier.padding(20.dp,18.dp,20.dp,8.dp),fontWeight=FontWeight.Bold,color=Muted);Row(Modifier.padding(horizontal=14.dp)){Mini("4","Viewings",Icons.Default.Visibility){open(Page("Viewings","Manage upcoming property viewings",Icons.Default.Visibility))};Mini("7","Follow-ups",Icons.Default.Notifications){open(Page("Follow-ups","Never lose a warm lead",Icons.Default.Notifications))}};Section("Quick actions",listOf(Page("Add property","Create a new property listing",Icons.Default.AddHome),Page("Add lead","Capture a buyer or seller",Icons.Default.PersonAdd),Page("Marketing Studio","Create branded property marketing",Icons.Default.AutoAwesome),Page("Seller report","Generate a professional owner update",Icons.Default.PictureAsPdf)),open)}}
@Composable fun Properties(open:(Page)->Unit){Column{Header("Properties","12 active • 3 drafts");Search();Section("Portfolio",listOf(Page("Active listings","Properties currently on the market",Icons.Default.HomeWork),Page("Drafts","Finish incomplete listings",Icons.Default.EditNote),Page("On show","Upcoming show properties",Icons.Default.Event),Page("Offers","Track offers and negotiations",Icons.Default.Handshake),Page("Sold & archived","Your completed business",Icons.Default.TaskAlt),Page("Add property","Basics → Location → Features → Photos → Seller → Mandate",Icons.Default.AddHome)),open)}}
@Composable fun Leads(open:(Page)->Unit){Column{Header("Leads & CRM","Keep every relationship moving");Search();Section("Contacts",listOf(Page("Hot leads","People requiring attention now",Icons.Default.LocalFireDepartment),Page("Buyers","Requirements, budgets and matches",Icons.Default.PersonSearch),Page("Sellers","Owners, mandates and communication",Icons.Default.RealEstateAgent),Page("Buyer matcher","Match requirements to your listings",Icons.Default.CompareArrows),Page("Follow-ups","Calls, WhatsApps and next actions",Icons.Default.PhoneInTalk),Page("Add contact","Capture a new buyer or seller",Icons.Default.PersonAdd)),open)}}
@Composable fun Calendar(open:(Page)->Unit){Column{Header("Calendar","Friday, 2 October");Section("Schedule",listOf(Page("Today's viewings","4 appointments scheduled",Icons.Default.Visibility),Page("Tasks","7 actions to complete",Icons.Default.CheckCircle),Page("Reminders","Follow-ups and mandate dates",Icons.Default.Alarm),Page("Schedule viewing","Property → Buyer → Date & time",Icons.Default.AddCircle),Page("Viewing feedback","Interested • Maybe • Not interested",Icons.Default.RateReview)),open)}}
@Composable fun More(open:(Page)->Unit){Column(Modifier.verticalScroll(rememberScrollState())){Header("AgentKit SA","Your professional toolkit");Section("Business",listOf(Page("Marketing Studio","Social posts, story cards and listing copy",Icons.Default.AutoAwesome),Page("Documents","Mandates, IDs and property documents",Icons.Default.Folder),Page("Seller Reports","Enquiries, viewings, feedback and offers",Icons.Default.PictureAsPdf),Page("Deal pipeline","Lead → Mandate → Offer → Sold",Icons.Default.AccountTree),Page("Commission calculator","Estimate commission and earnings",Icons.Default.Calculate),Page("Expenses & mileage","Record business costs and travel",Icons.Default.ReceiptLong),Page("Agent branding","Logo, profile, agency and contact details",Icons.Default.Palette),Page("Settings","Security, backup, export and preferences",Icons.Default.Settings)),open)}}
@Composable fun Detail(p:Page,back:()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){IconButton(back){Icon(Icons.Default.ArrowBack,null)};Text(p.title,fontSize=22.sp,fontWeight=FontWeight.Bold,color=Ink)};Box(Modifier.padding(20.dp).fillMaxWidth().background(Color.White,RoundedCornerShape(24.dp)).padding(24.dp)){Column{Icon(p.icon,null,tint=Brand,modifier=Modifier.size(42.dp));Spacer(Modifier.height(16.dp));Text(p.title,fontSize=26.sp,fontWeight=FontWeight.Bold,color=Ink);Text(p.subtitle,Modifier.padding(top=6.dp),color=Muted);Spacer(Modifier.height(22.dp));Text("UI WORKSPACE",fontWeight=FontWeight.Bold,color=Brand);Text("This page is linked and ready for its detailed workflow, fields and actions.",Modifier.padding(top=8.dp),color=Muted)}}}}
@Composable fun Section(title:String,items:List<Page>,open:(Page)->Unit){Column(Modifier.padding(14.dp)){Text(title.uppercase(),Modifier.padding(6.dp,12.dp),fontWeight=FontWeight.Bold,color=Muted);items.forEach{p->Row(Modifier.fillMaxWidth().padding(vertical=4.dp).background(Color.White,RoundedCornerShape(18.dp)).clickable{open(p)}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(Brand.copy(.10f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(p.icon,null,tint=Brand)};Column(Modifier.weight(1f).padding(horizontal=14.dp)){Text(p.title,fontWeight=FontWeight.SemiBold,color=Ink);Text(p.subtitle,fontSize=12.sp,color=Muted)};Icon(Icons.Default.ChevronRight,null,tint=Muted)}}}}
@Composable fun CardBlock(label:String,value:String,sub:String,on:()->Unit){Column(Modifier.padding(horizontal=20.dp).fillMaxWidth().background(Ink,RoundedCornerShape(24.dp)).clickable(onClick=on).padding(22.dp)){Text(label,color=Color.White.copy(.65f),fontWeight=FontWeight.Bold);Text(value,color=Color.White,fontSize=32.sp,fontWeight=FontWeight.Bold);Text(sub,color=Color.White.copy(.8f))}}
@Composable fun RowScope.Mini(n:String,label:String,icon:ImageVector,on:()->Unit){Column(Modifier.weight(1f).padding(6.dp).background(Color.White,RoundedCornerShape(20.dp)).clickable(onClick=on).padding(18.dp)){Icon(icon,null,tint=Brand);Text(n,fontSize=26.sp,fontWeight=FontWeight.Bold,color=Ink);Text(label,color=Muted)}}
@Composable fun Search(){OutlinedTextField("",{},Modifier.fillMaxWidth().padding(horizontal=20.dp),placeholder={Text("Search")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,shape=RoundedCornerShape(16.dp))}
