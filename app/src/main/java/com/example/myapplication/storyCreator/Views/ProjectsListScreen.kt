package com.example.myapplication.storyCreator.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.storyCreator.DTOs.ProjectDto
import com.example.myapplication.storyCreator.ViewModel.PublishState
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ProjectCard(
    project: ProjectDto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(PixelMidBlue)
            .border(3.dp, PixelAccent)
            .clickable(onClick = onClick)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelGold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Fork badge
                    if (project.isFork) {
                        Box(
                            modifier = Modifier
                                .background(PixelCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .border(1.dp, PixelCyan, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔱 FORK",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelCyan
                            )
                        }
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

                // Show original author if it's a fork
                if (project.isFork && project.originalAuthorName != null) {
                    Text(
                        text = "Forked from ${project.originalAuthorName}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = PixelCyan,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated: ${dateFormat.format(Date(project.updatedAt))}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share button
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share to Community",
                        tint = PixelGold,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = onShare,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )

                    // Delete button
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Project",
                        tint = PixelHighlight,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                onClick = onDelete,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectsListScreen(
    viewModel: StoryProjectViewModel,
    onProjectClick: (String) -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val publishState by viewModel.publishState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectDto?>(null) }
    var projectToShare by remember { mutableStateOf<ProjectDto?>(null) }

    // Handle publish success
    LaunchedEffect(publishState) {
        if (publishState is PublishState.Success) {
            // Show success message (you can add a Snackbar here)
            viewModel.resetPublishState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Create button bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .border(width = 2.dp, color = Color(0xFF2A2A2A))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PixelHighlight, RoundedCornerShape(4.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                            .clickable { showCreateDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create New Project",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Projects Grid
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PixelGold)
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
                        Text(
                            "📝",
                            fontSize = 64.sp
                        )
                        Text(
                            "No projects yet",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Click the + button to create your first story!",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
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
                        ProjectCard(
                            project = project,
                            onClick = { onProjectClick(project.id) },
                            onDelete = { projectToDelete = project },
                            onShare = { projectToShare = project }
                        )
                    }
                }
            }
        }

        // Create Project Dialog
        if (showCreateDialog) {
            CreateProjectDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, description ->
                    showCreateDialog = false
                    viewModel.createNewProject(title, description) { projectId ->
                        onProjectClick(projectId)
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (projectToDelete != null) {
            DeleteConfirmationDialog(
                projectTitle = projectToDelete!!.title,
                onConfirm = {
                    viewModel.deleteProject(projectToDelete!!.id)
                    projectToDelete = null
                },
                onDismiss = { projectToDelete = null }
            )
        }

        // Share Confirmation Dialog
        if (projectToShare != null) {
            ShareConfirmationDialog(
                projectTitle = projectToShare!!.title,
                isLoading = publishState is PublishState.Loading,
                onConfirm = {
                    viewModel.publishProject(projectToShare!!.id) {
                        projectToShare = null
                    }
                },
                onDismiss = { projectToShare = null }
            )
        }

        // Show error if publish failed
        if (publishState is PublishState.Error) {
            AlertDialog(
                onDismissRequest = { viewModel.resetPublishState() },
                title = {
                    Text(
                        "Error",
                        fontFamily = FontFamily.Monospace,
                        color = PixelGold
                    )
                },
                text = {
                    Text(
                        (publishState as PublishState.Error).message,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                },
                confirmButton = {
                    PixelTextButton(
                        onClick = { viewModel.resetPublishState() },
                        text = "OK"
                    )
                },
                containerColor = PixelDarkBlue,
                modifier = Modifier.border(3.dp, PixelHighlight)
            )
        }
    }
}

@Composable
fun ShareConfirmationDialog(
    projectTitle: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(enabled = !isLoading, onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(400.dp)
                .background(PixelDarkBlue)
                .border(3.dp, PixelHighlight)
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "🌍 SHARE TO COMMUNITY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelGold,
                    letterSpacing = 1.sp
                )

                Text(
                    "Share \"$projectTitle\" with the community?",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Text(
                    "Other users will be able to view and fork your project.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PixelGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PixelTextButton(
                            onClick = onDismiss,
                            text = "✕ CANCEL",
                            modifier = Modifier.weight(1f)
                        )

                        PixelTextButton(
                            onClick = onConfirm,
                            text = "✓ SHARE",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(500.dp)
                .background(PixelDarkBlue)
                .border(3.dp, PixelHighlight)
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "✨ CREATE NEW STORY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelGold,
                    letterSpacing = 1.sp
                )

                // Title field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Title *",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = PixelCyan
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter project title...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelHighlight,
                            unfocusedBorderColor = PixelAccent
                        )
                    )
                }

                // Description field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Description (Optional)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = PixelCyan
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Enter description...") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PixelHighlight,
                            unfocusedBorderColor = PixelAccent
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PixelTextButton(
                        onClick = onDismiss,
                        text = "✕ CANCEL",
                        modifier = Modifier.weight(1f)
                    )

                    PixelTextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title.trim(), description.trim())
                            }
                        },
                        text = "✓ CREATE",
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    projectTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(400.dp)
                .background(PixelDarkBlue)
                .border(3.dp, PixelHighlight)
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "⚠️ DELETE PROJECT?",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelHighlight,
                    letterSpacing = 1.sp
                )

                Text(
                    "Are you sure you want to delete \"$projectTitle\"? This action cannot be undone.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PixelTextButton(
                        onClick = onDismiss,
                        text = "CANCEL",
                        modifier = Modifier.weight(1f)
                    )

                    PixelTextButton(
                        onClick = onConfirm,
                        text = "DELETE",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Preview Functions
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProjectsListScreenPreview() {
    // Create a simple composable state-based preview without ViewModel
    var projects by remember {
        mutableStateOf(listOf(
            ProjectDto(
                id = "1",
                title = "The Dragon's Quest",
                description = "An epic fantasy adventure through mystical lands",
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis(),
                isFork = false,
                originalProjectId = null,
                originalAuthorName = null
            ),
            ProjectDto(
                id = "2",
                title = "Space Odyssey 2099",
                description = "A sci-fi thriller set in deep space",
                createdAt = System.currentTimeMillis() - 172800000,
                updatedAt = System.currentTimeMillis() - 3600000,
                isFork = true,
                originalProjectId = "original_1",
                originalAuthorName = "SciFiMaster"
            ),
            ProjectDto(
                id = "3",
                title = "Mystery at Midnight Manor",
                description = "",
                createdAt = System.currentTimeMillis() - 259200000,
                updatedAt = System.currentTimeMillis() - 7200000,
                isFork = false,
                originalProjectId = null,
                originalAuthorName = null
            )
        ))
    }
    val isLoading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectDto?>(null) }
    var projectToShare by remember { mutableStateOf<ProjectDto?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Create button bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .border(width = 2.dp, color = Color(0xFF2A2A2A))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PixelHighlight, RoundedCornerShape(4.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                            .clickable { showCreateDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create New Project",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Projects Grid
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
                    ProjectCard(
                        project = project,
                        onClick = {},
                        onDelete = { projectToDelete = project },
                        onShare = { projectToShare = project }
                    )
                }
            }
        }

        // Create Dialog
        if (showCreateDialog) {
            CreateProjectDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, description ->
                    showCreateDialog = false
                    // Add new project to list for preview
                    val newProject = ProjectDto(
                        id = (projects.size + 1).toString(),
                        title = title,
                        description = description,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        isFork = false,
                        originalProjectId = null,
                        originalAuthorName = null
                    )
                    projects = projects + newProject
                }
            )
        }

        // Delete Confirmation Dialog
        if (projectToDelete != null) {
            DeleteConfirmationDialog(
                projectTitle = projectToDelete!!.title,
                onConfirm = {
                    projects = projects.filter { it.id != projectToDelete!!.id }
                    projectToDelete = null
                },
                onDismiss = { projectToDelete = null }
            )
        }

        // Share Confirmation Dialog
        if (projectToShare != null) {
            ShareConfirmationDialog(
                projectTitle = projectToShare!!.title,
                isLoading = false,
                onConfirm = {
                    // In preview, just close the dialog
                    projectToShare = null
                },
                onDismiss = { projectToShare = null }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProjectsListScreenEmptyPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .border(3.dp, Color(0xFF000000))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "📚 MY STORY PROJECTS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelGold,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PixelHighlight, RoundedCornerShape(4.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create New Project",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Empty State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "📝",
                        fontSize = 64.sp
                    )
                    Text(
                        "No projects yet",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Click the + button to create your first story!",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectCardPreview() {
    ProjectCard(
        project = ProjectDto(
            id = "1",
            title = "The Dragon's Quest",
            description = "An epic fantasy adventure through mystical lands filled with magic and danger",
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis(),
            isFork = true,
            originalAuthorName = "MasterStoryteller"
        ),
        onClick = {},
        onDelete = {} ,
        onShare = {  }
    )
}

@Preview(showBackground = true)
@Composable
fun CreateProjectDialogPreview() {
    CreateProjectDialog(
        onDismiss = {},
        onCreate = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun DeleteConfirmationDialogPreview() {
    DeleteConfirmationDialog(
        projectTitle = "The Dragon's Quest",
        onConfirm = {},
        onDismiss = {}
    )
}