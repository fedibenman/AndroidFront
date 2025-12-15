package com.example.myapplication.storyCreator.Views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.storyCreator.model.ArtDimension
import com.example.myapplication.storyCreator.model.ArtStyle
import com.example.myapplication.storyCreator.model.ProjectArtStyle
import com.example.myapplication.storyCreator.model.Reference
import com.example.myapplication.storyCreator.model.ReferenceType
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

@Composable
fun ReferencesScreen(
    projectId: String?,
    references: MutableList<Reference>,
    projectArtStyle: ProjectArtStyle?,
    viewModel: StoryProjectViewModel?,
    isLoading: Boolean = false,
    onArtStyleSelected: (ProjectArtStyle) -> Unit,
    onAddReference: (Reference) -> Unit = {},
    onUpdateReference: (Reference) -> Unit = {},
    onDeleteReference: (String) -> Unit = {}
) {
    var showStyleSelector by remember { mutableStateOf(projectArtStyle == null) }
    var showAddReferenceDialog by remember { mutableStateOf(false) }
    var editingReference by remember { mutableStateOf<Reference?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(PixelMidBlue)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PixelGold)
            }
        } else if (showStyleSelector || projectArtStyle == null) {
            ArtStyleSelector(
                onStyleSelected = { dimension, style ->
                    val artStyle = ProjectArtStyle(dimension, style)
                    onArtStyleSelected(artStyle)
                    showStyleSelector = false
                }
            )
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(PixelDarkBlue)
                        .border(3.dp, PixelHighlight)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "📚 REFERENCES LIBRARY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Style: ${projectArtStyle.dimension.name} - ${projectArtStyle.style.name.replace("_", " ")}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = PixelCyan,
                            letterSpacing = 0.5.sp
                        )
                    }
                    PixelButton(
                        onClick = { showStyleSelector = true },
                        icon = "🎨",
                        contentDescription = "Change Style"
                    )
                }

                Spacer(Modifier.height(16.dp))

                PixelTextButton(
                    onClick = { showAddReferenceDialog = true },
                    text = "+ ADD REFERENCE",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (references.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(PixelDarkBlue)
                                .border(2.dp, PixelAccent)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No references yet. Click 'Add Reference' to get started!",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        references.forEach { reference ->
                            ReferenceCard(
                                reference = reference,
                                projectArtStyle = projectArtStyle,
                                onDelete = { onDeleteReference(reference.id) },
                                onEdit = { editingReference = reference }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        if (showAddReferenceDialog) {
            AddReferenceDialog(
                projectId = projectId,
                projectArtStyle = projectArtStyle,
                viewModel = viewModel,
                onDismiss = { showAddReferenceDialog = false },
                onAdd = { reference ->
                    onAddReference(reference)
                    showAddReferenceDialog = false
                }
            )
        }

        if (editingReference != null) {
            EditReferenceDialog(
                projectId = projectId,
                reference = editingReference!!,
                projectArtStyle = projectArtStyle,
                viewModel = viewModel,
                onDismiss = { editingReference = null },
                onSave = { updatedReference ->
                    onUpdateReference(updatedReference)
                    editingReference = null
                }
            )
        }
    }
}

@Composable
fun ArtStyleSelector(
    onStyleSelected: (ArtDimension, ArtStyle) -> Unit
) {
    var selectedDimension by remember { mutableStateOf<ArtDimension?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .width(600.dp)
                .background(PixelDarkBlue)
                .border(3.dp, PixelHighlight)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "🎨 SELECT ART STYLE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelGold,
                    letterSpacing = 1.sp
                )

                if (selectedDimension == null) {
                    Text(
                        "Choose dimension:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = PixelCyan
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PixelTextButton(
                            onClick = { selectedDimension = ArtDimension.TWO_D },
                            text = "📐 2D Art",
                            modifier = Modifier.fillMaxWidth()
                        )
                        PixelTextButton(
                            onClick = { selectedDimension = ArtDimension.THREE_D },
                            text = "🎮 3D Art",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        "Selected: ${selectedDimension!!.name}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = PixelCyan
                    )

                    Text(
                        "Choose style:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = PixelCyan
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (selectedDimension == ArtDimension.TWO_D) {
                            PixelTextButton(
                                onClick = { onStyleSelected(ArtDimension.TWO_D, ArtStyle.PIXEL_ART) },
                                text = "🟦 Pixel Art",
                                modifier = Modifier.fillMaxWidth()
                            )
                            PixelTextButton(
                                onClick = { onStyleSelected(ArtDimension.TWO_D, ArtStyle.STANDARD_2D) },
                                text = "🎨 Standard 2D",
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            PixelTextButton(
                                onClick = { onStyleSelected(ArtDimension.THREE_D, ArtStyle.LOW_POLY) },
                                text = "📦 Low Poly",
                                modifier = Modifier.fillMaxWidth()
                            )
                            PixelTextButton(
                                onClick = { onStyleSelected(ArtDimension.THREE_D, ArtStyle.REALISTIC) },
                                text = "💎 Realistic 3D",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    PixelTextButton(
                        onClick = { selectedDimension = null },
                        text = "← BACK",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun Model3DViewer(
    modelData: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black)
            .border(2.dp, Color.Cyan),
        contentAlignment = Alignment.Center
    ) {
        when {
            modelData.isNullOrEmpty() -> {
                Text(
                    "No 3D Model",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontFamily = FontFamily.Monospace
                )
            }

            modelData == "placeholder-3d-model-data" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("🎮", fontSize = 48.sp)
                    Text("3D Model", fontSize = 12.sp, color = Color.Cyan, fontFamily = FontFamily.Monospace)
                    Text("Generating...", fontSize = 10.sp, color = Color.Yellow, fontFamily = FontFamily.Monospace)
                }
            }

            else -> {
                // Get context and lifecycle at composable level
                val context = LocalContext.current
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                
                // Create engine and model loader
                val engine = rememberEngine()
                val modelLoader = rememberModelLoader(engine)
                val cameraNode = rememberCameraNode(engine) {
                    // Position camera to view the model
                    position = Position(x = 0f, y = 0f, z = 4f)
                }
                
                val modelNode = rememberNodes {
                    // This will be populated when model loads
                }

                Scene(
                    modifier = Modifier.fillMaxSize(),
                    engine = engine,
                    modelLoader = modelLoader,
                    cameraNode = cameraNode,
                    childNodes = modelNode,
                    // Enable camera manipulation (pan, rotate, zoom)
                    cameraManipulator = rememberCameraManipulator()
                )
                
                // Load the model
                LaunchedEffect(modelData) {
                    try {
                        // Load model from URL (Meshy.ai returns a GLB URL)
                        // Download and load the GLB file
                        val asset = modelLoader.loadModel(
                            fileLocation = modelData
                        )
                        
                        if (asset != null) {
                            // Create instance from the asset
                            val instance = modelLoader.createInstance(asset)
                            
                            if (instance != null) {
                                val node = ModelNode(
                                    modelInstance = instance,
                                    scaleToUnits = 1.0f
                                )
                                modelNode.clear()
                                modelNode.add(node)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}




@Composable
fun AddReferenceDialog(
    projectId: String?,
    projectArtStyle: ProjectArtStyle?,
    viewModel: StoryProjectViewModel?,
    onDismiss: () -> Unit,
    onAdd: (Reference) -> Unit
) {
    var selectedType by remember { mutableStateOf<ReferenceType?>(null) }
    var name by remember { mutableStateOf("") }
    var lore by remember { mutableStateOf("") }
    var design by remember { mutableStateOf("") }
    var imageData by remember { mutableStateOf<String?>(null) }
    var modelData by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var generationError by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(700.dp)
                .heightIn(max = 750.dp)
                .padding(16.dp)
                .background(PixelDarkBlue)
                .border(3.dp, PixelHighlight)
                .clickable(enabled = false) { }
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "➕ ADD NEW REFERENCE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelGold,
                    letterSpacing = 1.sp
                )

                if (selectedType == null) {
                    Text(
                        "Select reference type:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = PixelCyan
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PixelTextButton(
                            onClick = { selectedType = ReferenceType.CHARACTER },
                            text = "👤 CHARACTER",
                            modifier = Modifier.weight(1f)
                        )
                        PixelTextButton(
                            onClick = { selectedType = ReferenceType.ENVIRONMENT },
                            text = "🌍 ENVIRONMENT",
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Text(
                        "Type: ${if (selectedType == ReferenceType.CHARACTER) "👤 CHARACTER" else "🌍 ENVIRONMENT"}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = PixelCyan
                    )

                    Text(
                        "Name:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(PixelMidBlue)
                            .border(2.dp, PixelAccent)
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (name.isEmpty()) {
                                        Text(
                                            if (selectedType == ReferenceType.CHARACTER)
                                                "e.g., Captain Aria"
                                            else
                                                "e.g., Enchanted Forest",
                                            color = Color(0xFF666666),
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Text(
                        "Lore:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(PixelMidBlue)
                            .border(2.dp, PixelAccent)
                    ) {
                        BasicTextField(
                            value = lore,
                            onValueChange = { lore = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(12.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (lore.isEmpty()) {
                                        Text(
                                            "Background story and context...",
                                            color = Color(0xFF666666),
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Text(
                        if (selectedType == ReferenceType.CHARACTER) "Character Design:" else "Environment Design:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(PixelMidBlue)
                            .border(2.dp, PixelAccent)
                    ) {
                        BasicTextField(
                            value = design,
                            onValueChange = { design = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(12.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (design.isEmpty()) {
                                        Text(
                                            if (selectedType == ReferenceType.CHARACTER)
                                                "Physical appearance, clothing, accessories..."
                                            else
                                                "Landscape, architecture, atmosphere...",
                                            color = Color(0xFF666666),
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    if (imageData != null && imageData!!.isNotEmpty()) {
                        Text(
                            if (projectArtStyle?.dimension == ArtDimension.THREE_D) "Generated Assets:" else "Generated Image:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = PixelCyan,
                            fontWeight = FontWeight.Bold
                        )

                        if (projectArtStyle?.dimension == ArtDimension.THREE_D && modelData != null) {
                            Model3DViewer(
                                modelData = modelData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        } else {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color.Black)
                                    .border(2.dp, PixelAccent)
                            ) {
                                ImageFromBase64(
                                    base64 = imageData!!,
                                    contentDescription = "Generated reference image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    if (generationError != null) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF3A1A1A))
                                .border(2.dp, Color(0xFFFF6B6B))
                                .padding(12.dp)
                        ) {
                            Text(
                                "❌ $generationError",
                                color = Color(0xFFFF6B6B),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // AI Generation Button
                    if (selectedType != null && name.isNotEmpty() && lore.isNotEmpty() && design.isNotEmpty()) {
                        PixelTextButton(
                            onClick = {
                                if (projectId != null && viewModel != null) {
                                    isGenerating = true
                                    generationError = null
                                    
                                    // Create temporary reference first
                                    val tempRef = Reference(
                                        type = selectedType!!,
                                        name = name,
                                        lore = lore,
                                        design = design
                                    )
                                    
                                    // Add reference first, then generate assets
                                    viewModel.addReference(projectId, tempRef) {
                                        // After adding, get the reference ID and generate
                                        val addedRef = viewModel.references.value.lastOrNull { it.name == name }
                                        if (addedRef != null) {
                                            viewModel.generateReferenceAssets(
                                                projectId = projectId,
                                                referenceId = addedRef.id,
                                                onSuccess = { img, mdl ->
                                                    imageData = img
                                                    modelData = mdl
                                                    isGenerating = false
                                                },
                                                onError = { err ->
                                                    generationError = err
                                                    isGenerating = false
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            text = if (isGenerating) "⏳ GENERATING..." else if (imageData != null) "🔄 REGENERATE" else "🎨 GENERATE WITH AI",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGenerating && projectId != null && viewModel != null
                        )
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(PixelAccent)
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PixelTextButton(
                            onClick = { selectedType = null },
                            text = "← BACK",
                            modifier = Modifier.weight(1f)
                        )

                        PixelTextButton(
                            onClick = {
                                if (name.isNotEmpty() && selectedType != null) {
                                    onAdd(
                                        Reference(
                                            type = selectedType!!,
                                            name = name,
                                            lore = lore,
                                            design = design,
                                            imageData = imageData,
                                            modelData = modelData
                                        )
                                    )
                                }
                            },
                            text = "✓ ADD",
                            modifier = Modifier.weight(1f),
                            enabled = name.isNotEmpty() && lore.isNotEmpty() && design.isNotEmpty()
                        )
                    }
                }

                if (selectedType == null) {
                    PixelTextButton(
                        onClick = onDismiss,
                        text = "✕ CANCEL",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun EditReferenceDialog(
    projectId: String?,
    reference: Reference,
    projectArtStyle: ProjectArtStyle?,
    viewModel: StoryProjectViewModel?,
    onDismiss: () -> Unit,
    onSave: (Reference) -> Unit
) {
    var name by remember { mutableStateOf(reference.name) }
    var lore by remember { mutableStateOf(reference.lore) }
    var design by remember { mutableStateOf(reference.design) }
    var imageData by remember { mutableStateOf(reference.imageData) }
    var modelData by remember { mutableStateOf(reference.modelData) }
    var isGenerating by remember { mutableStateOf(false) }
    var generationError by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(700.dp)
                .heightIn(max = 750.dp)
                .padding(16.dp)
                .background(PixelDarkBlue)
                .border(3.dp, PixelHighlight)
                .clickable(enabled = false) { }
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "✏️ EDIT REFERENCE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelGold,
                    letterSpacing = 1.sp
                )

                Text(
                    "Type: ${if (reference.type == ReferenceType.CHARACTER) "👤 CHARACTER" else "🌍 ENVIRONMENT"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = PixelCyan
                )

                Text("Name:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                Box(Modifier.fillMaxWidth().background(PixelMidBlue).border(2.dp, PixelAccent)) {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    )
                }

                Text("Lore:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                Box(Modifier.fillMaxWidth().background(PixelMidBlue).border(2.dp, PixelAccent)) {
                    BasicTextField(
                        value = lore,
                        onValueChange = { lore = it },
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(12.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    )
                }

                Text(
                    if (reference.type == ReferenceType.CHARACTER) "Character Design:" else "Environment Design:",
                    fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White
                )
                Box(Modifier.fillMaxWidth().background(PixelMidBlue).border(2.dp, PixelAccent)) {
                    BasicTextField(
                        value = design,
                        onValueChange = { design = it },
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(12.dp),
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PixelTextButton(
                        onClick = {
                            if (projectId != null && viewModel != null) {
                                isGenerating = true
                                generationError = null
                                viewModel.generateReferenceAssets(
                                    projectId = projectId,
                                    referenceId = reference.id,
                                    onSuccess = { img, mdl ->
                                        imageData = img
                                        modelData = mdl
                                        isGenerating = false
                                    },
                                    onError = { err ->
                                        generationError = err
                                        isGenerating = false
                                    }
                                )
                            }
                        },
                        text = if (isGenerating) "⏳ GENERATING..." else if (imageData != null) "🔄 REGENERATE" else "🎨 GENERATE",
                        modifier = Modifier.weight(1f),
                        enabled = !isGenerating && name.isNotEmpty() && lore.isNotEmpty() && design.isNotEmpty() && projectId != null && viewModel != null
                    )

                    if (imageData != null) {
                        PixelTextButton(
                            onClick = { imageData = null; modelData = null },
                            text = "🗑️ REMOVE",
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                }

                if (generationError != null) {
                    Box(Modifier.fillMaxWidth().background(Color(0xFF3A1A1A)).border(2.dp, Color(0xFFFF6B6B)).padding(12.dp)) {
                        Text("❌ $generationError", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                if (imageData != null && imageData!!.isNotEmpty()) {
                    Text("Reference Image:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = PixelCyan, fontWeight = FontWeight.Bold)
                    if (projectArtStyle?.dimension == ArtDimension.THREE_D && modelData != null) {
                        Model3DViewer(modelData = modelData, modifier = Modifier.fillMaxWidth().height(200.dp))
                    } else {
                        Box(Modifier.fillMaxWidth().height(200.dp).background(Color.Black).border(2.dp, PixelAccent)) {
                            ImageFromBase64(base64 = imageData!!, contentDescription = "Reference image", modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                Box(Modifier.fillMaxWidth().height(2.dp).background(PixelAccent))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PixelTextButton(onClick = onDismiss, text = "✕ CANCEL", modifier = Modifier.weight(1f))
                    PixelTextButton(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onSave(reference.copy(name = name, lore = lore, design = design, imageData = imageData, modelData = modelData))
                            }
                        },
                        text = "💾 SAVE",
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotEmpty() && lore.isNotEmpty() && design.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
fun ReferenceCard(
    reference: Reference,
    projectArtStyle: ProjectArtStyle?,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val is3D = projectArtStyle?.dimension == ArtDimension.THREE_D

    Box(
        Modifier
            .fillMaxWidth()
            .background(PixelDarkBlue)
            .border(2.dp, PixelAccent)
            .clickable { expanded = !expanded }
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reference.imageData != null && reference.imageData!!.isNotEmpty()) {
                        Box(
                            Modifier
                                .size(60.dp)
                                .background(Color.Black)
                                .border(2.dp, PixelAccent)
                        ) {
                            ImageFromBase64(
                                base64 = reference.imageData!!,
                                contentDescription = "Reference thumbnail",
                                modifier = Modifier.fillMaxSize()
                            )

                            if (is3D && reference.modelData != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xDD000000))
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        "🎮",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            Modifier
                                .size(60.dp)
                                .background(PixelMidBlue)
                                .border(2.dp, PixelAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (reference.type == ReferenceType.CHARACTER) "👤" else "🌍",
                                fontSize = 28.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            reference.name,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelGold
                        )
                        Text(
                            buildString {
                                append(reference.type.name)
                                if (is3D && reference.modelData != null) {
                                    append(" • 3D Model")
                                } else if (reference.imageData != null) {
                                    append(" • Has Image")
                                }
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelButton(
                        onClick = { onEdit() },
                        icon = "✏️",
                        contentDescription = "Edit",
                        modifier = Modifier.size(32.dp)
                    )
                    PixelButton(
                        onClick = onDelete,
                        icon = "🗑️",
                        contentDescription = "Delete",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300, easing = EaseOutCubic)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300, easing = EaseInCubic)) + fadeOut()
            ) {
                Column(
                    Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(PixelAccent)
                    )

                    if (is3D && reference.modelData != null) {
                        Text(
                            "🎮 3D Model View:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = PixelCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Model3DViewer(
                            modelData = reference.modelData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    } else if (reference.imageData != null && reference.imageData!!.isNotEmpty()) {
                        Text(
                            "🖼️ Reference Image:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = PixelCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(Color.Black)
                                .border(2.dp, PixelAccent)
                        ) {
                            ImageFromBase64(
                                base64 = reference.imageData!!,
                                contentDescription = "Reference image",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Text(
                        "📖 Lore:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = PixelCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        reference.lore,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.White,
                        lineHeight = 16.sp
                    )

                    Text(
                        if (reference.type == ReferenceType.CHARACTER) "🎨 Character Design:" else "🏞️ Environment Design:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = PixelCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        reference.design,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.White,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}