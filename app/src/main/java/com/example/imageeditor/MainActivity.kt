package com.example.imageeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imageeditor.ui.theme.ImageEditorTheme
import androidx.compose.ui.graphics.painter.Painter
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ImageEditorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Set the background color to black

                ) {
                    ImageEditorScreen()
                }
            }
        }
    }
}


@Composable
fun ImageEditorScreen() {
    val viewModel: ImageViewModel = viewModel()
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderSection(viewModel)
        ImagePreviewSection(viewModel)
        EditingOptions(viewModel)
    }
}


@Composable
fun HeaderSection(viewModel: ImageViewModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = { viewModel.saveImageToGallery(context) },
            modifier = Modifier
                .width(100.dp)
                .height(60.dp)
        ) {
            Text(
                text = "Save",
                style = TextStyle(fontSize = 20.sp),
                color = Color.White
            )
        }

        TextButton(
            onClick = { viewModel.clearCurrentImage() },
            modifier = Modifier
                .width(100.dp)
                .height(60.dp)
        ) {
            Text(
                text = "Cancel",
                style = TextStyle(fontSize = 20.sp),
                color = Color.White
            )
        }
    }
}


@Composable
fun ImagePreviewSection(imageModel: ImageViewModel) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageModel.imageUri = result.data?.data
        }
    }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .size(400.dp),
        contentAlignment = Alignment.Center
    ) {
        // Observe the imageUri from the ViewModel
        imageModel.imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri, imageModel, context),
                contentDescription = "Displayed Image",
                modifier = Modifier.fillMaxSize() // Make the image fill the box
            )
        } ?: run {
            // Show button to pick an image if none is selected
            IconButton(
                onClick = {
                    // Launch picker to select an image
                    val pickImageIntent = Intent(Intent.ACTION_PICK).apply {
                        type = "image/*"
                    }
                    imagePickerLauncher.launch(pickImageIntent)
                },
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_photo),
                    contentDescription = "Add Image",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}


@Composable
fun EditingOptions(selectedOption: ImageViewModel) {

    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),

        horizontalArrangement = Arrangement.Center
    ) {

        IconButton(
            onClick = { selectedOption("Undo") },
            modifier = Modifier.size(40.dp)  // Set the size of the IconButton
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.undo),
                contentDescription = "Undo",
                tint = Color.White,
                modifier = Modifier.size(30.dp)  // Set the size of the Icon
            )
        }

        IconButton(
            onClick = { selectedOption("Redo") },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.redo),
                contentDescription = "Redo",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }

    // Editing Options Section
    Row(
        modifier = Modifier
            .padding(10.dp)
            .horizontalScroll(scrollState)
    ) {

        IconButton(
            onClick = { selectedOption("Basic Editing") },
            modifier = Modifier.size(100.dp)  // Set the size of the IconButton
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.basic_editing),
                contentDescription = "Basic Editing",
                tint = Color.White,
                modifier = Modifier.size(70.dp)  // Set the size of the Icon
            )
        }

        IconButton(
            onClick = { selectedOption("Advanced Editing") },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.advanced_editing),
                contentDescription = "Advanced Editing",
                tint = Color.White,
                modifier = Modifier.size(70.dp)
            )
        }

        IconButton(
            onClick = { selectedOption("Cropping and Selection") },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.crop),
                contentDescription = "Cropping and Selection",
                tint = Color.White,
                modifier = Modifier
                    .size(70.dp)
            )
        }

        IconButton(
            onClick = { selectedOption("Background Color Changer") },
            modifier = Modifier
                .size(100.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.background_color_changer),
                contentDescription = "Background Color Changer",
                tint = Color.White,
                modifier = Modifier.size(70.dp)
            )
        }

        IconButton(
            onClick = { selectedOption("Foreground Color Changer") },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.foreground_color_changer),
                contentDescription = "Foreground Color Changer",
                tint = Color.White,
                modifier = Modifier.size(70.dp)
            )
        }

        IconButton(
            onClick = { selectedOption("Filters") },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.filter),
                contentDescription = "Filters",
                tint = Color.White,
                modifier = Modifier.size(70.dp)
            )
        }
    }

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState, true),
    ) {
        Text(
            text = "      Basic Editing      ",
            style = TextStyle(fontSize = 13.sp), // Sets the font size to 24sp
            color = Color.White
        )

        Text(
            text = " Advance Editing   ",
            style = TextStyle(fontSize = 13.sp),
            color = Color.White
        )

        Text(
            text = "   Crop & Select    ",
            style = TextStyle(fontSize = 13.sp),
            color = Color.White
        )

        Text(
            text = "    Background  ",
            style = TextStyle(fontSize = 13.sp),
            color = Color.White
        )

        Text(
            text = "   Foreground  ",
            style = TextStyle(fontSize = 13.sp),
            color = Color.White
        )

        Text(
            text = "    Filters   ",
            style = TextStyle(fontSize = 13.sp),
            color = Color.White
        )


    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .horizontalScroll(scrollState),
    ) {}

}

class ImageViewModel : ViewModel() {
    var imageUri by mutableStateOf<Uri?>(null)
    private var _imageBitmap = mutableStateOf<ImageBitmap?>(null)
    val imageBitmap: State<ImageBitmap?> = _imageBitmap

    fun loadImage(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                withContext(Dispatchers.Main) {
                    _imageBitmap.value = bitmap?.asImageBitmap()
                }
            }
        }
    }
    fun saveImageToGallery(context: Context) {
        imageUri?.let { uri ->
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            val url = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            url?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    contentResolver.openInputStream(uri)?.copyTo(outputStream)
                }
            }
        }
    }

    fun clearCurrentImage() {
        imageUri = null
        _imageBitmap.value = null
    }
}


@Composable
fun rememberAsyncImagePainter(uri: Uri, viewModel: ImageViewModel, context: Context): Painter {
    viewModel.loadImage(uri, context)
    val imageBitmap by viewModel.imageBitmap
    return imageBitmap?.let { BitmapPainter(it) } ?: EmptyPainter
}

object EmptyPainter : Painter() {
    override val intrinsicSize: Size
        get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        // Draw nothing if no image is loaded
    }
}

fun selectedOption(s: String) {

}

@Preview(showBackground = true)
@Composable
fun ImageEditorPreview() {
    ImageEditorTheme {
        ImageEditorScreen()
    }
}
