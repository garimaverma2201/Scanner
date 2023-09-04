package com.app.documentscanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v4.content.PermissionChecker
import android.util.Log
import com.app.documentscanner.cropdocument.CropDocumentActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.app.documentscanner.databinding.ActivityMainBinding
import com.app.documentscanner.reviewrecipt.ReviewDocumentActivity

class MainActivity : AppCompatActivity() {

    private var TAG : String = MainActivity::class.java.simpleName
    private lateinit var binding : ActivityMainBinding

    private val REQUEST_PERMISSION_CAMERA = 101
    private val REQUEST_RECEIPT_CAPTURE = 100
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 102
    private val REQUEST_PICK_IMAGE = 103
    private var fileUri: Uri? = null

    companion object {
        val KEY_RECEIPT_PATH = "RECEIPT_PATH"
        val IMAGE_PATH = Environment
            .getExternalStorageDirectory().path + "/scanSample"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickListener()
    }

    private fun initClickListener() {
        binding.tvCaptureReceipt.setOnClickListener {
            checkCameraPermission()
        }

        binding.tvChooseGallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE)
        }
    }

    private fun startScanCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        val isDirectoryCreated = file.parentFile?.mkdirs()
        Log.d(TAG, "openCamera: isDirectoryCreated: $isDirectoryCreated")
        val tempFileUri = FileProvider.getUriForFile(applicationContext,
            "com.scanlibrary.provider", // As defined in Manifest
            file)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
        startActivityForResult(cameraIntent, REQUEST_RECEIPT_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_RECEIPT_CAPTURE && resultCode == Activity.RESULT_OK) {
            startCropActivity(fileUri!!)
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data!!.getData() as Uri
            startCropActivity(imageUri)
        }
    }

    private fun checkCameraPermission() {
        if (!(PermissionChecker.checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            val permissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CAMERA)
        } else {
            checkExternalPermission()
        }
    }

    private fun checkExternalPermission() {
        if (!(PermissionChecker.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)
        } else {
            startScanCamera()
        }
    }

    fun startReviewReceiptActivity(receiptPath: Uri) {
        val intent = Intent(this, ReviewDocumentActivity::class.java)
        intent.putExtra(KEY_RECEIPT_PATH, receiptPath)
        startActivity(intent)

    }

    fun startCropActivity(receiptPath: Uri) {
        val intent = Intent(this, CropDocumentActivity::class.java)
        intent.putExtra(KEY_RECEIPT_PATH, receiptPath)
        startActivity(intent)
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        clearTempImages()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file = File(IMAGE_PATH, "IMG_" + timeStamp +
                ".jpg")
        fileUri = Uri.fromFile(file)
        return file
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                val grantedExternal = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (grantedExternal) {
                    // process our code
                    startScanCamera()
                }
            }
            REQUEST_PERMISSION_CAMERA -> {
                val grantedCamera = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (grantedCamera) {
                    checkExternalPermission()
                }
            }
            else ->
                //                mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun clearTempImages() {
        try {
            val tempFolder = File(IMAGE_PATH)
            for (f in tempFolder.listFiles()!!)
                f.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

