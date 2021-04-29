package com.example.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import com.example.kidsdrawingapp.databinding.DialogBrushSizeBinding


class MainActivity : AppCompatActivity() {

    private lateinit var bindingActivity: ActivityMainBinding
    private lateinit var bindingBrush: DialogBrushSizeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingActivity.root)

        bindingActivity.drawingView.setSizeForBrush(20.toFloat())

        val btnBrush = bindingActivity.ibBrush
        btnBrush.setOnClickListener{
            showBrushSizeChooseDialog()
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
}