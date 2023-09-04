package com.app.documentscanner.reviewrecipt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.app.documentscanner.MainActivity
import com.app.documentscanner.databinding.ActivityReviewDocumentBinding
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.File

class ReviewDocumentActivity : AppCompatActivity() {

    private lateinit var binding : ActivityReviewDocumentBinding
    lateinit var mPhotoViewAttacher: PhotoViewAttacher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initApp()
    }

    private fun initApp() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)


        val receiptPath = intent.getStringExtra(MainActivity.KEY_RECEIPT_PATH)
//        var receipt = AppUtils.getBitmap(receiptPath, this)
        val mReceiptFile = receiptPath?.let { File(it) }
        var receipt: Bitmap? = null
        if (mReceiptFile != null) {
            if (mReceiptFile.exists()) {
                receipt = BitmapFactory.decodeFile(mReceiptFile.absolutePath)
            }
        }
        val vto = binding.reviewReceiptIvReceipt.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.reviewReceiptIvReceipt.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                val width = binding.reviewReceiptIvReceipt.getMeasuredWidth()
                val height = binding.reviewReceiptIvReceipt.getMeasuredHeight()
                var newWidth = receipt!!.width
                if (receipt.width < width) {
                    newWidth = width
                }

                val newHeight = newWidth * height / width
                val newReceipt = Bitmap.createScaledBitmap(receipt, newWidth, newHeight, false)

                binding.reviewReceiptIvReceipt.setImageBitmap(newReceipt)
                mPhotoViewAttacher = PhotoViewAttacher(binding.reviewReceiptIvReceipt)
                mPhotoViewAttacher.scaleType = ImageView.ScaleType.CENTER_CROP

            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}