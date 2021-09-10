package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private val clickedString = resources.getString(R.string.button_name)
    private val loadingString = resources.getString(R.string.button_loading)
    private val completedString = resources.getString(R.string.download)
    private var text = completedString

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> {
                text = clickedString
            }
            ButtonState.Loading -> {
                text = loadingString
                val textRect = Rect()
                textPaint.getTextBounds(text, 0, text.length, textRect)
                circleRect.set(
                    widthSize / 2.0f + textRect.width() / 2.0f + textRect.height() * 0.5f,
                    heightSize / 2.0f - textRect.height() / 2.0f,
                    widthSize / 2.0f + textRect.width() / 2.0f + textRect.height() * 1.5f,
                    heightSize / 2.0f + textRect.height() / 2.0f
                )
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                text = completedString
                valueAnimator.end()
                valueAnimator.setCurrentFraction(0.0f)
            }
        }
    }

    private val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)

    private var completed : Float = 0.0f

    private var sweepAngle : Float = 0.0f

    private val valueAnimator = ValueAnimator.ofInt(0,100).apply {
        duration = 1000
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            completed = animatedFraction
            progressRect.right = widthSize * completed
            sweepAngle = 360.0f * completed
            super.invalidate()
        }
    }

    private val customBackgroundColor = attributes.getColor(
        R.styleable.LoadingButton_backgroundColor,
        ContextCompat.getColor(context, R.color.colorPrimaryDark)
    )

    private val customForegroundColor = attributes.getColor(
        R.styleable.LoadingButton_foregroundColor,
        ContextCompat.getColor(context, R.color.colorPrimary)
    )

    private val customCircleColor = attributes.getColor(
        R.styleable.LoadingButton_circleColor,
        ContextCompat.getColor(context, R.color.colorAccent)
    )

    private val customTextColor = attributes.getColor(
        R.styleable.LoadingButton_textColor,
        ContextCompat.getColor(context, R.color.white)
    )

    private var progressRect = RectF(0.0f, 0.0f, 0.0f, 0.0f)

    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = customForegroundColor
    }

    private var circleRect = RectF(0.0f, 0.0f, 0.0f, 0.0f)

    private val circlePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = customCircleColor
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        color = customTextColor
    }

    init {
        isClickable = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawColor(customBackgroundColor)
            if (buttonState == ButtonState.Loading) {
                it.drawRect(progressRect, progressPaint)
            }
            it.drawText(
                text,
                widthSize / 2.0f,
                // See https://stackoverflow.com/questions/11120392/android-center-text-on-canvas
                heightSize / 2.0f - ((textPaint.ascent() + textPaint.descent()) / 2f),
                textPaint)
            if (buttonState == ButtonState.Loading) {
                it.drawArc(circleRect, 270.0f, sweepAngle, true, circlePaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = w
        heightSize = h

        textPaint.textSize = heightSize / 3.0f

        progressRect.bottom = heightSize.toFloat()
        progressRect.right = textPaint.textSize
    }

    override fun performClick(): Boolean {
        super.performClick()

        invalidate()

        return true
    }

    fun isLoading(): Boolean {
        return buttonState == ButtonState.Loading
    }

    fun setState(state: ButtonState) {
        buttonState = state
    }
}
