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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.storyCreator.model.ArtDimension
import com.example.myapplication.storyCreator.model.ArtStyle
import com.example.myapplication.storyCreator.model.ProjectArtStyle
import com.example.myapplication.storyCreator.model.Reference
import com.example.myapplication.storyCreator.model.ReferenceType
import com.example.myapplication.ui.theme.LocalThemeManager

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
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    
    var showStyleSelector by remember { mutableStateOf(projectArtStyle == null) }
    var showAddReferenceDialog by remember { mutableStateOf(false) }
    var editingReference by remember { mutableStateOf<Reference?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (isDarkMode) PixelMidBlue else Color(0xFFF0F0F0))
    ) {
        // Theme toggle button at top right
        Box(
            modifier = Modifier
                .padding(top = 50.dp, end = 20.dp)
                .align(Alignment.TopEnd)
        ) {
            com.example.myapplication.ui.theme.AnimatedThemeToggle()
        }
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
                        .background(if (isDarkMode) PixelDarkBlue else Color(0xFFE0E0E0))
                        .border(3.dp, if (isDarkMode) PixelHighlight else Color(0xFF2196F3))
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
                            color = if (isDarkMode) PixelGold else Color(0xFF1976D2),
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Style: ${projectArtStyle.dimension.name} - ${projectArtStyle.style.name.replace("_", " ")}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (isDarkMode) PixelCyan else Color(0xFF2196F3),
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
                                .background(if (isDarkMode) PixelDarkBlue else Color(0xFFE8F4F8))
                                .border(2.dp, if (isDarkMode) PixelAccent else Color(0xFF2196F3))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No references yet. Click 'Add Reference' to get started!",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = if (isDarkMode) Color(0xFF666666) else Color(0xFF666666),
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
                // Replace placeholder handling with proper error messages for empty/null model data
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Text("No Model Data", fontSize = 12.sp, color = Color.Red, fontFamily = FontFamily.Monospace)
                    Text("Model URL is empty or null", fontSize = 10.sp, color = Color(0xFF666666), fontFamily = FontFamily.Monospace)
                }
            }

            modelData == "placeholder-3d-model-data" -> {
                // Replace placeholder handling with proper error messages for placeholder data
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("🎮", fontSize = 48.sp)
                    Text("3D Model Unavailable", fontSize = 12.sp, color = Color.Red, fontFamily = FontFamily.Monospace)
                    Text("Model generation failed or in progress", fontSize = 10.sp, color = Color(0xFF666666), fontFamily = FontFamily.Monospace)
                }
            }

            else -> {
                // Replace localhost with the API base URL for proper network access
                val finalModelUrl = if (modelData.contains("localhost")) {
                    // Replace localhost with the actual API base URL
                    modelData.replace("localhost:3001", com.example.myapplication.Repository.ApiClient.BASE_URL.removePrefix("http://"))
                        .replace("localhost", com.example.myapplication.Repository.ApiClient.BASE_URL.removePrefix("http://"))
                }else {
                    modelData
                }
                
                android.util.Log.d("Model3DViewer", "Original URL: $modelData")
                android.util.Log.d("Model3DViewer", "Final URL: $finalModelUrl")

                // Track if WebView has been initialized to prevent duplicate loading
                var isWebViewInitialized by remember { mutableStateOf(false) }

                AndroidView(
                    factory = { context ->
                        android.webkit.WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            
                            // Enhanced network and security settings for CDN access
                            settings.allowUniversalAccessFromFileURLs = true
                            settings.allowFileAccessFromFileURLs = true
                            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                            
                            // Transparent background
                            setBackgroundColor(0) 
                            
                            // Enhanced WebChromeClient to capture console logs
                            webChromeClient = object : android.webkit.WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                    consoleMessage?.let { msg ->
                                        val logLevel = when (msg.messageLevel()) {
                                            android.webkit.ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                            android.webkit.ConsoleMessage.MessageLevel.WARNING -> "WARN"
                                            android.webkit.ConsoleMessage.MessageLevel.DEBUG -> "DEBUG"
                                            else -> "INFO"
                                        }
                                        android.util.Log.d("Model3DViewer-JS", "[$logLevel] ${msg.message()} (${msg.sourceId()}:${msg.lineNumber()})")
                                    }
                                    return true
                                }
                            }
                            
                            // WebViewClient to capture page loading events
                            webViewClient = object : android.webkit.WebViewClient() {
                                override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    android.util.Log.d("Model3DViewer", "Page loading started: $url")
                                }
                                
                                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    android.util.Log.d("Model3DViewer", "Page loading finished: $url")
                                }
                                
                                override fun onReceivedError(view: android.webkit.WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                                    super.onReceivedError(view, errorCode, description, failingUrl)
                                    android.util.Log.e("Model3DViewer", "WebView error: $errorCode - $description for URL: $failingUrl")
                                }
                            }
                        }
                    },
                    update = { webView ->
                        // Only load HTML content once to prevent duplicate model rendering
                        if (!isWebViewInitialized) {
                            val htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

<title>3D Model Viewer</title>

<style>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

html, body {
    margin: 0;
    padding: 0;
    width: 100%;
    height: 100%;
    min-height: 100vh;
    background: #000;
    overflow: hidden;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
}

#container {
    width: 100%;
    height: 100%;
    min-height: 100vh;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
}

#loading, #error {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-family: monospace;
    z-index: 10;
}

#loading { color: cyan; }
#error { color: red; display: none; }

.spinner {
    width: 24px;
    height: 24px;
    border: 3px solid #333;
    border-top: 3px solid cyan;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 10px;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}
</style>
</head>

<body>
<div id="container"></div>

<div id="loading">
    <div class="spinner"></div>
    <div>Loading 3D Model…</div>
</div>

<div id="error"></div>

<script>
/* ==========================================================
   ONE-TIME STATE
========================================================== */
let initialized = false;
let scene, camera, renderer, controls, model;

/* ==========================================================
   HELPERS
========================================================== */
function showError(msg) {
    document.getElementById('loading').style.display = 'none';
    const el = document.getElementById('error');
    el.textContent = msg;
    el.style.display = 'block';
}

function hideLoading() {
    document.getElementById('loading').style.display = 'none';
}

function loadScript(src) {
    return new Promise((resolve, reject) => {
        const s = document.createElement('script');
        s.src = src;
        s.onload = resolve;
        s.onerror = reject;
        document.head.appendChild(s);
    });
}

/* ==========================================================
   LOAD LIBRARIES (SEQUENTIAL, SAFE)
========================================================== */
async function loadLibraries() {
    const THREE_CDNS = [
        'https://cdn.jsdelivr.net/npm/three@0.147.0/build/three.min.js',
        'https://unpkg.com/three@0.147.0/build/three.min.js'
    ];

    const GLTF_CDNS = [
        'https://cdn.jsdelivr.net/npm/three@0.147.0/examples/js/loaders/GLTFLoader.js'
    ];

    const ORBIT_CDNS = [
        'https://cdn.jsdelivr.net/npm/three@0.147.0/examples/js/controls/OrbitControls.js'
    ];

    for (const url of THREE_CDNS) {
        try { await loadScript(url); break; } catch {}
    }
    if (!window.THREE) throw 'Three.js failed to load';

    for (const url of GLTF_CDNS) {
        try { await loadScript(url); break; } catch {}
    }
    if (!THREE.GLTFLoader) throw 'GLTFLoader failed to load';

    for (const url of ORBIT_CDNS) {
        try { await loadScript(url); break; } catch {}
    }
}

/* ==========================================================
   INITIALIZE VIEWER (RUNS ONCE)
========================================================== */
function initViewer() {
    if (initialized) return;
    initialized = true;

    const container = document.getElementById('container');
    console.log('Container dimensions:', container.clientWidth, container.clientHeight);
    
    // Fallback to window dimensions if container has 0 height
    const width = container.clientWidth || window.innerWidth;
    const height = container.clientHeight || window.innerHeight;
    console.log('Using dimensions:', width, height);
    
    if (height === 0) {
        console.error('Container height is 0! Using window.innerHeight as fallback');
    }

    scene = new THREE.Scene();
    scene.background = new THREE.Color(0x2a2a2a);

    camera = new THREE.PerspectiveCamera(
        60,
        width / height,
        0.1,
        1000
    );
    camera.position.set(0, 0, 5);

    renderer = new THREE.WebGLRenderer({ 
        alpha: true, 
        powerPreference: 'low-power',
        precision: 'mediump'
    });
    renderer.setPixelRatio(1);
    
    const w = window.innerWidth || 300;
    const h = window.innerHeight || 300;
    renderer.setSize(w, h);
    camera.aspect = w / h;
    camera.updateProjectionMatrix();
    
    container.appendChild(renderer.domElement);

    scene.add(new THREE.AmbientLight(0xffffff, 1.2));

    const light = new THREE.DirectionalLight(0xffffff, 1.5);
    light.position.set(5, 5, 5);
    scene.add(light);

    if (THREE.OrbitControls) {
        controls = new THREE.OrbitControls(camera, renderer.domElement);
        controls.enableDamping = true;
        controls.enablePan = false;
    }

    loadModel();
    animate();
}

/* ==========================================================
   LOAD MODEL (ONLY ONCE)
========================================================== */
function loadModel() {
    const url = '$finalModelUrl';
    console.log('Loading model from URL:', url);
    const loader = new THREE.GLTFLoader();

    loader.load(
        url,
        gltf => {
            model = gltf.scene;

            const box = new THREE.Box3().setFromObject(model);
            const size = box.getSize(new THREE.Vector3());
            const center = box.getCenter(new THREE.Vector3());

            model.position.sub(center);
            model.scale.setScalar(2 / Math.max(size.x, size.y, size.z));

            scene.add(model);
            hideLoading();
        },
        xhr => {
            if (xhr.lengthComputable) {
                document.querySelector('#loading div:last-child').textContent =
                    `Loading ${'$'}{Math.round((xhr.loaded / xhr.total) * 100)}%`;
            }
        },
        err => showError('Failed to load model')
    );
}

/* ==========================================================
   RENDER LOOP
========================================================== */
function animate() {
    requestAnimationFrame(animate);
    if (controls) controls.update();
    renderer.render(scene, camera);
}

/* ==========================================================
   RESIZE
========================================================== */
window.addEventListener('resize', () => {
    if (!camera || !renderer) return;
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
});

/* ==========================================================
   START
========================================================== */
(async function start() {
    try {
        await loadLibraries();
        initViewer();
    } catch (e) {
        showError(e.toString());
    }
})();
</script>
</body>
</html>

                        """.trimIndent()
                        
                        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        
                        // Add logging to debug WebView content
                        android.util.Log.d("Model3DViewer", "Loading WebView with HTML content:")
                        android.util.Log.d("Model3DViewer", "Model URL: $finalModelUrl")
                        android.util.Log.d("Model3DViewer", "HTML Content (first 500 chars): ${htmlContent.take(500)}")
                        android.util.Log.d("Model3DViewer", "HTML Content (last 500 chars): ${htmlContent.takeLast(500)}")
                        
                        // Mark as initialized to prevent duplicate loading
                        isWebViewInitialized = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
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
                                                    // Refresh references to get the saved data from server
                                                    viewModel.loadReferences(projectId)
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
                                        // Refresh references to get the saved data from server
                                        if (projectId != null && viewModel != null) {
                                            viewModel.loadReferences(projectId)
                                        }
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
    val themeManager = LocalThemeManager.current
    val isDarkMode = themeManager.isDarkMode
    var expanded by remember { mutableStateOf(false) }
    val is3D = projectArtStyle?.dimension == ArtDimension.THREE_D

    Box(
        Modifier
            .fillMaxWidth()
            .background(if (isDarkMode) PixelDarkBlue else Color(0xFFE8F4F8))
            .border(2.dp, if (isDarkMode) PixelAccent else Color(0xFF2196F3))
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
                    // Only show thumbnail for 2D references or if no 3D model exists
                    if (!is3D || reference.modelData == null) {
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
                    } else {
                        // For 3D models, show a 3D icon instead of thumbnail
                        Box(
                            Modifier
                                .size(60.dp)
                                .background(PixelMidBlue)
                                .border(2.dp, PixelAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🎮",
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
                            color = if (isDarkMode) PixelGold else Color(0xFF1976D2)
                        )
                        Text(
                            buildString {
                                append(reference.type.name)
                                if (is3D && reference.modelData != null && reference.modelData != "placeholder-3d-model-data") {
                                    append(" • 3D Model")
                                } else if (reference.imageData != null) {
                                    append(" • Has Image")
                                }
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (isDarkMode) Color(0xFF999999) else Color(0xFF666666)
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

                    if (is3D && reference.modelData != null && reference.modelData != "placeholder-3d-model-data") {
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