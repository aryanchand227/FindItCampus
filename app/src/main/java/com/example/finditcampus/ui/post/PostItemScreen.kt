package com.example.finditcampus.ui.post
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.finditcampus.theme.*

val MintBackground = Color(0xFFF2F8F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItemScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedItemImageUri by remember { mutableStateOf<String?>(null) }
    var analyzedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var isPublishing by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    
    // Form State
    var reportType by remember { mutableStateOf("") } // "lost" or "found"
    var location by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MintBackground,
            topBar = {
            Column(modifier = Modifier.background(MintBackground)) {
                CenterAlignedTopAppBar(
                    title = { Text("Report an item", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDark)
                        }
                    },
                    actions = {
                        Text(
                            text = "0$currentStep/04",
                            color = TextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MintBackground
                    )
                )
                // Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= currentStep) PrimaryOrange else SurfaceBeige)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (currentStep > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = borderStroke(PrimaryOrange) // Fallback border
                    ) {
                        Text("<- Back", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (currentStep < 4) {
                                currentStep++
                            } else {
                                // Publish
                                isPublishing = true
                                scope.launch {
                                    try {
                                        var finalImageUrl = selectedItemImageUri
                                        if (selectedItemImageUri != null && selectedItemImageUri!!.startsWith("content://")) {
                                            
val uri = android.net.Uri.parse(selectedItemImageUri)
                                            val uploadedUrl = com.example.finditcampus.utils.ImageUploader.uploadToCatbox(context, uri)
                                            if (uploadedUrl != null) {
                                                finalImageUrl = uploadedUrl
                                            }
                                        }

                                        val newItem = com.example.finditcampus.data.MockItem(
                                            id = java.util.UUID.randomUUID().toString(),
                                            title = title.ifEmpty { "New Item" },
                                            description = description,
                                            location = location.ifEmpty { "Campus" },
                                            time = "Just now",
                                            category = itemType.ifEmpty { "Others" },
                                            type = reportType,
                                            latOffset = 0.5f + (Math.random().toFloat() - 0.5f) * 0.5f,
                                            lngOffset = 0.5f + (Math.random().toFloat() - 0.5f) * 0.5f,
                                            isMine = true,
                                            imageUrl = finalImageUrl,
                                            imageTags = analyzedTags,
                                            status = "open"
                                        )

                                        com.example.finditcampus.data.GlobalState.lastPostedType = reportType
                                        
                                        val repo = com.example.finditcampus.data.ReportRepository()
                                        repo.addReport(newItem)
                                        showSuccessAnimation = true
                                        kotlinx.coroutines.delay(1500)
                                        onClose()
                                    } catch(e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isPublishing = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Text(if (currentStep == 4) "Publish" else "Continue ->", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier.padding(paddingValues),
            label = "step_transition"
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                when (step) {
                    1 -> Step1WhatHappened(
                        selectedType = reportType,
                        onTypeSelected = { 
                            reportType = it
                            currentStep++ 
                        }
                    )
                    2 -> Step2SmartVision(
                        isAnalyzing = isAnalyzing,
                        onImageSelected = { bitmap ->
                            isAnalyzing = true
                            scope.launch {
                                try {
                                    val filename = "item_img_${System.currentTimeMillis()}.jpg"
                                    val file = java.io.File(context.filesDir, filename)
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        java.io.FileOutputStream(file).use { out ->
                                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                                        }
                                    }
                                    selectedItemImageUri = android.net.Uri.fromFile(file).toString()
                                    // Use ML Kit to visually analyze the photo for matching!
                                    val mlKit = com.example.finditcampus.ai.MLKitService()
                                    val extractedTags = mlKit.extractTagsFromImage(bitmap)
                                    analyzedTags = extractedTags
                                    kotlinx.coroutines.delay(800)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isAnalyzing = false
                                    currentStep++
                                }
                            }
                        }
                    )
                    3 -> Step3CampusPin(
                        selectedLocation = location,
                        onLocationSelected = { location = it }
                    )
                    4 -> Step4Details(
                        title = title,
                        onTitleChange = { title = it },
                        itemType = itemType,
                        onItemTypeChange = { itemType = it },
                        description = description,
                        onDescriptionChange = { description = it }
                    )
                }
            }
        }
        
        if (isPublishing || showSuccessAnimation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.White, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = showSuccessAnimation,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "UPI_Animation"
                    ) { isSuccess ->
                        if (!isSuccess) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = PrimaryOrange,
                                    modifier = Modifier.size(56.dp),
                                    strokeWidth = 6.dp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Posting...", color = TextDark, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                        } else {
                            var showTick by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                showTick = true
                            }
                            val scale by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (showTick) 1f else 0f,
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = 0.5f,
                                    stiffness = 200f
                                )
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Posted!", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    } // Closes Scaffold content
    } // Closes Box
} // Closes PostItemScreen

// Fallback border helper
@Composable
fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)

@Composable
fun StepHeader(step: String, title: String, subtitle: String) {
    Text(
        text = step,
        color = PrimaryOrange,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = title,
        color = TextDark,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 36.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = subtitle,
        color = TextGray,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun Step1WhatHappened(selectedType: String, onTypeSelected: (String) -> Unit) {
    StepHeader(
        step = "STEP 01",
        title = "What happened?",
        subtitle = "We'll use this to tune smart matching for you."
    )
    
    // Lost Option
    OptionCard(
        title = "I lost something",
        subtitle = "Tell the campus what you're looking for.",
        icon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = PrimaryOrangeDark, modifier = Modifier.size(40.dp)) },
        onClick = { onTypeSelected("lost") }
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Found Option
    OptionCard(
        title = "I found something",
        subtitle = "Help return an item to its owner.",
        icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PrimaryOrangeDark, modifier = Modifier.size(40.dp)) }, 
        onClick = { onTypeSelected("found") }
    )
}

@Composable
fun OptionCard(
    title: String,
    subtitle: String,
    icon: @Composable
() -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, SurfaceBeige, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceBeige),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, color = TextGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun Step2SmartVision(isAnalyzing: Boolean = false, onImageSelected: (android.graphics.Bitmap) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onImageSelected(bitmap)
    }
    
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                // Convert hardware bitmap to software for Gemini API if needed
                val softwareBitmap = bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true) ?: bitmap
                onImageSelected(softwareBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    StepHeader(
        step = "STEP 02 â€¢ SMART VISION",
        title = "Show us the item",
        subtitle = "Gemini will suggest tags so your report is easier to find."
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceBeige)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAnalyzing) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = PrimaryOrange,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Gemini is analyzing...", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Extracting tags, colors, and context.", color = TextGray, fontSize = 14.sp)
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, PrimaryOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("AI auto-tagging", fontWeight = FontWeight.Black, fontSize = 24.sp, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Take a clear photo of the item, and we'll pre-fill the form.", color = TextGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    if (!isAnalyzing) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(16.dp),
                border = borderStroke(PrimaryOrange)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PrimaryOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take photo", color = PrimaryOrangeDark, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(16.dp),
                border = borderStroke(PrimaryOrange)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = PrimaryOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose photo", color = PrimaryOrangeDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun Step3CampusPin(selectedLocation: String, onLocationSelected: (String) -> Unit) {
    StepHeader(
        step = "STEP 03 Â· CAMPUS PIN",
        title = "Where did it\nhappen?",
        subtitle = "Pick the closest landmark at Lovely Professional University."
    )
    
    // Map Placeholder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceBeige),
        contentAlignment = Alignment.Center
    ) {
        Text("LPU CAMPUS MAP", color = PrimaryOrangeDark, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Locations Grid (simulated with Columns and Rows)
    val locations = com.example.finditcampus.data.CampusData.locations.map { it.name }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (rowIndex in 0 until (locations.size / 2 + 1)) {
            val index1 = rowIndex * 2
            val index2 = index1 + 1
            if (index1 < locations.size) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LocationChip(
                        name = locations[index1],
                        isSelected = selectedLocation == locations[index1],
                        modifier = Modifier.weight(1f),
                        onClick = { onLocationSelected(locations[index1]) }
                    )
                    if (index2 < locations.size) {
                        LocationChip(
                            name = locations[index2],
                            isSelected = selectedLocation == locations[index2],
                            modifier = Modifier.weight(1f),
                            onClick = { onLocationSelected(locations[index2]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun LocationChip(name: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) LightOrangeTint else MintBackground)
            .border(1.dp, if (isSelected) PrimaryOrange else SurfaceBeige, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(name, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4Details(
    title: String, onTitleChange: (String) -> Unit,
    itemType: String, onItemTypeChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit
) {
    val predefinedCategories = listOf("Electronics", "IDs & Cards", "Keys", "Wallet/Purse", "Books/Notes", "Clothing", "Other")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(if (predefinedCategories.contains(itemType)) itemType else if (itemType.isNotEmpty()) "Other" else "") }

    StepHeader(
        step = "STEP 04 • DETAILS",
        title = "Make it\nrecognizable",
        subtitle = "A few details help the right person spot it quickly."
    )
    
    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = SurfaceBeige,
        unfocusedContainerColor = SurfaceBeige,
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = PrimaryOrange
    )
    
    Text("Title", fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        placeholder = { Text("e.g. Blue Hydro Flask", color = TextGray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = colors
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text("Item type", fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select category", color = TextGray) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(16.dp),
            colors = colors
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            predefinedCategories.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedCategory = selectionOption
                        if (selectionOption != "Other") {
                            onItemTypeChange(selectionOption)
                        } else {
                            onItemTypeChange("") // Clear for custom input
                        }
                        expanded = false
                    }
                )
            }
        }
    }
    if (selectedCategory == "Other") {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = itemType,
            onValueChange = onItemTypeChange,
            placeholder = { Text("Type custom category...", color = TextGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = colors
        )
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text("Description", fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 8.dp))
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = { Text("Add identifying details, stickers, or where you noticed it", color = TextGray) },
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = colors
    )
}













