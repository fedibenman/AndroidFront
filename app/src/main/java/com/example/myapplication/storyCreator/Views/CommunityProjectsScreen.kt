package com.example.myapplication.storyCreator.Views


import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.storyCreator.DTOs.CommunityProjectDto
import com.example.myapplication.storyCreator.ViewModel.CommunityProjectViewModel
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.storyCreator.model.FlowchartState
import com.example.myapplication.storyCreator.model.NodeType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class StoryStep(
    val nodeId: String,
    val nodeType: NodeType,
    val text: String,
    val imageData: String?,
    val choice: String? = null, // The choice made to get here
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CommunityProjectsScreen(
    viewModel: CommunityProjectViewModel,
    storyProjectViewModel: StoryProjectViewModel, // Add this parameter to load flowcharts
    onProjectClick: (String) -> Unit,
    onForkSuccess: () -> Unit
) {
    val projects by viewModel.communityProjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showPreview by remember { mutableStateOf(false) }
    var selectedProjectState by remember { mutableStateOf<FlowchartState?>(null) }
    var isLoadingProject by remember { mutableStateOf(false) }

    // Show preview overlay if a project is selected
    if (showPreview && selectedProjectState != null) {
        PreviewOverlayWithHistory(
            state = selectedProjectState!!,
            onClose = {
                showPreview = false
                selectedProjectState = null
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PixelDarkBlue)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Filter chips bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .border(width = 2.dp, color = Color(0xFF2A2A2A))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Popular", "Recent", "Starred").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = {
                                    selectedFilter = filter
                                    viewModel.filterProjects(filter)
                                },
                                label = {
                                    Text(
                                        filter,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PixelHighlight,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Projects Grid
                if (isLoading || isLoadingProject) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = PixelGold)
                            if (isLoadingProject) {
                                Text(
                                    "Loading preview...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("🌟", fontSize = 64.sp)
                            Text(
                                "No community projects yet",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(projects) { project ->
                            CommunityProjectCard(
                                project = project,
                                isAuthor = project.authorId == currentUserId,
                                onClick = {
                                    // Load the flowchart for preview
                                    isLoadingProject = true
                                    storyProjectViewModel.loadFlowchart(project.id) { flowchartState ->
                                        isLoadingProject = false
                                        if (flowchartState != null) {
                                            selectedProjectState = flowchartState
                                            showPreview = true
                                        }
                                    }
                                },
                                onStar = { viewModel.toggleStar(project.id) },
                                onFork = {
                                    viewModel.forkProject(project.id)
                                    onForkSuccess()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityProjectCard(
    project: CommunityProjectDto,
    isAuthor: Boolean,
    onClick: () -> Unit,
    onStar: () -> Unit,
    onFork: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(PixelMidBlue)
            .border(3.dp, PixelAccent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    // Preview Icon Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF2A5A2A), RoundedCornerShape(4.dp))
                            .border(2.dp, Color(0xFF00FF00), RoundedCornerShape(4.dp))
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            fontSize = 16.sp,
                            color = Color(0xFF00FF00),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (project.description.isNotEmpty()) {
                    Text(
                        text = project.description,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "by ${project.authorName}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = PixelCyan
                    )
                    if (isAuthor) {
                        Text(
                            text = "(You)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = PixelGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = PixelGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${project.starCount}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallSplit,
                            contentDescription = "Forks",
                            tint = PixelCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${project.forkCount}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = dateFormat.format(Date(project.createdAt)),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                // Action buttons row (Star and Fork only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Star button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                if (project.isStarredByUser) PixelGold else Color(0xFF2A2A2A),
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                2.dp,
                                if (project.isStarredByUser) PixelGold else PixelAccent,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable(onClick = onStar),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (project.isStarredByUser) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star",
                                tint = if (project.isStarredByUser) Color.White else PixelGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (project.isStarredByUser) "STARRED" else "STAR",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (project.isStarredByUser) Color.White else PixelGold
                            )
                        }
                    }

                    // Fork button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                if (isAuthor) Color(0xFF3A3A3A) else PixelHighlight,
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                2.dp,
                                if (isAuthor) Color.Gray else Color.White,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable(enabled = !isAuthor, onClick = onFork),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = "Fork",
                                tint = if (isAuthor) Color.Gray else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isAuthor) "YOUR PROJECT" else "FORK",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAuthor) Color.Gray else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PreviewOverlayWithHistory(
    state: FlowchartState,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val start = state.nodes.firstOrNull { it.type == NodeType.Start } ?: state.nodes.firstOrNull()
    var currentNodeId by remember { mutableStateOf(start?.id) }
    var storyHistory by remember { mutableStateOf<List<StoryStep>>(emptyList()) }
    var showHistory by remember { mutableStateOf(false) }

    // Initialize history with start node
    LaunchedEffect(Unit) {
        start?.let { startNode ->
            storyHistory = listOf(
                StoryStep(
                    nodeId = startNode.id,
                    nodeType = startNode.type,
                    text = startNode.text,
                    imageData = startNode.imageData
                )
            )
        }
    }

    if (showHistory) {
        // Show history view
        StoryHistoryView(
            history = storyHistory,
            onClose = { showHistory = false },
            onExportPdf = {
                exportStoryToPdf(context, storyHistory, state)
            }
        )
    } else {
        // Show normal preview
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
                // Header
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

                        // History button
                        PixelButton(
                            onClick = { showHistory = true },
                            icon = "📜",
                            contentDescription = "View History"
                        )

                        Spacer(Modifier.width(8.dp))

                        // Close button
                        PixelButton(
                            onClick = onClose,
                            icon = "✕",
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Main content
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

                    // Text box
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
                                        onClick = {
                                            currentNodeId = decision.id
                                            storyHistory = storyHistory + StoryStep(
                                                nodeId = decision.id,
                                                nodeType = decision.type,
                                                text = decision.text,
                                                imageData = decision.imageData,
                                                choice = decision.text
                                            )
                                        },
                                        text = "→ ${decision.text.ifBlank { "Choice" }}",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }
                            } else {
                                val nextNode = node.outs.mapNotNull { state.findNode(it) }.firstOrNull()
                                if (nextNode != null) {
                                    PixelTextButton(
                                        onClick = {
                                            currentNodeId = nextNode.id
                                            storyHistory = storyHistory + StoryStep(
                                                nodeId = nextNode.id,
                                                nodeType = nextNode.type,
                                                text = nextNode.text,
                                                imageData = nextNode.imageData
                                            )
                                        },
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
                                        onClick = {
                                            currentNodeId = option.id
                                            storyHistory = storyHistory + StoryStep(
                                                nodeId = option.id,
                                                nodeType = option.type,
                                                text = option.text,
                                                imageData = option.imageData,
                                                choice = option.text
                                            )
                                        },
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

                            // View Full Story button
                            PixelTextButton(
                                onClick = { showHistory = true },
                                text = "📜 VIEW FULL STORY",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(10.dp))

                            PixelTextButton(
                                onClick = {
                                    currentNodeId = state.nodes.firstOrNull { it.type == NodeType.Start }?.id
                                    storyHistory = listOf(
                                        StoryStep(
                                            nodeId = start?.id ?: "",
                                            nodeType = NodeType.Start,
                                            text = start?.text ?: "",
                                            imageData = start?.imageData
                                        )
                                    )
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
}

@Composable
fun StoryHistoryView(
    history: List<StoryStep>,
    onClose: () -> Unit,
    onExportPdf: () -> Unit
) {
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0a0a0a))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
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
                        "📜 YOUR STORY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF00),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))

                    // Export PDF button
                    PixelButton(
                        onClick = onExportPdf,
                        icon = "📄",
                        contentDescription = "Export PDF"
                    )

                    Spacer(Modifier.width(8.dp))

                    // Close button
                    PixelButton(
                        onClick = onClose,
                        icon = "✕",
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // History content
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2A2A2A))
                    .border(3.dp, Color(0xFF000000))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                history.forEachIndexed { index, step ->
                    // Step number and type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "STEP ${index + 1}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF00)
                        )

                        Text(
                            when (step.nodeType) {
                                NodeType.Start -> "🔴 START"
                                NodeType.Story -> "📝 STORY"
                                NodeType.Decision -> "❓ CHOICE"
                                NodeType.End -> "🔚 END"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Choice made (if any)
                    if (step.choice != null && step.choice.isNotBlank()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF3A2A5A))
                                .border(2.dp, Color(0xFF5A5AFF))
                                .padding(8.dp)
                        ) {
                            Text(
                                "→ Choice: ${step.choice}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFFAAAAFF)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Step content
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A1A))
                            .border(2.dp, Color(0xFF4A4A4A))
                            .padding(12.dp)
                    ) {
                        Text(
                            step.text.ifBlank { "No text" },
                            fontSize = 12.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }

                    if (index < history.size - 1) {
                        Spacer(Modifier.height(16.dp))

                        // Separator
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFF4A4A4A))
                        )

                        Spacer(Modifier.height(16.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Summary
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A3A1A))
                        .border(2.dp, Color(0xFF00FF00))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            "📊 SUMMARY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF00)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Total Steps: ${history.size}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                        Text(
                            "Choices Made: ${history.count { it.choice != null }}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun exportStoryToPdf(context: Context, history: List<StoryStep>, state: FlowchartState) {
    try {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        val margin = 50f
        val contentWidth = pageWidth - (2 * margin)

        var pageNumber = 1
        var yPosition = margin
        val lineHeight = 20f
        val maxYPosition = pageHeight - margin

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            textSize = 12f
        }

        val smallPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
        }

        // Title
        canvas.drawText("Story Playthrough", margin, yPosition, titlePaint)
        yPosition += lineHeight * 2

        // Date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        canvas.drawText("Generated: ${dateFormat.format(Date())}", margin, yPosition, smallPaint)
        yPosition += lineHeight * 2

        // Story content
        history.forEachIndexed { index, step ->
            // Check if we need a new page
            if (yPosition > maxYPosition - 100) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = margin
            }

            // Step header
            canvas.drawText("Step ${index + 1} - ${step.nodeType}", margin, yPosition, headerPaint)
            yPosition += lineHeight * 1.5f

            // Choice made
            if (step.choice != null && step.choice.isNotBlank()) {
                canvas.drawText("Choice: ${step.choice}", margin + 10, yPosition, bodyPaint)
                yPosition += lineHeight * 1.2f
            }

            // Step text (wrap text)
            val words = step.text.split(" ")
            var line = ""
            words.forEach { word ->
                val testLine = if (line.isEmpty()) word else "$line $word"
                val testWidth = bodyPaint.measureText(testLine)

                if (testWidth > contentWidth) {
                    canvas.drawText(line, margin + 10, yPosition, bodyPaint)
                    yPosition += lineHeight
                    line = word

                    // Check for new page
                    if (yPosition > maxYPosition - 50) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = margin
                    }
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, margin + 10, yPosition, bodyPaint)
                yPosition += lineHeight
            }

            yPosition += lineHeight * 0.5f
        }

        // Summary
        yPosition += lineHeight
        canvas.drawText("Summary", margin, yPosition, headerPaint)
        yPosition += lineHeight * 1.5f
        canvas.drawText("Total Steps: ${history.size}", margin + 10, yPosition, bodyPaint)
        yPosition += lineHeight
        canvas.drawText("Choices Made: ${history.count { it.choice != null }}", margin + 10, yPosition, bodyPaint)

        pdfDocument.finishPage(page)

        // Save the PDF
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Stories")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(directory, "Story_$timestamp.pdf")

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(context, "PDF saved to Downloads/Stories", Toast.LENGTH_LONG).show()
        Log.d("PDF Export", "Saved to: ${file.absolutePath}")
    } catch (e: Exception) {
        Log.e("PDF Export", "Error creating PDF", e)
        Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CommunityProjectsScreenPreview() {
    val projects = listOf(
        CommunityProjectDto(
            id = "1",
            title = "Epic Dragon Adventure",
            description = "A thrilling quest through mystical lands",
            authorId = "user1",
            authorName = "StoryMaster",
            starCount = 245,
            forkCount = 32,
            isStarredByUser = true,
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis()
        ),
        CommunityProjectDto(
            id = "2",
            title = "Space Station Mystery",
            description = "Solve the mystery aboard the space station",
            authorId = "currentUser",
            authorName = "SciFiWriter",
            starCount = 189,
            forkCount = 21,
            isStarredByUser = false,
            createdAt = System.currentTimeMillis() - 172800000,
            updatedAt = System.currentTimeMillis() - 3600000
        )
    )

    var projectsList by remember { mutableStateOf(projects) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .border(3.dp, Color(0xFF000000))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "🌍 COMMUNITY PROJECTS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelGold,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "Popular", "Recent", "Starred").forEach { filter ->
                            FilterChip(
                                selected = filter == "All",
                                onClick = {},
                                label = {
                                    Text(
                                        filter,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(projectsList) { project ->
                    CommunityProjectCard(
                        project = project,
                        isAuthor = project.authorId == "currentUser", // Simulating current user
                        onClick = {},
                        onStar = {
                            projectsList = projectsList.map {
                                if (it.id == project.id) {
                                    it.copy(
                                        isStarredByUser = !it.isStarredByUser,
                                        starCount = if (!it.isStarredByUser) it.starCount + 1 else it.starCount - 1
                                    )
                                } else it
                            }
                        },
                        onFork = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityProjectCardPreview() {
    CommunityProjectCard(
        project = CommunityProjectDto(
            id = "1",
            title = "Epic Dragon Adventure",
            description = "A thrilling quest through mystical lands filled with danger and magic",
            authorId = "user1",
            authorName = "StoryMaster",
            starCount = 245,
            forkCount = 32,
            isStarredByUser = true,
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis()
        ),
        isAuthor = false,
        onClick = {},
        onStar = {},
        onFork = {}
    )
}