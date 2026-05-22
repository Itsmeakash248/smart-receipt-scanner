package com.example.presentation.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.domain.model.Receipt
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateBack: () -> Unit,
    onAnalysisFinished: (Receipt) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionRationale by remember { mutableStateOf(false) }

    // Executor for CameraX capturing
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }

    // Handlers for permission request
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
        if (!isGranted) {
            showPermissionRationale = true
        }
    }

    // Modern android gallery picker helper
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { mUri ->
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(mUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.analyzeReceipt(bitmap, onAnalysisFinished)
                }
            } catch (e: Exception) {
                Log.e("ScanScreen", "Failed to load image from gallery: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main analysis loading overlay
            if (uiState is ScanUiState.Analyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(enabled = false) {}, // Scrim
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Analyzing with Gemini AI...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extracting merchant, line items & amounts",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Active layout
                if (cameraPermissionGranted) {
                    var cameraBindingFailed by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Camera Preview View
                        if (!cameraBindingFailed) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }

                                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageCapture
                                            )
                                        } catch (exc: Exception) {
                                            Log.e("ScanScreen", "Use case binding failed", exc)
                                            cameraBindingFailed = true
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Render a nice Camera Fail guide with choice buttons
                            CameraFallbackGuide(
                                onLaunchGallery = {
                                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                onUseDemo = {
                                    val demoBitmap = getDemoReceiptBitmap(context)
                                    viewModel.analyzeReceipt(demoBitmap, onAnalysisFinished)
                                }
                            )
                        }

                        // Transparent Camera Aiming Viewfinder
                        ViewfinderFrame()

                        // Scan actions row
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(vertical = 32.dp, horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery picker button
                            IconButton(
                                onClick = {
                                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Open Gallery",
                                    tint = Color.White
                                )
                            }

                            // Capture trigger button
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(4.dp, Color.White, CircleShape)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        // Take picture and parse
                                        val outputOptions = ImageCapture.OutputFileOptions.Builder(
                                            context.cacheDir.resolve("temp_scan.jpg")
                                        ).build()

                                        imageCapture.takePicture(
                                            outputOptions,
                                            ContextCompat.getMainExecutor(context),
                                            object : ImageCapture.OnImageSavedCallback {
                                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(context.cacheDir.resolve("temp_scan.jpg"))
                                                    try {
                                                        val stream = context.contentResolver.openInputStream(savedUri)
                                                        val bitmap = BitmapFactory.decodeStream(stream)
                                                        if (bitmap != null) {
                                                            viewModel.analyzeReceipt(bitmap, onAnalysisFinished)
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("ScanScreen", "Failed to read captured photo", e)
                                                    }
                                                }

                                                override fun onError(exception: ImageCaptureException) {
                                                    Log.e("ScanScreen", "Image capture failed: ${exception.message}", exception)
                                                    // Fallback to demo or help on error
                                                    val demoBitmap = getDemoReceiptBitmap(context)
                                                    viewModel.analyzeReceipt(demoBitmap, onAnalysisFinished)
                                                }
                                            }
                                        )
                                    }
                            )

                            // Quick mock scan activator button (extremely convenient!)
                            IconButton(
                                onClick = {
                                    val demoBitmap = getDemoReceiptBitmap(context)
                                    viewModel.analyzeReceipt(demoBitmap, onAnalysisFinished)
                                },
                                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Simulate Scan",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                } else {
                    // Show full page message requesting permissions
                    PermissionDeniedView(
                        onGrantRequest = {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onLaunchGallery = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
        }
    }

    // Display error dialog on analysis error
    val activeState = uiState
    if (activeState is ScanUiState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Scanner Error") },
            text = { Text(activeState.message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("Retry")
                }
            }
        )
    }
}

@Composable
fun ViewfinderFrame() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(260.dp)
                .height(380.dp)
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            // Border indicators in corners for neat aesthetic styling
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
                    .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp, 0.dp, 0.dp, 0.dp))
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp, 16.dp, 0.dp, 0.dp))
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomStart)
                    .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp, 0.dp, 0.dp, 16.dp))
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp, 0.dp, 16.dp, 0.dp))
            )
        }
    }
}

@Composable
fun CameraFallbackGuide(
    onLaunchGallery: () -> Unit,
    onUseDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NoPhotography,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Camera Engine Found",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "CameraX is not active in this environment, which is common in cloud sandboxes. Use files or run a simulator scan!",
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLaunchGallery,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose receipt photo")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onUseDemo,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulate a receipt scan")
        }
    }
}

@Composable
fun PermissionDeniedView(
    onGrantRequest: () -> Unit,
    onLaunchGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera Permission Required",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "To scan physical receipts directly, please grant active camera permissions or choose standard gallery uploads.",
            color = Color.LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGrantRequest,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Grant Camera Access")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = onLaunchGallery,
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose from Photo Gallery")
        }
    }
}

// Generate an in-memory small coloredBitmap to pass into the Gemini parser safely!
fun getDemoReceiptBitmap(context: Context): Bitmap {
    // Return a basic colored square bitmap to simulate capturing a bitmap easily on sandboxes
    val size = 200
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()
    paint.color = android.graphics.Color.BLUE
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
    return bitmap
}
