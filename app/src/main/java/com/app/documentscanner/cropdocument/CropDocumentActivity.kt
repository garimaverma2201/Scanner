package com.app.documentscanner.cropdocument

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.app.documentscanner.AppUtils
import com.app.documentscanner.MainActivity
import com.app.documentscanner.databinding.ActivityCropDocumentBinding
import com.app.documentscanner.opencv.OpenCVUtils
import com.app.documentscanner.reviewrecipt.ReviewDocumentActivity
import org.opencv.android.OpenCVLoader
import java.io.IOException

class CropDocumentActivity : AppCompatActivity(){

    init {
        //here goes static initializer code
        if (!OpenCVLoader.initDebug()) {
            Log.e("Scan", "OpenCVLoader.initDebug() = FALSE")
        } else {
            Log.e("Scan", "OpenCVLoader.initDebug() = TRUE")
        }
    }

    private lateinit var binding : ActivityCropDocumentBinding
    private var original: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        initialization()
    }

    private var bitmap: Bitmap?
        get() {
            val uri = uri
            try {
                var bitmap = AppUtils.getBitmap(uri, this)
                bitmap!!.setDensity(Bitmap.DENSITY_NONE)
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    bitmap = OpenCVUtils.rotate(bitmap, 90)
                }
//                this.contentResolver.delete(uri!!, null, null)
                return bitmap
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
        set(original) {
            val scaledBitmap = original?.let {
                Bitmap.createScaledBitmap(
                    it, binding.sourceImageView.width,
                    binding.sourceImageView.height, false)
            };
            binding.sourceImageView.setImageBitmap(scaledBitmap)
            val pointFs = scaledBitmap?.let { OpenCVUtils.getEdgePoints(it, binding.polygonView!!) }
            binding.polygonView!!.points = pointFs!!
            binding.polygonView!!.visibility = View.VISIBLE

            val layoutParams = FrameLayout.LayoutParams(binding.sourceImageView.width, binding.sourceImageView.height)
            layoutParams.gravity = Gravity.CENTER
            binding.polygonView!!.layoutParams = layoutParams
        }

    private val uri: Uri?
        get() = intent.getParcelableExtra<Uri>(MainActivity.KEY_RECEIPT_PATH)

    private fun initialization() {
        binding.sourceFrame!!.post {
            original = bitmap
            if (original != null) {
                bitmap = original
            }
        }

        binding.scanButton.setOnClickListener {
            binding.sourceFrame!!.post {
                val croppedBitmap = OpenCVUtils.cropReceiptByFourPoints(
                    bitmap!!,
                    binding.polygonView!!.getListPoint(),
                    binding.sourceImageView.width,
                    binding.sourceImageView.height
                )
                val savedPath = AppUtils.saveBitmapToFile(croppedBitmap!!)
                val intent = Intent(this, ReviewDocumentActivity::class.java)
                intent.putExtra(MainActivity.KEY_RECEIPT_PATH, savedPath)
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}