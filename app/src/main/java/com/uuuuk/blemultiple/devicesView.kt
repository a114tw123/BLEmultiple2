package com.uuuuk.blemultiple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class devicesView :ConstraintLayout{
//    var listener:OnTouchListener?=null
    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle
    ) {
        init(attrs)
    }
    private fun init(attrs: AttributeSet?){
        val view= View.inflate(context,R.layout.sample_devices_view,this)
        LayoutInflater.from(context).inflate(R.layout.sample_devices_view,this)
        val attributs=context.theme.obtainStyledAttributes(attrs,
            R.styleable.devicesView, 0, 0)
        try {

        }finally {
            attributs.recycle()
        }
    }
}