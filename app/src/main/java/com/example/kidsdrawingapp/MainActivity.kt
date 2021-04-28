package com.example.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import com.example.kidsdrawingapp.databinding.DialogBrishSizeBinding
import com.example.kidsdrawingapp.databinding.DialogBrushSizeBinding
import com.example.kidsdrawingapp.databinding.DialogeBrishSizeBinding

class MainActivity : AppCompatActivity() {

    private lateinit var bindingActivity: ActivityMainBinding
    private lateinit var bindingBrush: DialogBrushSizeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindingActivity = ActivityMainBinding.inflate(layoutInflater)
        val viewActivity = bindingActivity.root
        setContentView(viewActivity)

        bindingActivity.drawingView.setSizeForBrush(20.toFloat())
    }
    private fun showBrushSizeChooseDialog() {
        bindingBrush = DialogBrushSizeBinding.inflate(layoutInflater)
        val viewBrush = bindingBrush.root
        setContentView(viewBrush)

        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size :")
            // надо чтобы при запуске приложения можно было менять размер кисти.
            // код в функции ниже скопирован из примера где еще плагин kotlin extensions был
            // и я начинаю плавать что тут нужно, что нет и что на что менять
        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        })
        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        })

        val largeBtn = brushDialog.ib_large_brush
        largeBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        })
        brushDialog.show()
    }
}