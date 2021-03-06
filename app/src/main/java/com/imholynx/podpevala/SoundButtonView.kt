package com.imholynx.podpevala

import android.animation.RectEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

class SoundButtonView : View {

    lateinit var manager: AnimationManager

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }


    fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SoundButtonView, 0, 0)
            try {
                animationDuration = typedArray.getInt(R.styleable.SoundButtonView_animation_duration, 10000)
                centerColor = typedArray.getColor(R.styleable.SoundButtonView_center_color, Color.BLUE)
                roundColor = typedArray.getColor(R.styleable.SoundButtonView_round_color, Color.YELLOW)
                borderColor = typedArray.getColor(R.styleable.SoundButtonView_border_color, Color.CYAN)
                borderWidth = typedArray.getDimension(R.styleable.SoundButtonView_border_width, 4f)
                volume = typedArray.getFloat(R.styleable.SoundButtonView_volume,0f)
                circleRadius = typedArray.getFloat(R.styleable.SoundButtonView_circle_radius, 0.5f)
                icon  = typedArray.getDrawable(R.styleable.SoundButtonView_icon)
                manager = AnimationManager(animationDuration)
                } finally{
                typedArray.recycle()
            }
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animationDuration = 10000
    private var circleRadius = 0.5f
    private var centerColor = Color.CYAN
    private var roundColor = Color.YELLOW
    private var borderColor = Color.BLUE
    private var borderWidth = 4.0f
    private var volume:Float = 0.5f
    private var icon: Drawable? = null

    fun setVolume(value: Float)
    {
        volume = value
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRound(canvas)
        drawCircle(canvas)
        drawBorder(canvas)
        icon?.setBounds(50,50,50,50)
        icon?.draw(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        paint.color = centerColor
        paint.style = Paint.Style.FILL
        val size = measuredWidth
        val radius = (size / 2f)*circleRadius
        //val radius = size / 2f
        canvas.drawCircle(size / 2f, size / 2f, radius, paint)
    }

    private fun drawBorder(canvas: Canvas) {
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth

        val size = measuredWidth
        val min = (circleRadius*size + borderWidth)/2f
        val max = (size  - borderWidth)/2f
        val borderRadius = min + manager.getPosition(volume)*(max-min)

        val circle = RectF(size/2f - borderRadius,size/2f - borderRadius, size/2f+borderRadius,size/2f + borderRadius)
        canvas.drawArc(circle,0f,30f,false,paint)
        canvas.drawArc(circle,60f,30f,false,paint)
        canvas.drawArc(circle,120f,30f,false,paint)
        canvas.drawArc(circle,180f,30f,false,paint)
        canvas.drawArc(circle,240f,30f,false,paint)
        canvas.drawArc(circle,300f,30f,false,paint)
    }

    private fun drawRound(canvas: Canvas){
        paint.color = roundColor
        paint.style = Paint.Style.FILL
        paint.strokeWidth = borderWidth

        val size = measuredWidth
        val min = (circleRadius*size + borderWidth)/2f
        val max = (size  - borderWidth)/2f
        val borderRadius = min + volume*(max-min)

        canvas.drawCircle(size / 2f, size / 2f, borderRadius-borderWidth, paint)
    }
}