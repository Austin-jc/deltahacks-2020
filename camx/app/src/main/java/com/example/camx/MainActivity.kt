package com.example.camx

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 10
private var lastAnalyzedTimestamp = 0L
//val localModel = FirebaseAutoMLLocalModel.Builder()
//    .setAssetFilePath("manifest.json")
//    .build()

//val options = FirebaseVisionObjectDetectorOptions.Builder()
//    .build()
//
//val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
class MainActivity : AppCompatActivity() {
    private class booundingRectView @JvmOverloads constructor (
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr){

        override fun onDraw(canvas: Canvas?){
            super.onDraw(canvas)

        }
    }
    private class ImgAnalyzer : ImageAnalysis.Analyzer{
        private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }

        override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
            val options = FirebaseVisionObjectDetectorOptions.Builder()
                .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build()

            val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)

            var myPaint = Paint()
            myPaint.setStyle(Paint.Style.STROKE)

            val currentTimestamp = System.currentTimeMillis()
            if(currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis((2))){
                val mediaImage = image?.image
                val imageRotation = degreesToFirebaseRotation(rotationDegrees)


                val bitmap = Bitmap.createBitmap(mediaImage?.width!!, mediaImage?.height!!, Bitmap.Config.ARGB_8888 )
                val canvas = Canvas(bitmap)
                if (mediaImage != null){
                    val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                    detector.processImage(image)
                        .addOnSuccessListener { items ->
                            for (item in items){
                                canvas.drawRect(item.boundingBox, myPaint)
                                textbox.setText(item.classificationCategory))
                            }
                        }
                        .addOnFailureListener{ e ->

                        }
                    lastAnalyzedTimestamp = currentTimestamp
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        textbox = findViewById<TextView>(R.id.view_text)

        viewFinder = findViewById<TextureView>(R.id.view_finder)
        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView

    private fun startCamera() {
        // TODO: Implement CameraX operations
        //Create configuration obj for the VF
        val analyzerConfig = ImageAnalysisConfig.Builder().apply{
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply{
            setAnalyzer(executor, ImgAnalyzer())
        }

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640,640))
        }.build()

        //build VF use case
        val preview = Preview(previewConfig)

        //Every time the VF is update, recompute layout
        preview.setOnPreviewOutputUpdateListener{
            //update surface texture need to remove and readd
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            //?????
            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }
        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, analyzerUseCase)
    }

    private fun updateTransform() {
        // TODO: Implement camera viewfinder transformations
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

//    private class ImageAnalyzer : ImageAnalysis.Analyzer{
//        private fun
//    }


//    /**
//     * Process result from permission request dialog box, has the request
//     * been granted? If yes, start Camera. Otherwise display a toast
//     */
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                viewFinder.post { startCamera() }
//            } else {
//                Toast.makeText(this,
//                    "Permissions not granted by the user.",
//                    Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    /**
//     * Check if all permission specified in the manifest have been granted
//     */
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(
//            baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }
//}
}
