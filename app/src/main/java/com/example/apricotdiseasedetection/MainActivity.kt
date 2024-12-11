package com.example.apricotdiseasedetection
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.yourapp.SingletonRequestQueue
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    val buttonGallery = findViewById<Button>(R.id.buttonGallery)
    val buttonCamera = findViewById<Button>(R.id.buttonCamera)
    val buttonPredict = findViewById<Button>(R.id.buttonPredict)


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val API_URL = "http://localhost:8022/predict/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the SingletonRequestQueue
        val queue = SingletonRequestQueue.getInstance(this).requestQueue

        buttonCamera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        buttonGallery.setOnClickListener {
            dispatchPickPictureIntent()
        }

        buttonPredict.setOnClickListener {
            imageView?.drawable?.let { drawable ->
                val bitmap = (drawable as BitmapDrawable).bitmap
                sendImageForPrediction(bitmap)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun dispatchPickPictureIntent() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(imageBitmap)
                    imageView.visibility = VISIBLE
                    buttonPredict.visibility = VISIBLE
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    val imageStream = contentResolver.openInputStream(imageUri!!)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    imageView.setImageBitmap(selectedImage)
                    imageView.visibility = VISIBLE
                    buttonPredict.visibility = VISIBLE
                }
            }
        }
    }

    private fun sendImageForPrediction(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val jsonBody = JSONObject()
        jsonBody.put("image", encodedImage)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            jsonBody,
            Response.Listener { response ->
                // Handle the response from the API
                val prediction = response.toString()
                // Display the prediction or further process it
                Toast.makeText(this, "Prediction: $prediction", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the RequestQueue
        SingletonRequestQueue.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
}
