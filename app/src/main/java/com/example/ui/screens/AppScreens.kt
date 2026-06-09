package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import com.example.repository.GovScheme
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.locale.*
import com.example.viewmodel.AppViewModel
import java.io.ByteArrayOutputStream

// --- Custom Frosted Glass Theme Helper Components ---
@Composable
fun FrostedGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0E1410) else Color(0xFFF3F7F2)
    val topOrbColor = if (isDark) Color(0x2E34D399) else Color(0x38A7F3D0) // Emerald light glow
    val bottomOrbColor = if (isDark) Color(0x1F059669) else Color(0x2EA6F4C5) // Mint/emerald-600 glow

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawBehind {
                // Top-Left Glowing Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(topOrbColor, Color.Transparent),
                        center = Offset(-80f, -80f),
                        radius = 350.dp.toPx()
                    ),
                    radius = 350.dp.toPx(),
                    center = Offset(-80f, -80f)
                )

                // Middle-Right/Bottom Glowing Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bottomOrbColor, Color.Transparent),
                        center = Offset(size.width + 100f, size.height * 0.55f),
                        radius = 450.dp.toPx()
                    ),
                    radius = 450.dp.toPx(),
                    center = Offset(size.width + 100f, size.height * 0.55f)
                )
            }
    ) {
        content()
    }
}

// --- Main Router ---
@Composable
fun KrushiMitraApp(viewModel: AppViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    CompositionLocalProvider(LocalLanguage provides currentLanguage) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = isLoggedIn,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "auth_routing"
            ) { logged ->
                if (logged) {
                    DashboardScreen(viewModel)
                } else {
                    AuthScreen(viewModel)
                }
            }
        }
    }
}

// --- OTP Authentication & Profile Setup Screen ---
@Composable
fun AuthScreen(viewModel: AppViewModel) {
    val currentLang = LocalLanguage.current
    val progress by viewModel.authProgress.collectAsState()
    val smsSent by viewModel.authSmsSent.collectAsState()
    val verifiedOtp by viewModel.verifiedOtp.collectAsState()

    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    
    // Profile Creation form states
    var fullName by remember { mutableStateOf("") }
    var villageName by remember { mutableStateOf("") }
    var districtName by remember { mutableStateOf("") }
    var landArea by remember { mutableStateOf("") }
    var sownCrops by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    FrostedGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Branding
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = getStr("app_name"),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = getStr("slogan"),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Language Selection Toggle (Visual layout, large touch targets)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferred Language / भाषा निवडा / भाषा चुनें",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.LightGray else Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val selected = lang == currentLang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .clickable { viewModel.toggleLanguage(lang) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lang.displayName,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            if (verifiedOtp.isEmpty()) {
                // Phone OTP Form
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getStr("login_title"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = getStr("login_subtitle"),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        if (!smsSent) {
                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = { if (it.length <= 10) mobileNumber = it },
                                label = { Text(getStr("enter_mobile")) },
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mobile_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    viewModel.sendLoginOtp(mobileNumber)
                                },
                                enabled = mobileNumber.length == 10 && !progress,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("otp_button")
                            ) {
                                if (progress) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(getStr("get_otp"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // OTP verification entry
                            Text(
                                text = "Simulated OTP Sent! Code is: ${viewModel.generatedOtpCode.value}",
                                color = Color(0xFFC62828),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6) otpCode = it },
                                label = { Text(getStr("enter_otp")) },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    viewModel.verifyEnteredOtp(otpCode, mobileNumber)
                                },
                                enabled = otpCode.length >= 6,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("verify_button")
                            ) {
                                Text(getStr("verify_otp"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Profile Setup Form
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = getStr("create_profile"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(getStr("full_name")) },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = villageName,
                            onValueChange = { villageName = it },
                            label = { Text(getStr("village")) },
                            leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = districtName,
                            onValueChange = { districtName = it },
                            label = { Text(getStr("district")) },
                            leadingIcon = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = landArea,
                            onValueChange = { landArea = it },
                            label = { Text(getStr("land_area")) },
                            leadingIcon = { Icon(Icons.Default.Landscape, null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = sownCrops,
                            onValueChange = { sownCrops = it },
                            label = { Text(getStr("primary_crops")) },
                            placeholder = { Text(getStr("primary_crops_hint")) },
                            leadingIcon = { Icon(Icons.Default.Eco, null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.createNewProfile(
                                    name = fullName.ifBlank { "Rohit Patil" },
                                    mobile = mobileNumber.ifBlank { "9876543210" },
                                    village = villageName.ifBlank { "Sillod" },
                                    district = districtName.ifBlank { "Aurangabad" },
                                    size = landArea.toDoubleOrNull() ?: 5.0,
                                    crops = sownCrops.ifBlank { "Cotton, Soyabean" }
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(top = 8.dp)
                        ) {
                            Text(getStr("save_profile"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// --- Scaffold Navigation & Dashboard Screen ---
@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    var activeTab by remember { mutableStateOf("home") }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isDark) Color(0x1F99EEEE) else Color(0x26000000)
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                // Tab Home
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(if (activeTab == "home") Icons.Filled.Home else Icons.Outlined.Home, null) },
                    label = { Text(getStr("home"), overflow = TextOverflow.Ellipsis, maxLines = 1) }
                )
                // Tab Assistant
                NavigationBarItem(
                    selected = activeTab == "assistant",
                    onClick = { activeTab = "assistant" },
                    icon = { Icon(if (activeTab == "assistant") Icons.Filled.Chat else Icons.Outlined.Chat, null) },
                    label = { Text(getStr("assistant"), overflow = TextOverflow.Ellipsis, maxLines = 1) }
                )
                // Tab Weather
                NavigationBarItem(
                    selected = activeTab == "weather",
                    onClick = { activeTab = "weather" },
                    icon = { Icon(if (activeTab == "weather") Icons.Filled.Cloud else Icons.Outlined.Cloud, null) },
                    label = { Text(getStr("weather"), overflow = TextOverflow.Ellipsis, maxLines = 1) }
                )
                // Tab Mandi
                NavigationBarItem(
                    selected = activeTab == "mandi",
                    onClick = { activeTab = "mandi" },
                    icon = { Icon(if (activeTab == "mandi") Icons.Filled.CurrencyRupee else Icons.Outlined.CurrencyRupee, null) },
                    label = { Text("Mandi", overflow = TextOverflow.Ellipsis, maxLines = 1) }
                )
                // Tab Schemes
                NavigationBarItem(
                    selected = activeTab == "schemes",
                    onClick = { activeTab = "schemes" },
                    icon = { Icon(if (activeTab == "schemes") Icons.Filled.AssignmentTurnedIn else Icons.Outlined.AssignmentTurnedIn, null) },
                    label = { Text(getStr("schemes"), overflow = TextOverflow.Ellipsis, maxLines = 1) }
                )
                // Tab Community
                NavigationBarItem(
                    selected = activeTab == "community",
                    onClick = { activeTab = "community" },
                    icon = { Icon(if (activeTab == "community") Icons.Filled.Groups else Icons.Outlined.Groups, null) },
                    label = { Text("Group", overflow = TextOverflow.Ellipsis, maxLines = 1) }
                )
                // Tab Profile
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { activeTab = "profile" },
                    icon = { Icon(if (activeTab == "profile") Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle, null) },
                    label = { Text(getStr("profile"), overflow = TextOverflow.Ellipsis, maxLines = 1) }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        FrostedGlassBackground {
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width / 3 } + fadeOut()
                    },
                    label = "tab_routing"
                ) { tab ->
                    when (tab) {
                        "home" -> HomeScreen(viewModel, onScanClick = { activeTab = "disease_scan" }, onHelpClick = { activeTab = "expert" })
                        "assistant" -> AssistantScreen(viewModel)
                        "weather" -> WeatherScreen(viewModel)
                        "mandi" -> MandiScreen(viewModel)
                        "schemes" -> SchemesScreen(viewModel)
                        "community" -> CommunityScreen(viewModel)
                        "profile" -> ProfileScreen(viewModel)
                        "disease_scan" -> DiseaseScreen(viewModel, onBack = { activeTab = "home" })
                        "expert" -> EmergencySupportScreen(onBack = { activeTab = "home" })
                    }
                }
            }
        }
    }
}

// --- Home Hub Screen ---
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onScanClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val financials by viewModel.financialsList.collectAsState()
    val crops by viewModel.cropsList.collectAsState()
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    // Calculate dynamic financial balances
    val incomeSum = financials.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expenseSum = financials.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = incomeSum - expenseSum

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card with custom in-app banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x40FFFFFF) else Color(0x80FFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "नमस्कार , ${profile?.fullName ?: "Farmer"}!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${profile?.village ?: "Sillod"}, ${profile?.district ?: "Aurangabad"}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudQueue, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Banner alert (daily advice/weather)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFEB3B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (LocalLanguage.current == AppLanguage.MARATHI) {
                                "आजचा कृषी सल्ला: आभाळ ढगाळ असल्याने कापूस पिकावर कीड पडण्याची शक्यता आहे. आज रासायनिक औषध फवारणी टाळा."
                            } else {
                                "Agri Tip: Cloudy weather expected. High chance of whitefly pests in cotton. Postpone liquid sprays for 24 hrs."
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Feature Large Buttons (Feature 3 & 10)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action 1: Scan
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color(0x1F34D399) else Color(0x33A7F3D0))
                    .border(1.2.dp, if (isDark) Color(0x3334D399) else Color(0x8034D399), RoundedCornerShape(24.dp))
                    .clickable { onScanClick() }
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = getStr("disease_scan"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action 2: Help Center
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color(0x1FEE9B2E) else Color(0x22F59E0B))
                    .border(1.2.dp, if (isDark) Color(0x33EE9B2E) else Color(0x66F59E0B), RoundedCornerShape(24.dp))
                    .clickable { onHelpClick() }
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isDark) Color(0xFFD97706) else Color(0xFFEE7D00), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SupportAgent, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = getStr("helplines"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFFEE7D00)
                    )
                }
            }
        }

        // Live Mandi Quick Widget
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x2AFFFFFF) else Color(0x99FFFFFF)),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = getStr("nearby_mandis"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Live 🟢",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Sample rates in home page
                val quickMandis = listOf(
                    MandiItem("1", "Soyabean", "Akola APMC", "Akola", 4100.0, 4650.0, 4420.0, 4390.0, true),
                    MandiItem("3", "Cotton", "Wardha APMC", "Wardha", 6700.0, 7400.0, 7120.0, 6980.0, true)
                )
                quickMandis.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${item.cropName} (${item.mandiName})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Min: ₹${item.minPrice.toInt()} - Max: ₹${item.maxPrice.toInt()}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${item.modelPrice.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = if (isDark) Color(0x1F999999) else Color(0x1F000000))
                }
            }
        }

        // Local Farm management Overview
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x2AFFFFFF) else Color(0x99FFFFFF)),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = getStr("crop_health"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Cash balance indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = getStr("cash_balance"), fontSize = 12.sp, color = if (isDark) Color.LightGray else Color.Gray)
                        Text(text = "₹$balance", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (balance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFC62828))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(text = "In: ₹${incomeSum.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(text = "Out: ₹${expenseSum.toInt()}", fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Crops list cycles
                Text(text = getStr("active_crops"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.LightGray else Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                if (crops.isEmpty()) {
                    Text(
                        text = "No active crops registered. Visit profile to add crop cycles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    crops.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Eco, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = record.cropName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = "Sown: ${record.sowingDate}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- Feature 2: Conversational Bot Screen (Chat) ---
@Composable
fun AssistantScreen(viewModel: AppViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val loading by viewModel.chatLoading.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isDark = isSystemInDarkTheme()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App top header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x66FFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Android, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(getStr("assistant"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("KrushiMitra Gemini AI 🟢 Online", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // Quick suggestions row
        val suggestionPrompts = if (LocalLanguage.current == AppLanguage.MARATHI) {
            listOf(
                "कापूस पिकावरील बोंड अळी नियंत्रण सांगा",
                "सोयाबीन पिकासाठी खत वेळापत्रक",
                "कांदा पोखरणारी अळी उपाय"
            )
        } else if (LocalLanguage.current == AppLanguage.HINDI) {
            listOf(
                "कपास फसल के लिए रोग नियंत्रण",
                "गेंहू की बुवाई के समय उत्तम खाद",
                "सोयाबीन का कीट बचाओ"
            )
        } else {
            listOf(
                "Organic fertilizers for cotton",
                "Soybean disease control plan",
                "PM Kisan scheme registration"
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestionPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.submitAgentQuery(prompt) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = prompt, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Conversation history lists
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                val cardBg = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val borderStroke = if (msg.isUser) null else BorderStroke(1.2.dp, if (isDark) Color(0x26FFFFFF) else Color(0x80FFFFFF))
                val textColor = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                val corners = if (msg.isUser) {
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 290.dp)
                            .clip(corners)
                            .background(cardBg)
                            .then(if (borderStroke != null) Modifier.border(borderStroke, corners) else Modifier)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = msg.text,
                            fontSize = 15.sp,
                            color = textColor,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            if (loading) {
                item {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("कृषिमित्र विचार करत आहे...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Message input bar with send button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .windowInsetsPadding(WindowInsets.ime),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text(getStr("crop_advice_placeholder"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x4D000000),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            FloatingActionButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        viewModel.submitAgentQuery(inputQuery)
                        inputQuery = ""
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null)
            }
        }
    }
}

// --- Feature 3: Crop Disease leaf scan simulation ---
@Composable
fun DiseaseScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val report by viewModel.leafReport.collectAsState()
    val loading by viewModel.leafScanLoading.collectAsState()
    val selectedBase64 by viewModel.selectedBitmapBase64.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(text = getStr("disease_scan"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // Info prompt to guide the farmer
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = getStr("disease_scan_desc"),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Simulated High-Quality Crop Leaf template selectors
        Text(text = "Try with sample diseased crop leaf templates:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.LightGray else Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val templates = listOf(
                "Soybean Rust" to "rust",
                "Cotton Blight" to "blight",
                "Wheat stem rust" to "wheat"
            )
            templates.forEach { (label, code) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable {
                            // Generate dummy base64 string
                            val dummyBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
                            viewModel.triggerImageScan(dummyBase64)
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }
            }
        }

        // Image Preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .border(1.2.dp, if (isDark) Color(0x33FFFFFF) else Color(0x7F000000), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedBase64 != null) {
                // Preloaded image icon overlay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Eco, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Text("Leaf Image Selected ✔", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("Select a leaf template above to launch AI analysis", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        // Diagnostics Outputs
        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("कृषिमित्र बुरशी व रोग शोधत आहे...", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        } else if (report != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF)),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Gemini Diagnosed Report 🔬",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = report!!,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("disease_report")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { viewModel.clearLeafScan() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Scan Another Leaf", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Feature 4: Climate Weather Screen ---
@Composable
fun WeatherScreen(viewModel: AppViewModel) {
    val weather = remember { viewModel.fetchWeatherDetails() }
    val profile by viewModel.userProfile.collectAsState()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = getStr("weather_title"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Large status card representing current climate
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x40FFFFFF) else Color(0x80FFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = profile?.district ?: "Aurangabad", fontSize = 18.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (weather.condition.contains("Rain", true)) Icons.Filled.WbCloudy else Icons.Filled.WbSunny,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "${weather.temp}°C", fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = weather.condition, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Medium)

                if (weather.rainAlert != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFC62828).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = weather.rainAlert, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Detailed climatic gauges (Humidity, Wind speed, Sowing comfort)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val gaugeBorder = BorderStroke(1.2.dp, if (isDark) Color(0x24FFFFFF) else Color(0x66FFFFFF))
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                border = gaugeBorder,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF29B6F6))
                    Text(text = getStr("humidity"), fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    Text(text = "${weather.humidity}%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                border = gaugeBorder,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Air, null, tint = if (isDark) Color(0xFF81C784) else Color(0xFF455A64))
                    Text(text = getStr("wind_speed"), fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    Text(text = "${weather.windSpeed} km/h", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                border = gaugeBorder,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Umbrella, null, tint = Color(0xFF7E57C2))
                    Text(text = getStr("precipitation"), fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    Text(text = "${weather.rainProbability}%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 7-day forecast lists
        Text(text = getStr("forecast_7day"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x28FFFFFF) else Color(0x90FFFFFF)),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                weather.dailyForecast.forEach { day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = day.dayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(62.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = when (day.condition) {
                                    "Heavy Rain" -> Icons.Default.Thunderstorm
                                    "Light Showers" -> Icons.Default.Grain
                                    "Partly Cloudy" -> Icons.Default.Cloud
                                    else -> Icons.Default.WbSunny
                                },
                                contentDescription = null,
                                tint = if (day.condition.contains("Rain") || day.condition.contains("Showers")) Color(0xFF29B6F6) else Color(0xFFFFB300),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = day.condition, fontSize = 13.sp, color = if (isDark) Color.LightGray else Color.DarkGray)
                        }
                        Text(text = "${day.tempMax}° / ${day.tempMin}°", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    }
                    HorizontalDivider(color = if (isDark) Color(0x15FFFFFF) else Color(0x15000000))
                }
            }
        }
    }
}

// --- Feature 5: Mandi Rates Market Pricing ---
@Composable
fun MandiScreen(viewModel: AppViewModel) {
    val search by viewModel.mandiSearchQuery.collectAsState()
    val cropType by viewModel.selectedCropType.collectAsState()
    val mandiItems = viewModel.fetchMandiPrices()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = getStr("mandi_prices"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Search text field
        OutlinedTextField(
            value = search,
            onValueChange = { viewModel.mandiSearchQuery.value = it },
            placeholder = { Text(getStr("mandi_search")) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x4D000000),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Selectable filter chips for crop
        val cropsFilter = listOf(
            "All" to "AL",
            "Soyabean" to "Soyabean",
            "Cotton" to "Cotton",
            "Onion" to "Onion",
            "Wheat" to "Wheat",
            "Rice" to "Rice"
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(cropsFilter) { (label, code) ->
                val active = cropType == code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .then(if (!active) Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp)) else Modifier)
                        .clickable { viewModel.selectedCropType.value = code }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = label, color = if (active) Color.White else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Prices list
        if (mandiItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No markets matching criteria", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mandiItems) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0x28FFFFFF) else Color(0x90FFFFFF)),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = item.cropName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "${item.mandiName} (Dist. ${item.district})", fontSize = 13.sp, color = if (isDark) Color.LightGray else Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Avg: ₹${item.modelPrice.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = if (item.trendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (item.trendUp) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = getStr("min_max") + ":", fontSize = 12.sp, color = if (isDark) Color.LightGray else Color.Gray)
                                Text(text = "₹${item.minPrice.toInt()} - ₹${item.maxPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Dynamic Trend sparkline visual (Canvas)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = getStr("trend_7day"), fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            val trendColor = if (item.trendUp) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                            ) {
                                val points = if (item.trendUp) listOf(0.9f, 0.7f, 0.8f, 0.6f, 0.5f, 0.3f, 0.1f) else listOf(0.1f, 0.3f, 0.2f, 0.4f, 0.5f, 0.7f, 0.9f)
                                val path = Path()
                                val stepX = size.width / (points.size - 1)
                                points.forEachIndexed { idx, yRatio ->
                                    val x = idx * stepX
                                    val y = yRatio * size.height
                                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(
                                    path = path,
                                    color = trendColor,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Feature 6: Welfare Government Schemes Screen ---
@Composable
fun SchemesScreen(viewModel: AppViewModel) {
    val schemes = viewModel.fetchAvailableSchemes()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = getStr("schemes_title"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Schemes list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(schemes) { scheme ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, if (isDark) Color(0x24FFFFFF) else Color(0x85FFFFFF)),
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = scheme.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = scheme.desc, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = getStr("eligibility_checker") + ":", fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = scheme.eligibility, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = getStr("apply_guide") + ":", fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray, fontWeight = FontWeight.Bold)
                        Text(text = scheme.applyGuide, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// --- Feature 9: Community discussion board ---
@Composable
fun CommunityScreen(viewModel: AppViewModel) {
    val posts by viewModel.communityPosts.collectAsState()
    var commentInput by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = getStr("community_board"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Create new post form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x24FFFFFF) else Color(0x66FFFFFF)),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    placeholder = { Text(getStr("post_hint")) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x4D000000),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Button(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            viewModel.postCommunityQuestion(commentInput)
                            commentInput = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(getStr("post_btn"), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Active discussion list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(posts) { post ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, if (isDark) Color(0x28FFFFFF) else Color(0x90FFFFFF)),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = post.authorName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = post.authorVillage, fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = post.questionText, fontSize = 15.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { /* Simulate Like */ }) {
                                Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${post.likesCount} ${getStr("likes")}", fontSize = 12.sp, color = if (isDark) Color.LightGray else Color.Gray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Comment, null, tint = if (isDark) Color.LightGray else Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${post.commentsCount} ${getStr("comments")}", fontSize = 12.sp, color = if (isDark) Color.LightGray else Color.Gray)
                            }
                        }

                        // Simulated comments expansion
                        if (post.commentsCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (post.id == 1L) {
                                        Text(text = "■ Vijay Shinde: Apply Neem Cake mixed with Vermicompost. It gives amazing results!", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "■ Suresh Kale: Azotobacter liquid fertilizer also works very well around this time.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        Text(text = "■ Expert Krishi Mitra: Spray Acetamiprid 20% SP at 80g per acre to control the whiteflies that spread this virus.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "■ Vikas Jadhav: Set up blue and yellow sticky traps everywhere in your borders immediately!", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Feature 8: Profile, Crop Organizer, & Financial Logs Screen ---
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val financials by viewModel.financialsList.collectAsState()
    val crops by viewModel.cropsList.collectAsState()
    val isDark = isSystemInDarkTheme()

    var showFinancialDialog by remember { mutableStateOf(false) }
    var showCropDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var cropNameText by remember { mutableStateOf("") }
    var predictedSowDate by remember { mutableStateOf("") }
    var predictedYieldAmt by remember { mutableStateOf("") }

    var logAmountText by remember { mutableStateOf("") }
    var logCategoryText by remember { mutableStateOf("") }
    var logTypeText by remember { mutableStateOf("INCOME") }
    var logDescText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = getStr("profile"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Profile details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x28FFFFFF) else Color(0x90FFFFFF)),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = profile?.fullName?.take(1) ?: "K", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = profile?.fullName ?: "Farmer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = profile?.mobileNumber ?: "No number", fontSize = 13.sp, color = if (isDark) Color.LightGray else Color.Gray)
                    }
                }
                HorizontalDivider(color = if (isDark) Color(0x15FFFFFF) else Color(0x15000000))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = getStr("village") + ":", color = if (isDark) Color.LightGray else Color.Gray, fontSize = 14.sp)
                    Text(text = profile?.village ?: "Not set", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = getStr("district") + ":", color = if (isDark) Color.LightGray else Color.Gray, fontSize = 14.sp)
                    Text(text = profile?.district ?: "Not set", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = getStr("land_area") + ":", color = if (isDark) Color.LightGray else Color.Gray, fontSize = 14.sp)
                    Text(text = "${profile?.landArea ?: 0.0} Acres", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = getStr("primary_crops") + ":", color = if (isDark) Color.LightGray else Color.Gray, fontSize = 14.sp)
                    Text(text = profile?.primaryCrops ?: "Not set", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Logout button
                Button(
                    onClick = { viewModel.logoutFarmer() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Log Out", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Crop cycle section with Add Floating-like layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Active Crop Cycles", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(
                onClick = { showCropDialog = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (crops.isEmpty()) {
            Text(text = "No recorded crop cycles. Click '+' to add fields.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        } else {
            crops.forEach { record ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, if (isDark) Color(0x24FFFFFF) else Color(0x80FFFFFF)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = record.cropName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Sowing Date: ${record.sowingDate}", fontSize = 12.sp, color = if (isDark) Color.LightGray else Color.Gray)
                            Text(text = "Expected Harvest: ${record.expectedHarvest} Quintals", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { viewModel.deleteCropCycle(record) }) {
                            Icon(Icons.Default.Delete, null, tint = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }

        // Financial Tracker section with Add Layout
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Income & Expense Records", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(
                onClick = { showFinancialDialog = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (financials.isEmpty()) {
            Text(text = "No transactions logged. Click '+' to track profits.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        } else {
            financials.forEach { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, if (isDark) Color(0x24FFFFFF) else Color(0x80FFFFFF)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = log.category, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = log.description, fontSize = 12.sp, color = if (isDark) Color.LightGray else Color.Gray)
                            Text(text = log.date, fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            text = "${if (log.type == "INCOME") "+" else "-"} ₹${log.amount.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.type == "INCOME") MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- dialog modals for submissions ---
        if (showCropDialog) {
            AlertDialog(
                onDismissRequest = { showCropDialog = false },
                title = { Text("Log New Crop Sown") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cropNameText,
                            onValueChange = { cropNameText = it },
                            label = { Text("Crop Name (e.g. Cotton)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = predictedSowDate,
                            onValueChange = { predictedSowDate = it },
                            label = { Text("Sowing Date (YYYY-MM-DD)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = predictedYieldAmt,
                            onValueChange = { predictedYieldAmt = it },
                            label = { Text("Expected Yield (Quintals)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addCropCycle(
                                cropNameText.ifBlank { "Cotton" },
                                predictedSowDate.ifBlank { "2026-06-01" },
                                predictedYieldAmt.toDoubleOrNull() ?: 12.0
                            )
                            showCropDialog = false
                            cropNameText = ""
                            predictedSowDate = ""
                            predictedYieldAmt = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCropDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
                }
            )
        }

        if (showFinancialDialog) {
            AlertDialog(
                onDismissRequest = { showFinancialDialog = false },
                title = { Text("Log New Transaction") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = logCategoryText,
                            onValueChange = { logCategoryText = it },
                            label = { Text("Category (e.g., Seeds, Profit)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = logAmountText,
                            onValueChange = { logAmountText = it },
                            label = { Text("Amount (₹)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = logDescText,
                            onValueChange = { logDescText = it },
                            label = { Text("Description") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = logTypeText == "INCOME", onClick = { logTypeText = "INCOME" })
                                Text("Income")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = logTypeText == "EXPENSE", onClick = { logTypeText = "EXPENSE" })
                                Text("Expense")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addFinancialLog(
                                category = logCategoryText.ifBlank { "Seeds" },
                                amt = logAmountText.toDoubleOrNull() ?: 5000.0,
                                type = logTypeText,
                                description = logDescText.ifBlank { "Bought Bt Cotton seeds" }
                            )
                            showFinancialDialog = false
                            logCategoryText = ""
                            logAmountText = ""
                            logDescText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Log", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinancialDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
                }
            )
        }
    }
}

// --- Feature 10: Farmer Emergency Helplines & expert consultations ---
@Composable
fun EmergencySupportScreen(onBack: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFD32F2F))
            }
            Text(text = getStr("emergency"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
        }

        // Alert Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.12f)),
            border = BorderStroke(1.2.dp, Color(0xFFD32F2F).copy(alpha = 0.35f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReportProblem, null, tint = Color(0xFFD32F2F))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "If experiencing widespread pest outbreak or sudden crop failure, request an immediate physical inspection from the extension office.",
                    fontSize = 13.sp,
                    color = if (isDark) Color(0xFFEF4444) else Color(0xFF991B1B),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Helplines list
        Text(text = getStr("toll_free"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.LightGray else Color.Gray)
        val helplineContacts = listOf(
            "Kisan Call Center (KCC)" to "1800-180-1551",
            "Maharashtra Agri Department" to "1800-233-4000",
            "Crop Insurance (PMFBY)" to "1800-200-5142"
        )
        helplineContacts.forEach { (name, num) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, if (isDark) Color(0x24FFFFFF) else Color(0x80FFFFFF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = num, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                            .clickable { /* Simulate phone dial call */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Request Expert Call Back Form
        Text(text = getStr("expert_call"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.LightGray else Color.Gray)
        var selectCropOption by remember { mutableStateOf("") }
        var problemDescriptionText by remember { mutableStateOf("") }
        var successMessage by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, if (isDark) Color(0x28FFFFFF) else Color(0x90FFFFFF)),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!successMessage) {
                    OutlinedTextField(
                        value = selectCropOption,
                        onValueChange = { selectCropOption = it },
                        label = { Text("Crop Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x4D000000),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = problemDescriptionText,
                        onValueChange = { problemDescriptionText = it },
                        label = { Text("Explain problem in brief (e.g. Leaf turning yellow)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x4D000000),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Button(
                        onClick = { successMessage = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Request", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Request Submitted Successfully!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "An Agricultural Extension Officer will call you back within 24 hours.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
