package com.example.imageeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.imageeditor.ui.theme.ImageEditorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LandingScreen : ComponentActivity() {
    private var image: android.net.Uri? = null
    private var displayed by mutableStateOf<android.graphics.Bitmap?>(null)
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
    Column(modifier = Modifier
        .fillMaxHeight(1f)
        ) {
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
            .padding(20.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = {
                viewModel.saveImageToGallery(context)
                viewModel.clearCurrentImage() },

            modifier = Modifier
                .width(80.dp)
                .height(30.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(50.dp))
                .border(BorderStroke(2.dp, Color.Gray), shape = RoundedCornerShape(50.dp)),


        ) {
            Text(
                text = "Save",
                style = TextStyle(fontSize = 13.sp),
                color = Color.Black
            )
        }

        TextButton(
            onClick = { viewModel.clearCurrentImage() },
            modifier = Modifier
                .width(80.dp)
                .height(30.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(50.dp))
                .border(BorderStroke(2.dp, Color.Gray), shape = RoundedCornerShape(50.dp)),
        ) {
            Text(
                text = "Cancel",
                style = TextStyle(fontSize = 13.sp),
                color = Color.Black
            )
        }
    }
}


@Composable
fun ImagePreviewSection(viewModel: ImageViewModel) {
    val context = LocalContext.current
    val imageBitmap = viewModel.imageBitmap.value

    // Log the current URI every time the composable recomposes
    Log.d("ImageEditor", "Composing ImagePreviewSection with URI: ${viewModel.imageUri}")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newUri = result.data?.data
            newUri?.let {
                viewModel.imageUri = it
                viewModel.loadImage(it, context)
            }
        }
    }

    LaunchedEffect(key1 = viewModel.imageUri) {
        viewModel.imageUri?.let {
            Log.d("ImageEditor", "Loading new image for URI: $it")
            viewModel.loadImage(it, context)
        }
    }


    Box(
        modifier = Modifier
            .background(Color.Black)
            .size(520.dp),
        contentAlignment = Alignment.Center
    ) {
        imageBitmap?.let {
            Image(
                painter = BitmapPainter(it),
                contentDescription = "Displayed Image",
                modifier = Modifier.fillMaxSize()
            )
        } ?: IconButton(
            onClick = {
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
                tint = Color.Gray,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
fun EditingOptions(viewModel: ImageViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val changeBackgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newImageUri = result.data?.getStringExtra("newImageUri")
            newImageUri?.let {
                Log.d("ImageEditor", "New Image URI: $it")
                viewModel.updateImage(it, context)

            }
        }
    }


    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .padding(10.dp),

        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        IconButton(
            onClick = { selectedOption("Undo") },
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, shape = RoundedCornerShape(40.dp))
                .border(BorderStroke(2.dp, Color.Gray), shape = RoundedCornerShape(40.dp)),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.undo),
                contentDescription = "Undo",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)  // Set the size of the Icon
            )
        }

        IconButton(
            onClick = { selectedOption("Redo") },
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, shape = RoundedCornerShape(40.dp))
                .border(BorderStroke(2.dp, Color.Gray), shape = RoundedCornerShape(40.dp)),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.redo),
                contentDescription = "Redo",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Editing Options Section
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth()
    ) {

        IconButton(
            onClick = { selectedOption("Basic Editing") },
            modifier = Modifier
                .size(80.dp)
                .absolutePadding(left = 15.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.basic_editing),
                contentDescription = "Basic Editing",
                tint = Color.Gray,
                modifier = Modifier
                    .size(60.dp)  // Set the size of the Icon
            )
        }

        IconButton(
            onClick = { selectedOption("Cropping and Selection") },
            modifier = Modifier
                .size(80.dp)

        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.crop),
                contentDescription = "Cropping and Selection",
                tint = Color.Gray,
                modifier = Modifier
                    .size(50.dp)
            )
        }

        IconButton(
            onClick = { selectedOption("Advanced Editing") },
            modifier = Modifier
                .size(80.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.advanced_editing),
                contentDescription = "Advanced Editing",
                tint = Color.Gray,
                modifier = Modifier.size(50.dp)
            )
        }

        IconButton(
            onClick = {
                val intent = Intent(context, ChangeBackground::class.java).apply {
                    putExtra("imageUri", viewModel.imageUri.toString())
                }
                changeBackgroundLauncher.launch(intent)
            },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.background_color_changer),
                contentDescription = "Background Color Changer",
                tint = Color.Gray,
                modifier = Modifier.size(50.dp)
            )
        }


        IconButton(
            onClick = { selectedOption("Filters") },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.filter),
                contentDescription = "Filters",
                tint = Color.Gray,
                modifier = Modifier.size(50.dp)
            )
        }

        IconButton(
            onClick = { selectedOption("Foreground Color Changer") },
            modifier = Modifier
                .size(80.dp)
                .absolutePadding(right = 8.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.foreground_color_changer),
                contentDescription = "Foreground Color Changer",
                tint = Color.Gray,
                modifier = Modifier.size(50.dp)
            )
        }


    }

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState, true)
            .fillMaxWidth(),
    ) {
        Text(
            text = "       Basic Editing      ",
            style = TextStyle(fontSize = 10.sp), // Sets the font size to 24sp
            color = Color.LightGray
        )

        Text(
            text = "     Crop & Select   ",
            style = TextStyle(fontSize = 10.sp),
            color = Color.LightGray
        )

        Text(
            text = "    Advance Editing  ",
            style = TextStyle(fontSize = 10.sp),
            color = Color.LightGray
        )

        Text(
            text = "   Background Color    ",
            style = TextStyle(fontSize = 10.sp),
            color = Color.LightGray
        )

        Text(
            text = "   Apply Filters     ",
            style = TextStyle(fontSize = 10.sp),
            color = Color.LightGray
        )

        Text(
            text = " Foreground Color   ",
            style = TextStyle(fontSize = 10.sp),
            color = Color.LightGray
        )
    }
}

class ImageViewModel : ViewModel() {
    var imageUri by mutableStateOf<Uri?>(null)
    private var _imageBitmap = mutableStateOf<ImageBitmap?>(null)
    val imageBitmap: State<ImageBitmap?> = _imageBitmap

    fun loadImage(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    withContext(Dispatchers.Main) {
                        _imageBitmap.value = bitmap?.asImageBitmap()
                    }
                } ?: Log.e("ImageViewModel", "Failed to open input stream for URI: $uri")
            } catch (e: Exception) {
                Log.e("ImageViewModel", "Error loading image from URI: $uri", e)
            }
        }
    }

    fun updateImage(resultUri: String?, context: Context) {
        resultUri?.let {
            val uniqueUri = Uri.parse(it).buildUpon().appendQueryParameter("timestamp", System.currentTimeMillis().toString()).build()
            Log.d("ImageEditor", "Updating image URI to: $uniqueUri")
            imageUri = uniqueUri
            loadImage(uniqueUri, context)
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
