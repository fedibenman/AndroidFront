package com.example.myapplication.storyCreator.Views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.myapplication.ui.theme.AnimatedThemeToggle
import com.example.myapplication.ui.theme.LocalThemeManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.storyCreator.model.FlowNode
import com.example.myapplication.storyCreator.model.FlowchartState
import com.example.myapplication.storyCreator.model.NodeType
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// Constants
private const val NODE_WIDTH = 140
private const val NODE_HEIGHT = 70
private const val NODE_BORDER = 3
private const val CONNECTION_RADIUS = 10f
private const val SHADOW_OFFSET = 3
private const val CONNECTION_SNAP_DISTANCE = 30f

// Color scheme - Fancy pixel art theme
val PixelDarkBlue = Color(0xFF1a1a2e)
val PixelMidBlue = Color(0xFF16213e)
val PixelAccent = Color(0xFF0f3460)
val PixelHighlight = Color(0xFFe94560)
val PixelGold = Color(0xFFffd369)
val PixelCyan = Color(0xFF00d4ff)
private val PixelPurple = Color(0xFF9d4edd)

// Extension functions
private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
}

@Composable
fun ImageUploadDialog(
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            Modifier
                .align(Alignment.Center)
                .width(400.dp)
                .padding(16.dp)
                .clickable(enabled = false) { },
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Upload Image",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF5F5F5))
                        .border(2.dp, Color(0xFFCCCCCC))
                        .clickable {
                            onImageSelected("image_placeholder_data")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📁", fontSize = 48.sp)
                        Text(
                            "Click to select an image",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            "(File picker not available in preview)",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

data class DrawingPath(
    val points: List<Offset>,
    val color: Color
)

fun saveSketchAsBase64(
    pathsData: List<DrawingPath>,
    canvasSize: IntSize
): String? {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) {
        return null
    }

    if (pathsData.isEmpty()) {
        return null
    }

    try {
        val bitmap = Bitmap.createBitmap(
            canvasSize.width,
            canvasSize.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            strokeWidth = 5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        pathsData.forEach { drawingPath ->
            paint.color = drawingPath.color.toArgb()

            val points = drawingPath.points
            if (points.size >= 2) {
                for (i in 0 until points.size - 1) {
                    canvas.drawLine(
                        points[i].x,
                        points[i].y,
                        points[i + 1].x,
                        points[i + 1].y,
                        paint
                    )
                }
            }
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        bitmap.recycle()
        outputStream.close()

        return "data:image/png;base64,$base64String"

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
fun PixelArtIcon(iconType: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val pixelSize = size.width / 8f

        when (iconType) {
            "close" -> {
                drawRect(Color.White, Offset(pixelSize * 1, pixelSize * 1),
                    Size(pixelSize, pixelSize)
                )
                drawRect(Color.White, Offset(pixelSize * 2, pixelSize * 2), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 3, pixelSize * 3), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 4, pixelSize * 4), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 5, pixelSize * 5), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 6, pixelSize * 6), Size(pixelSize, pixelSize))

                drawRect(Color.White, Offset(pixelSize * 6, pixelSize * 1), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 5, pixelSize * 2), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 4, pixelSize * 3), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 3, pixelSize * 4), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 2, pixelSize * 5), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 1, pixelSize * 6), Size(pixelSize, pixelSize))
            }
            "save" -> {
                for (i in 2..5) {
                    drawRect(Color.White, Offset(pixelSize * 2, pixelSize * i), Size(pixelSize * 4, pixelSize))
                }
                drawRect(Color.Black, Offset(pixelSize * 3, pixelSize * 2), Size(pixelSize * 2, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 3, pixelSize * 5), Size(pixelSize * 2, pixelSize * 1))
            }
            "cancel" -> {
                drawRect(Color.White, Offset(pixelSize * 2, pixelSize * 3), Size(pixelSize * 4, pixelSize * 2))
                drawRect(Color.White, Offset(pixelSize * 2, pixelSize * 2), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 2, pixelSize * 5), Size(pixelSize, pixelSize))
                drawRect(Color.White, Offset(pixelSize * 1, pixelSize * 3), Size(pixelSize, pixelSize * 2))
            }
        }
    }
}

@Composable
fun SketchPadDialog(
    onDismiss: () -> Unit,
    onSketchSaved: (String) -> Unit
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    var pathsData by remember { mutableStateOf(listOf<DrawingPath>()) }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    val canvasSize = remember { mutableStateOf(IntSize.Zero) }

    val colors = listOf(
        Color.Black, Color.Red, Color.Blue, Color.Green,
        Color.Yellow, Color.Magenta, Color.Cyan
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(600.dp)
                .padding(16.dp)
                .background(if (isDarkMode) PixelDarkBlue else Color(0xFFE8F4F8))
                .border(3.dp, if (isDarkMode) PixelHighlight else Color(0xFF2196F3))
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "✏️ SKETCH PAD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelGold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixelButton(
                            onClick = {
                                pathsData = emptyList()
                                currentPoints = emptyList()
                            },
                            icon = "⟲",
                            contentDescription = "Clear"
                        )

                        PixelButton(
                            onClick = onDismiss,
                            icon = "✕",
                            contentDescription = "Cancel"
                        )

                        PixelButton(
                            onClick = {
                                val base64Image = saveSketchAsBase64(
                                    pathsData = pathsData,
                                    canvasSize = canvasSize.value
                                )
                                if (base64Image != null) {
                                    onSketchSaved(base64Image)
                                    onDismiss()
                                }
                            },
                            enabled = pathsData.isNotEmpty(),
                            icon = "💾",
                            contentDescription = "Save"
                        )
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(color)
                                .border(
                                    width = if (color == selectedColor) 3.dp else 2.dp,
                                    color = if (color == selectedColor) PixelGold else PixelAccent
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.White)
                        .border(3.dp, PixelHighlight)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { size ->
                                canvasSize.value = size
                            }
                            .pointerInput(selectedColor) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        currentPoints = currentPoints + change.position
                                    },
                                    onDragEnd = {
                                        if (currentPoints.isNotEmpty()) {
                                            pathsData = pathsData + DrawingPath(
                                                points = currentPoints,
                                                color = selectedColor
                                            )
                                            currentPoints = emptyList()
                                        }
                                    }
                                )
                            }
                    ) {
                        pathsData.forEach { drawingPath ->
                            val points = drawingPath.points
                            if (points.size >= 2) {
                                for (i in 0 until points.size - 1) {
                                    drawLine(
                                        color = drawingPath.color,
                                        start = points[i],
                                        end = points[i + 1],
                                        strokeWidth = 5f
                                    )
                                }
                            }
                        }

                        if (currentPoints.size >= 2) {
                            for (i in 0 until currentPoints.size - 1) {
                                drawLine(
                                    color = selectedColor,
                                    start = currentPoints[i],
                                    end = currentPoints[i + 1],
                                    strokeWidth = 5f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditNodeDialog(
    node: FlowNode,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    var textValue by remember { mutableStateOf(node.text) }
    var showImageOptions by remember { mutableStateOf(false) }
    var showSketchPad by remember { mutableStateOf(false) }
    var currentImageData by remember { mutableStateOf(node.imageData) }
    var isGeneratingText by remember { mutableStateOf(false) }
    var isGeneratingImage by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(600.dp)
                .heightIn(max = 700.dp)
                .padding(16.dp)
                .background(if (isDarkMode) PixelDarkBlue else Color(0xFFE8F4F8))
                .border(3.dp, if (isDarkMode) PixelHighlight else Color(0xFF2196F3))
                .clickable(enabled = false) { }
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "✎ EDIT ${when (node.type) {
                        NodeType.Start -> "START"
                        NodeType.Story -> "STORY"
                        NodeType.Decision -> "DECISION"
                        NodeType.End -> "END"
                    }} NODE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelGold,
                    letterSpacing = 1.sp
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(PixelMidBlue)
                        .border(2.dp, PixelAccent)
                ) {
                    BasicTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(12.dp),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxSize()) {
                                if (textValue.isEmpty()) {
                                    Text(
                                        "Enter the story text or choice...",
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

                // AI Generate Text Button
                PixelTextButton(
                    onClick = {
                        isGeneratingText = true
                        // TODO: Add AI text generation logic here
                        kotlinx.coroutines.MainScope().launch {
                            kotlinx.coroutines.delay(1500) // Simulate API call
                            textValue = "Generated story text based on context..."
                            isGeneratingText = false
                        }
                    },
                    text = if (isGeneratingText) "⏳ GENERATING..." else "🤖 AI GENERATE TEXT",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGeneratingText
                )

                Text(
                    "📷 ADD IMAGE (OPTIONAL)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelCyan,
                    letterSpacing = 0.5.sp
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PixelTextButton(
                        onClick = { showImageOptions = true },
                        text = "📁 UPLOAD",
                        modifier = Modifier.weight(1f)
                    )

                    PixelTextButton(
                        onClick = { showSketchPad = true },
                        text = "✏️ SKETCH",
                        modifier = Modifier.weight(1f)
                    )
                }

                // AI Generate Image Button
                PixelTextButton(
                    onClick = {
                        isGeneratingImage = true
                        // TODO: Add AI image generation logic here
                        kotlinx.coroutines.MainScope().launch {
                            kotlinx.coroutines.delay(2000) // Simulate API call
                            currentImageData = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
                            node.imageData = currentImageData
                            isGeneratingImage = false
                        }
                    },
                    text = if (isGeneratingImage) "⏳ GENERATING IMAGE..." else "🎨 AI GENERATE IMAGE",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGeneratingImage
                )

                if (currentImageData != null && currentImageData!!.isNotEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(PixelMidBlue)
                            .border(2.dp, PixelAccent)
                    ) {
                        if (currentImageData!!.startsWith("data:image")) {
                            ImageFromBase64(
                                base64 = currentImageData!!,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "📁 IMAGE ATTACHED ✓",
                                    color = PixelGold,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "Ready to save",
                                    fontSize = 10.sp,
                                    color = Color(0xFF666666),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
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
                        onClick = onDismiss,
                        text = "✕ CANCEL",
                        modifier = Modifier.weight(1f)
                    )

                    PixelTextButton(
                        onClick = { onSave(textValue) },
                        text = "💾 SAVE",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (showImageOptions) {
            ImageUploadDialog(
                onDismiss = { showImageOptions = false },
                onImageSelected = { imageData ->
                    currentImageData = imageData
                    node.imageData = imageData
                    showImageOptions = false
                }
            )
        }

        if (showSketchPad) {
            SketchPadDialog(
                onDismiss = { showSketchPad = false },
                onSketchSaved = { sketchData ->
                    currentImageData = sketchData
                    node.imageData = sketchData
                    showSketchPad = false
                }
            )
        }
    }
}
@Composable
fun FlowchartCanvas(
    state: FlowchartState,
    onSaveGraph: (FlowchartState) -> Unit
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    val density = LocalDensity.current
    val nodeWidthPx = with(density) { NODE_WIDTH.dp.toPx() }
    val nodeHeightPx = with(density) { NODE_HEIGHT.dp.toPx() }

    var scale by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var selectedConnection by remember { mutableStateOf<Pair<String, String>?>(null) }
    var previewMode by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val nodeDragOffsets = remember { mutableStateMapOf<String, Offset>() }

    var connectingFromNodeId by remember { mutableStateOf<String?>(null) }
    var connectionLineEnd by remember { mutableStateOf<Offset?>(null) }
    var hoveredTargetNodeId by remember { mutableStateOf<String?>(null) }

    var isCanvasDragging by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(if (isDarkMode) PixelDarkBlue else Color(0xFFF5F5F5))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(if (isDarkMode) PixelMidBlue else Color(0xFFE8F4F8))
                .pointerInput(Unit) {
                    detectTransformGestures { _, panChange, zoomChange, _ ->
                        if (zoomChange != 1f) {
                            scale = (scale * zoomChange).coerceIn(0.3f, 3f)
                        }
                        pan += panChange * scale
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isCanvasDragging = true
                            selectedNodeId = null
                            selectedConnection = null
                        },
                        onDrag = { change, dragAmount ->
                            if (isCanvasDragging && connectingFromNodeId == null) {
                                change.consume()
                                pan += dragAmount
                            }
                        },
                        onDragEnd = {
                            isCanvasDragging = false
                        },
                        onDragCancel = {
                            isCanvasDragging = false
                        }
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val position = event.changes.first().position

                            if (connectingFromNodeId != null) {
                                connectionLineEnd = position

                                val fromNode = state.findNode(connectingFromNodeId!!)
                                var foundTarget: String? = null

                                if (fromNode != null) {
                                    state.nodes.forEach { target ->
                                        if (target.id != connectingFromNodeId && target.type != NodeType.Start) {
                                            val inputPoint = Offset(
                                                target.position.x * scale + pan.x,
                                                target.position.y * scale + pan.y + nodeHeightPx / 2
                                            )

                                            if (position.distanceTo(inputPoint) <= CONNECTION_SNAP_DISTANCE) {
                                                val canConnect = when (fromNode.type) {
                                                    NodeType.Story -> when (target.type) {
                                                        NodeType.Story -> true
                                                        NodeType.Decision -> true
                                                        NodeType.End -> true
                                                        NodeType.Start -> false
                                                    }
                                                    NodeType.Decision -> when (target.type) {
                                                        NodeType.Story -> true
                                                        NodeType.Decision -> false
                                                        NodeType.End -> true
                                                        NodeType.Start -> false
                                                    }
                                                    NodeType.Start -> target.type == NodeType.Story ||
                                                            target.type == NodeType.Decision
                                                    NodeType.End -> false
                                                }
                                                if (canConnect) foundTarget = target.id
                                            }
                                        }
                                    }
                                }
                                hoveredTargetNodeId = foundTarget
                            }

                            event.changes.forEach { change ->
                                if (change.changedToUp() && connectingFromNodeId != null) {
                                    val fromNode = state.findNode(connectingFromNodeId!!)
                                    val targetNode = hoveredTargetNodeId?.let { state.findNode(it) }

                                    if (fromNode != null && targetNode != null) {
                                        if (fromNode.type == NodeType.Story && targetNode.type == NodeType.Decision) {
                                            if (!fromNode.outs.contains(targetNode.id)) {
                                                fromNode.outs.add(targetNode.id)
                                            }
                                        }
                                        else if (fromNode.type == NodeType.Story) {
                                            fromNode.outs.clear()

                                            if (targetNode.type == NodeType.Story) {
                                                state.nodes.forEach { node ->
                                                    if (node.id != fromNode.id && node.type == NodeType.Story) {
                                                        node.outs.remove(targetNode.id)
                                                    }
                                                }
                                            }

                                            fromNode.outs.add(targetNode.id)
                                        }
                                        else {
                                            if (!fromNode.outs.contains(targetNode.id)) {
                                                fromNode.outs.add(targetNode.id)
                                            }
                                        }
                                    }

                                    connectingFromNodeId = null
                                    connectionLineEnd = null
                                    hoveredTargetNodeId = null
                                    change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            DrawConnections(
                state = state,
                scale = scale,
                pan = pan,
                nodeWidthPx = nodeWidthPx,
                nodeHeightPx = nodeHeightPx,
                nodeDragOffsets = nodeDragOffsets,
                connectingFromNodeId = connectingFromNodeId,
                connectionLineEnd = connectionLineEnd,
                hoveredTargetNodeId = hoveredTargetNodeId,
                selectedConnection = selectedConnection,
                onSelectConnection = { fromId, toId ->
                    selectedConnection = Pair(fromId, toId)
                    selectedNodeId = null
                }
            )

            state.nodes.forEach { node ->
                key(node.id) {
                    NodeComponent(
                        node = node,
                        scale = scale,
                        pan = pan,
                        nodeWidthPx = nodeWidthPx,
                        nodeHeightPx = nodeHeightPx,
                        isSelected = selectedNodeId == node.id,
                        isHoveredForConnection = hoveredTargetNodeId == node.id,
                        isDraggingAnyNode = state.nodes.any { false },
                        dragOffset = nodeDragOffsets[node.id] ?: Offset.Zero,
                        onNodeClick = {
                            if (connectingFromNodeId == null) {
                                selectedNodeId = node.id
                                selectedConnection = null
                            }
                        },
                        onNodePositionChange = { newPos ->
                            node.position = newPos
                        },
                        onDragOffsetChange = { offset ->
                            nodeDragOffsets[node.id] = offset
                        },
                        onDragEnd = {
                            nodeDragOffsets.remove(node.id)
                        },
                        onStartConnection = { startPos ->
                            connectingFromNodeId = node.id
                            connectionLineEnd = startPos
                        }
                    )
                }
            }
        }

        BottomToolbar(
            scale = scale,
            pan = pan,
            previewMode = previewMode,
            selectedNodeId = selectedNodeId,
            selectedNodeType = selectedNodeId?.let { id -> state.findNode(id)?.type },
            selectedConnection = selectedConnection,
            onAddStory = {
                val rightMostX = state.nodes.maxOfOrNull { it.position.x } ?: 0f
                state.nodes.add(FlowNode(
                    type = NodeType.Story,
                    text = "Story",
                    position = Offset(rightMostX + 200f, 150f),
                    imageData = ""
                ))
            },
            onAddDecision = {
                val rightMostX = state.nodes.maxOfOrNull { it.position.x } ?: 0f
                state.nodes.add(FlowNode(
                    type = NodeType.Decision,
                    text = "Choice",
                    position = Offset(rightMostX + 200f, 150f),
                    imageData = ""
                ))
            },
            onEditNode = {
                showEditDialog = true
            },
            onDeleteNode = {
                selectedNodeId?.let { nodeId ->
                    val nodeToDelete = state.findNode(nodeId)
                    if (nodeToDelete != null &&
                        nodeToDelete.type != NodeType.Start &&
                        nodeToDelete.type != NodeType.End) {
                        state.nodes.removeIf { it.id == nodeId }
                        state.nodes.forEach { node ->
                            node.outs.remove(nodeId)
                        }
                        selectedNodeId = null
                    }
                }
            },
            onDeleteConnection = {
                selectedConnection?.let { (fromId, toId) ->
                    state.findNode(fromId)?.outs?.remove(toId)
                    selectedConnection = null
                }
            },
            onTogglePreview = { previewMode = !previewMode },
            onSave = { onSaveGraph(state) }
        )

        if (previewMode) {
            PreviewOverlay(
                state = state,
                onClose = { previewMode = false }
            )
        }

        if (showEditDialog && selectedNodeId != null) {
            val selectedNode = state.findNode(selectedNodeId!!)
            if (selectedNode != null) {
                EditNodeDialog(
                    node = selectedNode,
                    onDismiss = { showEditDialog = false },
                    onSave = { newText ->
                        selectedNode.text = newText
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun NodeComponent(
    node: FlowNode,
    scale: Float,
    pan: Offset,
    nodeWidthPx: Float,
    nodeHeightPx: Float,
    isSelected: Boolean,
    isHoveredForConnection: Boolean,
    isDraggingAnyNode: Boolean,
    dragOffset: Offset,
    onNodeClick: () -> Unit,
    onNodePositionChange: (Offset) -> Unit,
    onDragOffsetChange: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onStartConnection: (Offset) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var localDragOffset by remember { mutableStateOf(Offset.Zero) }

    val displayX = node.position.x + dragOffset.x
    val displayY = node.position.y + dragOffset.y

    val screenX = (displayX * scale + pan.x).roundToInt()
    val screenY = (displayY * scale + pan.y).roundToInt()

    Box(
        Modifier
            .offset { IntOffset(screenX, screenY) }
            .size(NODE_WIDTH.dp, NODE_HEIGHT.dp)
            .pointerInput(node.id) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        localDragOffset = Offset.Zero
                    },
                    onDrag = { change, delta ->
                        change.consume()
                        if (isDragging) {
                            localDragOffset += delta / scale
                            onDragOffsetChange(localDragOffset)
                        }
                    },
                    onDragEnd = {
                        if (isDragging) {
                            onNodePositionChange(node.position + localDragOffset)
                            localDragOffset = Offset.Zero
                            onDragEnd()
                            isDragging = false
                        }
                    },
                    onDragCancel = {
                        if (isDragging) {
                            onNodePositionChange(node.position + localDragOffset)
                            localDragOffset = Offset.Zero
                            onDragEnd()
                            isDragging = false
                        }
                    }
                )
            }
    ) {
        NodeVisual(
            node = node,
            isSelected = isSelected,
            onNodeClick = onNodeClick,
            onStartConnection = onStartConnection
        )

        if (isHoveredForConnection && node.type != NodeType.Start) {
            Box(
                Modifier
                    .size((CONNECTION_RADIUS * 2).dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-CONNECTION_RADIUS).dp)
                    .background(Color.Green, CircleShape)
            )
        }
    }
}



@Composable
fun NodeVisual(
    node: FlowNode,
    isSelected: Boolean,
    onNodeClick: () -> Unit,
    onStartConnection: (Offset) -> Unit
) {
    var showImage by remember(node.id) { mutableStateOf(false) }

    val bgColor = when (node.type) {
        NodeType.Start -> Color(0xFF90EE90)
        NodeType.Story -> Color(0xFFFFD700)
        NodeType.Decision -> Color(0xFF87CEEB)
        NodeType.End -> Color(0xFFFF6B6B)
    }

    val hasImage = !node.imageData.isNullOrBlank()
    val borderWidth = if (isSelected) 4.dp else 3.dp

    Box {
        // Shadow
        Box(
            modifier = Modifier
                .offset(3.dp, 3.dp)
                .size(140.dp, 70.dp)
                .background(Color(0xFF808080))
                .border(borderWidth, Color(0xFF606060))
        )

        // Main node
        Box(
            modifier = Modifier
                .size(140.dp, 70.dp)
                .border(borderWidth, Color.Black)
                .background(bgColor)
                .clickable { onNodeClick() }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (showImage && hasImage) {
                    // Show image view
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        // Image display
                        if (node.imageData != null) {
                            val isValidImageData = node.imageData!!.contains("base64") ||
                                    node.imageData!!.matches("^[A-Za-z0-9+/]+=*$".toRegex())

                            if (isValidImageData) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ImageFromBase64(
                                        base64 = node.imageData!!,
                                        contentDescription = "Node image",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                // Invalid image
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⚠️",
                                        fontSize = 20.sp,
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Show text view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Node text
                        Text(
                            text = if (node.text.isBlank()) {
                                when (node.type) {
                                    NodeType.Start -> "START"
                                    NodeType.Story -> "Story"
                                    NodeType.Decision -> "Choice"
                                    NodeType.End -> "END"
                                }
                            } else {
                                node.text
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Toggle button - always in top right corner
                if (hasImage) {
                    Icon(
                        imageVector = if (showImage) Icons.Default.TextFields else Icons.Default.CameraAlt,
                        contentDescription = if (showImage) "Show text" else "Show image",
                        tint = Color.Black,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(18.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                showImage = !showImage
                            }
                    )
                }
            }

            // Connection points - Right side (output)
            if (node.type != NodeType.End) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 10.dp)
                        .background(Color.Black, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> onStartConnection(offset) },
                                onDrag = { change, _ -> change.consume() }
                            )
                        }
                )
            }

            // Connection points - Left side (input)
            if (node.type != NodeType.Start) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (-10).dp)
                        .background(Color.Black, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
    }
}
@Composable
fun DrawConnections(
    state: FlowchartState,
    scale: Float,
    pan: Offset,
    nodeWidthPx: Float,
    nodeHeightPx: Float,
    nodeDragOffsets: Map<String, Offset>,
    connectingFromNodeId: String?,
    connectionLineEnd: Offset?,
    hoveredTargetNodeId: String?,
    selectedConnection: Pair<String, String>?,
    onSelectConnection: (String, String) -> Unit
) {
    Canvas(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    state.nodes.forEach { fromNode ->
                        fromNode.outs.forEach { toId ->
                            val target = state.findNode(toId) ?: return@forEach

                            // Apply drag offsets when calculating connection positions
                            val fromDragOffset = nodeDragOffsets[fromNode.id] ?: Offset.Zero
                            val toDragOffset = nodeDragOffsets[toId] ?: Offset.Zero

                            val fromPoint = Offset(
                                (fromNode.position.x + fromDragOffset.x) * scale + pan.x + nodeWidthPx,
                                (fromNode.position.y + fromDragOffset.y) * scale + pan.y + nodeHeightPx / 2
                            )
                            val toPoint = Offset(
                                (target.position.x + toDragOffset.x) * scale + pan.x,
                                (target.position.y + toDragOffset.y) * scale + pan.y + nodeHeightPx / 2
                            )

                            if (isPointNearConnection(pos, fromPoint, toPoint)) {
                                onSelectConnection(fromNode.id, toId)
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
    ) {
        // Existing connections
        state.nodes.forEach { node ->
            // Apply drag offset to the from node
            val fromDragOffset = nodeDragOffsets[node.id] ?: Offset.Zero
            val fromPoint = Offset(
                (node.position.x + fromDragOffset.x) * scale + pan.x + nodeWidthPx,
                (node.position.y + fromDragOffset.y) * scale + pan.y + nodeHeightPx / 2
            )

            node.outs.forEach { targetId ->
                val target = state.findNode(targetId) ?: return@forEach
                // Apply drag offset to the to node
                val toDragOffset = nodeDragOffsets[targetId] ?: Offset.Zero
                val toPoint = Offset(
                    (target.position.x + toDragOffset.x) * scale + pan.x,
                    (target.position.y + toDragOffset.y) * scale + pan.y + nodeHeightPx / 2
                )
                val isSelected = selectedConnection?.first == node.id && selectedConnection?.second == targetId
                val color = if (isSelected) Color(0xFFFF6B00) else Color.White // Orange when selected
                val strokeWidth = if (isSelected) 6f else 4f // Thicker default, even thicker when selected
                drawConnectionLine(fromPoint, toPoint, color, false, strokeWidth)
            }
        }

        // Temporary connection while dragging
        if (connectingFromNodeId != null && connectionLineEnd != null) {
            val fromNode = state.findNode(connectingFromNodeId) ?: return@Canvas
            val fromDragOffset = nodeDragOffsets[connectingFromNodeId] ?: Offset.Zero
            val fromPoint = Offset(
                (fromNode.position.x + fromDragOffset.x) * scale + pan.x + nodeWidthPx,
                (fromNode.position.y + fromDragOffset.y) * scale + pan.y + nodeHeightPx / 2
            )

            val targetPoint = if (hoveredTargetNodeId != null) {
                val target = state.findNode(hoveredTargetNodeId) ?: return@Canvas
                val toDragOffset = nodeDragOffsets[hoveredTargetNodeId] ?: Offset.Zero
                Offset(
                    (target.position.x + toDragOffset.x) * scale + pan.x,
                    (target.position.y + toDragOffset.y) * scale + pan.y + nodeHeightPx / 2
                )
            } else null

            val color = if (hoveredTargetNodeId != null) Color.Green else Color(0xFFAAAAAA)
            val isStraight = targetPoint == null

            if (isStraight) {
                drawLine(color, fromPoint, connectionLineEnd, strokeWidth = 3f)
            } else {
                drawConnectionLine(fromPoint, targetPoint!!, color, true, 3f)
            }
        }
    }
}

private fun DrawScope.drawConnectionLine(
    from: Offset,
    to: Offset,
    color: Color,
    isDragging: Boolean,
    strokeWidth: Float = 3f
) {
    val midX = (from.x + to.x) / 2

    // Horizontal from start
    drawLine(color, from, Offset(midX, from.y), strokeWidth)
    // Vertical
    drawLine(color, Offset(midX, from.y), Offset(midX, to.y), strokeWidth)
    // Horizontal to end
    drawLine(color, Offset(midX, to.y), to, strokeWidth)

    // Arrow
    val arrowSize = 8f
    val angle = atan2(to.y - from.y, to.x - from.x)
    drawLine(
        color, to,
        Offset(to.x - arrowSize * cos(angle - 0.5f), to.y - arrowSize * sin(angle - 0.5f)),
        strokeWidth
    )
    drawLine(
        color, to,
        Offset(to.x - arrowSize * cos(angle + 0.5f), to.y - arrowSize * sin(angle + 0.5f)),
        strokeWidth
    )
}

private fun isPointNearConnection(point: Offset, from: Offset, to: Offset): Boolean {
    val midX = (from.x + to.x) / 2
    val tolerance = 15f // Increased tolerance for easier clicking

    // Check horizontal segment 1
    if (point.y in (from.y - tolerance)..(from.y + tolerance) &&
        point.x in minOf(from.x, midX)..(maxOf(from.x, midX) + tolerance)) return true

    // Check vertical segment
    if (point.x in (midX - tolerance)..(midX + tolerance) &&
        point.y in minOf(from.y, to.y)..(maxOf(from.y, to.y) + tolerance)) return true

    // Check horizontal segment 2
    if (point.y in (to.y - tolerance)..(to.y + tolerance) &&
        point.x in (minOf(midX, to.x) - tolerance)..(maxOf(midX, to.x))) return true

    return false
}




@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: String,
    contentDescription: String
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                if (enabled) {
                    if (isDarkMode) Color(0xFF4A4A4A) else Color(0xFFE0E0E0)
                } else {
                    if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFCCCCCC)
                }
            )
            .border(
                width = 2.dp,
                color = if (enabled) {
                    if (isDarkMode) Color(0xFF6A6A6A) else Color(0xFF999999)
                } else {
                    if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFBBBBBB)
                }
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            color = if (enabled) {
                if (isDarkMode) Color.White else Color(0xFF333333)
            } else {
                if (isDarkMode) Color(0xFF666666) else Color(0xFF999999)
            }
        )
    }
}
// Pixel styled text button with label
@Composable
fun PixelTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    Box(
        modifier = modifier
            .height(36.dp)
            .background(
                if (enabled) {
                    if (isDarkMode) Color(0xFF4A4A4A) else Color(0xFFE0E0E0)
                } else {
                    if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFCCCCCC)
                }
            )
            .border(
                width = 2.dp,
                color = if (enabled) {
                    if (isDarkMode) Color(0xFF6A6A6A) else Color(0xFF999999)
                } else {
                    if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFBBBBBB)
                }
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (enabled) {
                if (isDarkMode) Color.White else Color(0xFF333333)
            } else {
                if (isDarkMode) Color(0xFF666666) else Color(0xFF999999)
            },
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun BottomToolbar(
    scale: Float,
    pan: Offset,
    previewMode: Boolean,
    selectedNodeId: String?,
    selectedNodeType: NodeType?,
    selectedConnection: Pair<String, String>?,
    onAddStory: () -> Unit,
    onAddDecision: () -> Unit,
    onEditNode: () -> Unit,
    onDeleteNode: () -> Unit,
    onDeleteConnection: () -> Unit,
    onTogglePreview: () -> Unit,
    onSave: () -> Unit
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFE0E0E0))
            .border(width = 3.dp, color = if (isDarkMode) Color(0xFF000000) else Color(0xFF999999))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Add Story button
        PixelTextButton(
            onClick = onAddStory,
            text = "+ STORY",
            modifier = Modifier.width(100.dp)
        )

        // Add Decision button
        PixelTextButton(
            onClick = onAddDecision,
            text = "+ CHOICE",
            modifier = Modifier.width(100.dp)
        )

        // Separator
        Box(
            Modifier
                .width(2.dp)
                .height(30.dp)
                .background(if (isDarkMode) Color(0xFF4A4A4A) else Color(0xFF999999))
        )

        // Edit Node icon
        PixelButton(
            onClick = onEditNode,
            enabled = selectedNodeId != null,
            icon = "✏️",
            contentDescription = "Edit Node"
        )

        // Delete Node icon
        PixelButton(
            onClick = onDeleteNode,
            enabled = selectedNodeId != null &&
                    selectedNodeType != NodeType.Start &&
                    selectedNodeType != NodeType.End,
            icon = "🗑️",
            contentDescription = "Delete Node"
        )

        // Delete Connection icon
        PixelButton(
            onClick = onDeleteConnection,
            enabled = selectedConnection != null,
            icon = "✂️",
            contentDescription = "Delete Connection"
        )

        Spacer(Modifier.weight(1f))

        // Zoom and Pan info
        Box(
            Modifier
                .background(if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFCCCCCC))
                .border(2.dp, if (isDarkMode) Color(0xFF4A4A4A) else Color(0xFF999999))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                "ZOOM: ${String.format("%.1f", scale)}x | PAN: (${pan.x.toInt()}, ${pan.y.toInt()})",
                color = if (isDarkMode) Color(0xFF00FF00) else Color(0xFF006600),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.weight(1f))

        // Preview icon
        PixelButton(
            onClick = onTogglePreview,
            icon = if (previewMode) "👁️" else "▶️",
            contentDescription = if (previewMode) "Exit Preview" else "Preview"
        )

        // Save icon
        PixelButton(
            onClick = onSave,
            icon = "💾",
            contentDescription = "Save Graph"
        )
    }
}
@Composable
fun PreviewOverlay(state: FlowchartState, onClose: () -> Unit) {
    val start = state.nodes.firstOrNull { it.type == NodeType.Start } ?: state.nodes.firstOrNull()
    var currentNodeId by remember { mutableStateOf(start?.id) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0a0a0a))
            .pointerInput(Unit) { }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Pixel art header - matching toolbar style
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .border(3.dp, Color(0xFF000000))
                    .padding(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📖 STORY MODE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF00),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    // Close button
                    PixelButton(
                        onClick = onClose,
                        icon = "✕",
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Main content area with toolbar styling
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2A2A2A))
                    .border(3.dp, Color(0xFF000000))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val node = state.findNode(currentNodeId ?: "") ?: return@Column

                // Node type badge
                Box(
                    Modifier
                        .background(Color(0xFF1A1A1A))
                        .border(2.dp, Color(0xFF4A4A4A))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "▶ ${when (node.type) {
                            NodeType.Start -> "🔴 START"
                            NodeType.Story -> "📝 STORY"
                            NodeType.Decision -> "❓ CHOICE"
                            NodeType.End -> "🔚 END"
                        }}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF00),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Display image if exists
                if (!node.imageData.isNullOrBlank()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color(0xFF1A1A1A))
                            .border(3.dp, Color(0xFF000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        ImageFromBase64(
                            base64 = node.imageData,
                            contentDescription = "Story image",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Text box with toolbar styling
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .border(3.dp, Color(0xFF000000))
                        .padding(16.dp)
                ) {
                    Text(
                        node.text.ifBlank { "No text provided." },
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                when (node.type) {
                    NodeType.Story, NodeType.Start -> {
                        val connectedDecisions = node.outs.mapNotNull { outId ->
                            state.findNode(outId)?.takeIf { it.type == NodeType.Decision }
                        }

                        if (connectedDecisions.isNotEmpty()) {
                            Text(
                                "⚔ CHOOSE YOUR PATH:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF00),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            connectedDecisions.forEach { decision ->
                                PixelTextButton(
                                    onClick = { currentNodeId = decision.id },
                                    text = "→ ${decision.text.ifBlank { "Choice" }}",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        } else {
                            val nextNode = node.outs.mapNotNull { state.findNode(it) }.firstOrNull()
                            if (nextNode != null) {
                                PixelTextButton(
                                    onClick = { currentNodeId = nextNode.id },
                                    text = "→ CONTINUE",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF3A2A1A))
                                        .border(2.dp, Color(0xFF4A4A4A))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "⚠ No follow-up connected.",
                                        color = Color(0xFFFFAA00),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                    NodeType.Decision -> {
                        val options = node.outs.mapNotNull { state.findNode(it) }
                        if (options.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF3A1A1A))
                                    .border(2.dp, Color(0xFF4A4A4A))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "⚠ No choices connected.",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Text(
                                "⚔ CHOOSE YOUR PATH:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF00),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            options.forEach { option ->
                                PixelTextButton(
                                    onClick = { currentNodeId = option.id },
                                    text = "→ ${option.text.ifBlank { "Option" }}",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                    NodeType.End -> {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A3A1A))
                                .border(3.dp, Color(0xFF000000))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "★ THE END ★",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF00),
                                textAlign = TextAlign.Center,
                                letterSpacing = 2.sp
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        PixelTextButton(
                            onClick = {
                                currentNodeId = state.nodes.firstOrNull { it.type == NodeType.Start }?.id
                            },
                            text = "↻ RESTART STORY",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlowBuilderScreen(
    projectId: String?,
    viewModel: StoryProjectViewModel? = null,
    onPersist: (FlowchartState) -> Unit = {}
) {
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    val nodes = remember { mutableStateListOf<FlowNode>() }
    val state = remember { FlowchartState(nodes) }
    var isLoading by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf("flow") }

    // References from ViewModel
    val references by viewModel?.references?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val projectArtStyle by viewModel?.projectArtStyle?.collectAsState() ?: remember { mutableStateOf(null) }
    val referencesLoading by viewModel?.referencesLoading?.collectAsState() ?: remember { mutableStateOf(false) }

    // Load project data
    LaunchedEffect(projectId) {
        if (projectId != null && viewModel != null) {
            isLoading = true
            // Load flowchart
            viewModel.loadFlowchart(projectId) { flowchartState ->
                if (flowchartState != null) {
                    state.nodes.clear()
                    state.nodes.addAll(flowchartState.nodes)
                } else {
                    state.nodes.clear()
                }
                isLoading = false
            }
            // Load references
            viewModel.loadReferences(projectId)
        } else {
            if (state.nodes.isEmpty()) {
                val s = FlowNode(
                    type = NodeType.Start,
                    text = "You awake",
                    position = Offset(60f, 40f),
                    imageData = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
                )
                val end = FlowNode(
                    type = NodeType.End,
                    text = "End of route",
                    position = Offset(860f, 140f),
                    imageData = ""
                )

                state.nodes.addAll(listOf(s, end))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Theme toggle button at top right
        Box(
            modifier = Modifier
                .padding(top = 50.dp, end = 20.dp)
                .align(Alignment.TopEnd)
        ) {
            AnimatedThemeToggle()
        }
        
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PixelGold)
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        "flow" -> {
                            FlowchartCanvas(
                                state = state,
                                onSaveGraph = { flow ->
                                    if (projectId != null && viewModel != null) {
                                        viewModel.saveFlowchart(projectId, flow)
                                        showSaveSuccess = true
                                        kotlinx.coroutines.MainScope().launch {
                                            kotlinx.coroutines.delay(2000)
                                            showSaveSuccess = false
                                        }
                                    }
                                    onPersist(flow)
                                }
                            )
                        }
                        "references" -> {
                            if (projectId != null && viewModel != null) {
                                ReferencesScreen(
                                    projectId = projectId,
                                    references = references.toMutableList(),
                                    projectArtStyle = projectArtStyle,
                                    viewModel = viewModel,
                                    isLoading = referencesLoading,
                                    onArtStyleSelected = { artStyle ->
                                        viewModel.updateArtStyle(projectId, artStyle)
                                    },
                                    onAddReference = { reference ->
                                        viewModel.addReference(projectId, reference)
                                    },
                                    onUpdateReference = { reference ->
                                        viewModel.updateReference(projectId, reference)
                                    },
                                    onDeleteReference = { referenceId ->
                                        viewModel.deleteReference(projectId, referenceId)
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "References not available in preview mode",
                                        fontFamily = FontFamily.Monospace,
                                        color = androidx.compose.ui.graphics.Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab Selector (Above Toolbar)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(if (isDarkMode) Color(0xFF0D0D0D) else Color(0xFFD0D0D0))
                        .border(width = 2.dp, color = if (isDarkMode) Color(0xFF000000) else Color(0xFF999999))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelTextButton(
                        onClick = { currentTab = "flow" },
                        text = "📊 FLOW EDITOR",
                        modifier = Modifier.weight(1f).height(40.dp)
                    )
                    PixelTextButton(
                        onClick = { currentTab = "references" },
                        text = "📚 REFERENCES",
                        modifier = Modifier.weight(1f).height(40.dp)
                    )
                }
            }
        }

        if (showSaveSuccess) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF00FF00), RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF00CC00), RoundedCornerShape(8.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 20.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PROJECT SAVED!",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}



@Preview(name = "Flow Builder", showBackground = true, widthDp = 1080, heightDp = 600)
@Composable
fun FlowBuilderScreenPreview() {
    FlowBuilderScreen(
        onPersist = { },
        projectId =null ,
    )
}




@Composable
fun ImageFromBase64(
    base64: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    // 1. Create the raw bitmap in remember
    val rawBitmap: Bitmap? = remember(base64) {
        try {
            val cleanBase64 = base64
                .removePrefix("data:image/png;base64,")
                .removePrefix("data:image/jpeg;base64,")
                .removePrefix("data:image/jpg;base64,")
                .trim()

            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e("ImageFromBase64", "Failed to decode image: ${e.message}")
            null
        }
    }

    // 2. Convert to ImageBitmap only when displaying
    val imageBitmap: ImageBitmap? = remember(rawBitmap) {
        rawBitmap?.asImageBitmap()
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Debug version - shows what's wrong
        Box(
            modifier = modifier
                .background(Color(0xFFE0E0E0))
                .border(1.dp, Color.Red)
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "❌ Failed to load image",
                    fontSize = 10.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Data length: ${base64.length}",
                    fontSize = 8.sp,
                    color = Color.DarkGray
                )
                Text(
                    "Preview: ${base64.take(20)}...",
                    fontSize = 8.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}