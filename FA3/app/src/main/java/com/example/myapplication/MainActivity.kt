package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button

class MainActivity : ComponentActivity() {
    private var image: android.net.Uri? = null
    private var displayed by mutableStateOf<android.graphics.Bitmap?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            Layout()
        }


    }

    @Composable
    fun Layout() {
        Box(
            modifier = Modifier.fillMaxSize(),
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

            if (displayed == null) {
                Button(
                    onClick = {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, 1)
                    },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    androidx.compose.material3.Text(text = "Upload Image")
                }
            }

            Button(
                onClick = {
                    val intent = Intent(this@MainActivity, MainActivity2::class.java).apply {
                        putExtra("imageUri", image.toString())
                    }
                    startActivityForResult(intent,2)
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            ) {
                androidx.compose.material3.Text(text = "Send Image")
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            image = data.data
            getImage()
        }
        else if (requestCode == 2 && resultCode == RESULT_OK && data != null){
            val uri=data.getStringExtra("image")
            image = Uri.parse(uri)
            val parse= Uri.parse(uri)
            displayed=BitmapFactory.decodeStream(contentResolver.openInputStream(parse))
        }

    }

    private fun getImage() {
        image?.let { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            displayed = bitmap
        }
    }
}