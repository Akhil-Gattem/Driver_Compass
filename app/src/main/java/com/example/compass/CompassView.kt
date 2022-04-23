package com.example.compass

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import kotlin.math.cos
import kotlin.math.sin


class CompassView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val leftRect = 0.0f
    private val topRect = 0.0f
    private val percent50 = 0.50f
    private var viewHeight = 0.0f
    private var viewWidth = 0.0f
    private var bitmapWidth = 0.0f
    private var bitmapHeight = 0.0f
    private var centerOfVIew = 0.0f
    private var outerCircleRadius = 0.0f
    private var innerCircleRadius = 0.0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var outerRingBmp =
        Bitmap.createBitmap(EDITOR_BMP_WIDTH, EDITOR_BMP_HEIGHT, Bitmap.Config.ARGB_8888)
    private var innerDialBmp =
        Bitmap.createBitmap(EDITOR_BMP_WIDTH, EDITOR_BMP_HEIGHT, Bitmap.Config.ARGB_8888)
    private lateinit var poiBmp: Bitmap
    private lateinit var canvasBitmap: Canvas

    var rotateAngle = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.style = Paint.Style.STROKE
        val xCenterBmp = (viewWidth - bitmapWidth) * percent50
        val yCenterBmp = (viewHeight - bitmapHeight) * percent50

        drawOuterRotatingRing()
        canvas?.save()
        canvas?.rotate(rotateAngle, viewWidth * percent50, viewHeight * percent50)
        canvas?.drawBitmap(outerRingBmp, xCenterBmp, yCenterBmp, paint)
        canvas?.restore()

        drawInnerDialWithArcs()
        canvas?.drawBitmap(innerDialBmp, xCenterBmp, yCenterBmp, paint)
    }

    private fun drawFourSideArcs(innerDialCanvas: Canvas) {
        paint.strokeWidth = 1f
        val sweepAngle = 40f
        val rect = RectF(
            bitmapWidth * 0.01f, bitmapHeight * 0.01f,
            bitmapWidth * 0.99f, bitmapHeight * 0.99f
        )

        val anglesArcList = floatArrayOf(160f, 250f, 340f, 70f)
        anglesArcList.forEach {
            innerDialCanvas.drawArc(rect, it, sweepAngle, false, paint)
        }
    }

    private fun drawOuterRotatingRing() {
        paint.strokeWidth = 2f
        canvasBitmap.drawCircle(
            bitmapWidth * percent50,
            bitmapHeight * percent50,
            outerCircleRadius,
            paint
        )
        drawTexts()

    }

    private fun drawInnerDialWithArcs() {
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        val innerDialCanvas = Canvas(innerDialBmp)
        innerDialCanvas.drawCircle(
            bitmapWidth * percent50,
            bitmapHeight * percent50,
            innerCircleRadius,
            paint
        )
        drawFixedArrow(innerDialCanvas)
        drawPOIHeading(innerDialCanvas)
        drawFourSideArcs(innerDialCanvas)
        drawDashes(innerDialCanvas)
    }

    private fun drawFixedArrow(innerDialCanvas: Canvas) {
        val path = Path()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        path.moveTo(bitmapWidth * 0.47f, bitmapHeight * 0.25f)
        path.lineTo(bitmapWidth * percent50, bitmapHeight * 0.22f)
        path.lineTo(bitmapWidth * 0.53f, bitmapHeight * 0.25f)
        innerDialCanvas.drawPath(path, paint)
    }

    private fun drawPOIHeading(innerDialCanvas: Canvas) {
        paint.strokeWidth = 1f
        val pointFInner = getXYFromCircle(0f, innerCircleRadius)
        val pointFOuter = getXYFromCircle(0f, outerCircleRadius)
        val rectF = RectF(
            pointFInner.x*1.02f,
            pointFInner.y * 0.87f,
            pointFOuter.x * 0.99f,
            pointFOuter.y * 1.13f
        )
        innerDialCanvas.drawBitmap(poiBmp, null, rectF, paint)
    }

    private fun drawTexts() {
        paint.style = Paint.Style.FILL
        paint.textSize = centerOfVIew * 0.25f
        val radius = (outerCircleRadius + innerCircleRadius) * percent50
        drawNEWSText(135f, "E", getXYFromCircle(45f, radius))
        drawNEWSText(225f, "S", getXYFromCircle(135f, radius))
        drawNEWSText(315f, "W", getXYFromCircle(225f, radius))
        drawNEWSText(45f, "N", getXYFromCircle(315f, radius))
    }
    private fun drawDashes(innerDialCanvas: Canvas) {
        val paths = Path()
        paths.moveTo(bitmapWidth *0.325f, bitmapHeight * 0.59f)
        paths.quadTo(bitmapWidth * percent50, bitmapHeight * 0.59f ,bitmapWidth * 0.67f, bitmapHeight * 0.59f)
        val dashPaint = Paint()
        dashPaint.setARGB(255, 0, 0, 0)
        dashPaint.style = Paint.Style.STROKE
        dashPaint.strokeWidth = 4f
        dashPaint.pathEffect = DashPathEffect(floatArrayOf(0f, 3f, 9f, 12f, 28f), 0f)
        innerDialCanvas.drawPath(paths, dashPaint)
    }

    private fun drawNEWSText(angleText: Float, text: String, pointF: PointF) {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val height = bounds.height()
        val width = bounds.width()
        canvasBitmap.save()
        canvasBitmap.rotate(angleText, pointF.x, pointF.y)
        canvasBitmap.drawText(
            text,
            pointF.x - (width * percent50),
            pointF.y + (height * percent50),
            paint
        )
        canvasBitmap.restore()
    }

    private fun getXYFromCircle(angle: Float, radius: Float): PointF {
        val angleRadians = angle * (Math.PI / STRAIGHT_ANGLE)
        val x = (radius * cos(angleRadians) + centerOfVIew).toFloat()
        val y = (radius * sin(angleRadians) + centerOfVIew).toFloat()
        return PointF(x, y)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldheight)
        viewHeight = height.toFloat()
        viewWidth = width.toFloat()
        val viewMinSize = if (viewWidth > viewHeight) viewHeight else viewWidth
        outerRingBmp = scaleBitmapUpDown(outerRingBmp, viewMinSize)
        innerDialBmp = scaleBitmapUpDown(innerDialBmp, viewMinSize)
        bitmapWidth = outerRingBmp.width.toFloat()
        bitmapHeight = outerRingBmp.height.toFloat()
        centerOfVIew = viewMinSize * percent50
        outerCircleRadius = centerOfVIew * 0.93f
        innerCircleRadius = centerOfVIew * 0.60f
        canvasBitmap = Canvas(outerRingBmp)
        poiBmp = getBitmapFromVectorDrawable()
    }

    private fun scaleBitmapUpDown(scaleBitmap: Bitmap, distance: Float): Bitmap {
        val matrix = Matrix()
        matrix.setRectToRect(
            RectF(
                leftRect, topRect, scaleBitmap.width.toFloat(),
                scaleBitmap.height.toFloat()
            ),
            RectF(leftRect, topRect, distance, distance),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(
            scaleBitmap,
            leftRect.toInt(),
            topRect.toInt(),
            scaleBitmap.width,
            scaleBitmap.height,
            matrix,
            true
        )
    }

    private fun getBitmapFromVectorDrawable(): Bitmap {
        val drawable =
            (AppCompatResources.getDrawable(context, R.drawable.img_nav_bearing_gmc_rect))
        val bitmap =
            Bitmap.createBitmap(viewWidth.toInt(), viewHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable!!.setBounds(leftRect.toInt(), topRect.toInt(), bitmap.width, bitmap.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        const val EDITOR_BMP_HEIGHT = 100
        const val EDITOR_BMP_WIDTH = 100
        const val STRAIGHT_ANGLE = 180
    }
}