package com.example.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import com.example.kidsdrawingapp.databinding.DialogBrushSizeBinding
import java.util.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var bindingActivity: ActivityMainBinding
    private lateinit var bindingBrush: DialogBrushSizeBinding

    private val myCoroutineScope = CoroutineScope(Dispatchers.Main) // ivan
    private lateinit var mProgressDialog: Dialog  // ivan

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingActivity.root)

        bindingActivity.drawingView.setSizeForBrush(20.toFloat())
        mImageButtonCurrentPaint = bindingActivity.llPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_press)
        )

        bindingActivity.ibBrush.setOnClickListener{
            showBrushSizeChooseDialog()
        }
        bindingActivity.ibGallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)
            }else{
                requestStoragePermission()
            }
        }
        bindingActivity.ibUndo.setOnClickListener{
            bindingActivity.drawingView.onClickUndo()
        }
        bindingActivity.ibSave.setOnClickListener {
            if (isReadStorageAllowed()) {
                myCoroutineScope.launch {
                    showProgressDialog()
                    bitmapAsyncTask(getBitmapFromView(bindingActivity.flDrawingViewContainer))
                    cancelProgressDialog()
                }
            }else{
                requestStoragePermission()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,"permission granted, now you can use storage",
                    Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(this@MainActivity,"oops you just denied the permission",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == GALLERY) {

                try {

                    if (data!!.data != null) {
                        bindingActivity.ivBackground.visibility = View.VISIBLE
                        bindingActivity.ivBackground.setImageURI(data.data)

                    }else{
                        Toast.makeText(this@MainActivity,"error in parsing image", Toast.LENGTH_LONG).show()

                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }

    }

    private fun showBrushSizeChooseDialog() {
        val brushDialog = Dialog(this)
        bindingBrush = DialogBrushSizeBinding.inflate(layoutInflater)
        brushDialog.setContentView(bindingBrush.root)

        brushDialog.setTitle("Brush size:")

        val smallBtn = bindingBrush.ibSmallBrush
        smallBtn.setOnClickListener{
            bindingActivity.drawingView.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = bindingBrush.ibMediumBrush
        mediumBtn.setOnClickListener{
            bindingActivity.drawingView.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn = bindingBrush.ibLargeBrush
        largeBtn.setOnClickListener {
            bindingActivity.drawingView.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }
    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            bindingActivity.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_press)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint = view
        }
    }

    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"Need permission to add a background", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }



    private fun isReadStorageAllowed():Boolean{
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView( view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width,
            view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun bitmapAsyncTask(mBitmap: Bitmap){  // ivan fun
        var result = withContext(Dispatchers.IO) {
            var result = ""

            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                        externalCacheDir!!.absoluteFile.toString()
                                + File.separator + "KidDrawingApp_"
                                + System.currentTimeMillis() / 1000 + ".png"
                    )

                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            withContext(Dispatchers.Main) {
                if (result.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "File saved successfully :$result", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Something went wrong while saving the file.", Toast.LENGTH_LONG).show()
                }
            }

            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null){
                path, uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"

                startActivity(
                    Intent.createChooser(
                        shareIntent, "Share"
                    )
                )
            }
        }

    }

    private fun showProgressDialog() {
        mProgressDialog = Dialog(this@MainActivity)
        mProgressDialog.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog.show()
    }

    private fun cancelProgressDialog() {
        mProgressDialog.dismiss()
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}