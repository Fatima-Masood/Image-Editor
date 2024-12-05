package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.compose.ui.unit.sp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.net.URL
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt


class MainActivity2 : ComponentActivity() {

    private var displayed by mutableStateOf<Bitmap?>(null)
    private var original by mutableStateOf<Bitmap?>(null)
    private var tickConfirmation by mutableStateOf(false)
    private var crossConfirmation by mutableStateOf(false)
    private var value by mutableStateOf("")
    private var hexValue by mutableStateOf("")
    private var error by mutableStateOf(false)
    private var hexSelected by mutableStateOf(false)
    private var hexEntered by mutableStateOf(false)
    private var hexSelectedandEntered by mutableStateOf(false)
    private var hex_popup by mutableStateOf(false)
    private var initial by mutableStateOf<Bitmap?>(null)
    private var selectedColorEye by mutableStateOf(android.graphics.Color.TRANSPARENT)
    private var eyeDropperActivated by mutableStateOf(false)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        setContent {
            Layout()
        }

        getImage(imageUri)
    }

    @Composable
    fun Layout() {
        var selectedColor by remember { mutableStateOf(android.graphics.Color.TRANSPARENT) }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Image(
                painter = painterResource(id = R.drawable.color2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),

                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Background Color Changer",
                        color = Color(android.graphics.Color.parseColor("#655353")),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        fontFamily = FontFamily(Font(R.font.sansserif))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val density = LocalDensity.current.density
                Box(
                    modifier = Modifier
                        .size(520.dp)
                        .background(Color(0xFFF0F2D8))
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                if (eyeDropperActivated) {
                                    val bitmap = displayed ?: return@detectTapGestures
                                    val colorInt = getPixelColor(bitmap, offset.x, offset.y, density)
                                    selectedColorEye = colorInt
                                    applyColor(colorInt)
                                    eyeDropperActivated = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    displayed?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }


                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable { displayed = initial
                                selectedColor=android.graphics.Color.TRANSPARENT},
                        contentAlignment = Alignment.Center
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.ic_none),
                            contentDescription = "Remove Background",
                            modifier = Modifier.size(22.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable {
                                hexSelected=true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_brush),
                            contentDescription = "Add Value",
                            modifier = Modifier.size(22.dp),
                            contentScale = ContentScale.Fit
                        )
                    }



                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable {
                                hex_popup = true
                                selectedColor=android.graphics.Color.TRANSPARENT
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_addv),
                            contentDescription = "Add Value",
                            modifier = Modifier.size(22.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                }

                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom

                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(75.dp)
                            .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .clickable { onUploadImageClick()
                                selectedColor=android.graphics.Color.TRANSPARENT},
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Upload Image",
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Fit
                        )
                    }


                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(75.dp)
                            .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .clickable {
                                eyeDropperActivated = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_eye),
                            contentDescription = "Eye Dropper",
                            modifier = Modifier.size(22.dp),
                            contentScale = ContentScale.Fit
                        )
                    }



                    if (hexEntered){hexSelectedandEntered=true}
                    eachButton(color = android.graphics.Color.RED,isSelected = selectedColor == android.graphics.Color.RED)  {
                        selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.GREEN,isSelected = selectedColor == android.graphics.Color.GREEN) { selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.BLUE,isSelected = selectedColor == android.graphics.Color.BLUE) {selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.YELLOW,isSelected = selectedColor == android.graphics.Color.YELLOW) {selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.WHITE,isSelected = selectedColor == android.graphics.Color.WHITE) { selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.BLACK,isSelected = selectedColor == android.graphics.Color.BLACK) { selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.CYAN,isSelected = selectedColor == android.graphics.Color.CYAN) {selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.DKGRAY,isSelected = selectedColor == android.graphics.Color.DKGRAY) { selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.GRAY,isSelected = selectedColor == android.graphics.Color.GRAY) {
                        selectedColor = it
                        applyColor(it)
                    }
                    eachButton(color = android.graphics.Color.LTGRAY,isSelected = selectedColor == android.graphics.Color.LTGRAY) {selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.MAGENTA,isSelected = selectedColor == android.graphics.Color.MAGENTA) { selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.parseColor("#D11799"),isSelected = selectedColor == android.graphics.Color.parseColor("#D11799")) {selectedColor = it
                        applyColor(it)}
                    eachButton(color = android.graphics.Color.parseColor("#999B84"),isSelected = selectedColor == android.graphics.Color.parseColor("#999B84")) {selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.parseColor("#A8ABE0"),isSelected = selectedColor == android.graphics.Color.parseColor("#A8ABE0")) { selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.parseColor("#BADA55"),isSelected = selectedColor == android.graphics.Color.parseColor("#BADA55")) { selectedColor = it
                        applyColor(it)}
                    eachButton(color = android.graphics.Color.parseColor("#F6546A"),isSelected = selectedColor == android.graphics.Color.parseColor("#F6546A")) {selectedColor = it
                        applyColor(it) }
                    eachButton(color = android.graphics.Color.parseColor("#5D9FA0"),isSelected = selectedColor == android.graphics.Color.parseColor("#5D9FA0")) { selectedColor = it
                        applyColor(it) }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable {
                                crossConfirmation = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CrossIcon()

                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable {
                                tickConfirmation = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        TickIcon()
                    }
                }

                if(tickConfirmation){
                    showBoxTick()
                }

                if(crossConfirmation){
                    showBoxCross()
                }


                if (hexSelected) {
                    AlertDialog(
                        onDismissRequest = {
                            hexSelected = false
                            hexValue = ""
                            error = false
                        },
                        title = {
                            Text(text = "Enter Hex Value")
                        },
                        text = {
                            Column {
                                TextField(
                                    value = hexValue,
                                    onValueChange = { hexValue = it },
                                    label = { Text("Hex Value") }
                                )
                                if (error) {
                                    Text(
                                        text = "Incorrect value",
                                        color = Color.Red,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            error=false
                            Button(
                                onClick = {
                                    if (hexValue.length == 7 && hexValue[0] == '#' && hexValue.substring(1).all { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }) {
                                        hexEntered=true
                                        hexSelected = false
                                    } else {
                                        hexSelected = true
                                        error=true
                                    }
                                },colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color.Black
                                )

                            ) {
                                Text("Apply")
                            }
                        }
                    )
                }


                if (hex_popup) {
                    AlertDialog(
                        onDismissRequest = {
                            hex_popup = false
                            value = ""
                            error = false
                        },
                        title = {
                            Text(text = "Enter Hex Value")
                        },
                        text = {
                            Column {
                                TextField(
                                    value = value,
                                    onValueChange = { value = it },
                                    label = { Text("Hex Value") }
                                )
                                if (error) {
                                    Text(
                                        text = "Incorrect value",
                                        color = Color.Red,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            error=false
                            Button(
                                onClick = {
                                    if (value.length == 7 && value[0] == '#' && value.substring(1).all { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }) {
                                        val color = android.graphics.Color.parseColor(value)
                                        applyColor(color)
                                        hex_popup = false
                                    } else {
                                        hex_popup = true
                                        error=true
                                    }
                                },colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color.Black
                                )

                            ) {
                                Text("Apply")
                            }
                        }
                    )
                }
            }
        }
    }

    fun getPixelColor(bitmap: Bitmap, x: Float, y: Float, density: Float): Int {
        val scaledX = (x * density * bitmap.width / 1080).coerceIn(0f, bitmap.width.toFloat() - 1).toInt()
        val scaledY = (y * density * bitmap.height / 1920).coerceIn(0f, bitmap.height.toFloat() - 1).toInt()
        return try {
            bitmap.getPixel(scaledX, scaledY)
        } catch (e: Exception) {
            Color.Transparent.toArgb()
        }
    }


    @Composable
    fun showBoxTick() {
        AlertDialog(
            onDismissRequest = { tickConfirmation = false },
            title = {
                Text(text = "Confirmation")
            },
            text = {
                Text(text = "Are you sure you want to save changes?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        sendtoMain(displayed)
                        tickConfirmation = false
                    },colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Black
                    )
                ) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { tickConfirmation = false },colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Black
                    )
                ) {
                    Text(text = "No")
                }
            }
        )
    }

    @Composable
    fun showBoxCross() {
        AlertDialog(
            onDismissRequest = { crossConfirmation= false },
            title = {
                Text(text = "Confirmation")
            },
            text = {
                Text(text = "Are you sure you want to discard changes?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        sendtoMain(initial)
                        crossConfirmation = false
                    },colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Black
                    )

                ) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {


                Button(
                    onClick = { crossConfirmation = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Black
                    )
                ) {

                    Text(text = "No")
                }

            }

        )
    }

    fun onUploadImageClick() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                changeBackground2(this,imageUri)
            }
        }
    }

    @Composable
    fun eachButton(color: Int, isSelected: Boolean, onClick: (Int) -> Unit) {
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(75.dp)
                .background(color = Color(color), shape = RoundedCornerShape(8.dp))
                .border(width = 5.dp, color = if (isSelected) Color.Yellow else Color.Transparent, shape = RoundedCornerShape(8.dp))
                .clickable { onClick(color) },
            contentAlignment = Alignment.Center
        ) {
        }
    }

    private fun applyColor(color: Int) {
        original?.let {
            if (hexSelectedandEntered) {
                processBackground(it,hexValue, Color(color))
                hexValue = ""
            } else {
                changeBackground(it, color)
            }
        }
    }

    private fun changeBackground2(context: Context, imageUri: Uri) {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return
        val newBackground = BitmapFactory.decodeStream(inputStream) ?: return

        val originalBitmap = original ?: return
        val newBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)

        val foreRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val backRatio = newBackground.width.toFloat() / newBackground.height.toFloat()

        val X: Float
        val Y: Float

        if (foreRatio > backRatio) {
            X = originalBitmap.width.toFloat() / newBackground.width.toFloat()
            Y = X
        }
        else {
            Y = originalBitmap.height.toFloat() / newBackground.height.toFloat()
            X = Y
        }
        val transX = (originalBitmap.width - (newBackground.width * X)) / 2
        val transY = (originalBitmap.height - (newBackground.height * Y)) / 2


        val matrix = Matrix()
        matrix.setScale(X, Y)
        matrix.postTranslate(transX, transY)

        val canvas = android.graphics.Canvas(newBitmap)
        canvas.drawBitmap(newBackground, matrix, null)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        displayed = newBitmap
    }

    private fun processBackground(bitmap: Bitmap, hexValue: String, selectedColor: Color) {
        val enteredColor = android.graphics.Color.parseColor(hexValue)
        val value= 100

        displayed?.let { bitmap1 ->
            val newBitmap = bitmap1.copy(Bitmap.Config.ARGB_8888, true)
            for (x in 0 until bitmap1.width) {
                for (y in 0 until bitmap1.height) {
                    val colorPixel = bitmap1.getPixel(x, y)
                    val red = android.graphics.Color.red(colorPixel)
                    val green = android.graphics.Color.green(colorPixel)
                    val blue = android.graphics.Color.blue(colorPixel)
                    if (checkPixel(red, android.graphics.Color.red(enteredColor), value) &&
                        checkPixel(green, android.graphics.Color.green(enteredColor) , value) &&
                        checkPixel(blue, android.graphics.Color.blue(enteredColor), value)) {
                        newBitmap.setPixel(x, y, selectedColor.toArgb())
                    }
                }
            }

            val scaleX = newBitmap.width.toFloat() / bitmap.width.toFloat()
            val scaleY = newBitmap.height.toFloat() / bitmap.height.toFloat()
            val transX = (newBitmap.width - bitmap.width * scaleX) / 2
            val transY = (newBitmap.height - bitmap.height * scaleY) / 2

            val matrix = Matrix()
            matrix.postScale(scaleX, scaleY)
            matrix.postTranslate(transX, transY)

            val final = Bitmap.createBitmap(newBitmap.width, newBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(final)
            canvas.drawBitmap(newBitmap, 0f, 0f, null)
            canvas.drawBitmap(bitmap, matrix, null)
            displayed = final
            hexSelectedandEntered = false
            hexEntered = false
        }
    }

    fun checkPixel(value: Int, target: Int, threshold: Int): Boolean {
        return value >= target - threshold && value <= target + threshold
    }

    private fun changeBackground(bitmap: Bitmap, color: Int){
        val newBackground = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        newBackground.eraseColor(color)

        val scaleX = newBackground.width.toFloat() / bitmap.width.toFloat()
        val scaleY = newBackground.height.toFloat() / bitmap.height.toFloat()
        val transX = (newBackground.width - bitmap.width * scaleX) / 2
        val transY = (newBackground.height - bitmap.height * scaleY) / 2
        val mat = Matrix()
        mat.postScale(scaleX, scaleY)
        mat.postTranslate(transX, transY)
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(newBitmap)
        canvas.drawBitmap(newBackground, 0f, 0f, null)
        canvas.drawBitmap(bitmap, mat, null)

        displayed= newBitmap
    }
    private fun sendtoMain(bitmap: Bitmap?){
        bitmap?.let {
            val file = File(cacheDir, "image_next.jpg")
            it.writeBitmap(file)
            val intent = Intent().apply {
                putExtra("image", file.toUri().toString())
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun getImage(imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                withContext(Dispatchers.Main) {
                    original = bitmap
                    displayed = bitmap
                    initial = bitmap
                    detectBackground()

                }

                inputStream?.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun detectBackground() {
        original?.let { bitmap ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val temp = File.createTempFile("temp", ".jpeg", cacheDir)
                    bitmap.writeBitmap(temp)
                    val removed = removeBackgroundAPI(temp)
                    val newBitmap = BitmapFactory.decodeByteArray(removed, 0, removed.size)
                    withContext(Dispatchers.Main) {
                        original = newBitmap
                    }
                    temp.delete()
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
        }
    }




    private fun Bitmap.writeBitmap(file: File) {
        val output= file.outputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 100, output)
        output.flush()
        output.close()
    }


    private fun removeBackgroundAPI(file: File): ByteArray {
        val apiKey = "EVSp3THgec2jf1YjkqcXcFHr"
        val client = OkHttpClient()
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image_file", "image.jpg", RequestBody.create("image/jpeg".toMediaType(), file)).build()
        val request = Request.Builder().url("https://api.remove.bg/v1.0/removebg").addHeader("X-Api-Key", apiKey).post(body).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failure in removal: ${response.message}")
        }
        val type = response.header("Content-Type")
        if (type?.startsWith("application/json") == true) {
            val json = JSONObject(response.body?.string() ?: "")
            val image = json.getJSONObject("data").getString("result")
            return URL(image).readBytes()
        } else if (type?.startsWith("image/png") == true || type?.startsWith("image/jpeg")==true)  {
            return response.body?.bytes() ?: throw Exception("Empty")
        } else {
            throw Exception("Unexpected response type: $type")

        }

    }


}
@Composable
fun CrossIcon() {
    Canvas(modifier = Modifier.size(14.dp)) {
        drawLine(
            color = Color.White,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = 3f
        )
        drawLine(
            color = Color.White,
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
            strokeWidth = 3f
        )
    }
}
@Composable
fun TickIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(25.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Tick",
            tint = Color.White
        )
    }
}