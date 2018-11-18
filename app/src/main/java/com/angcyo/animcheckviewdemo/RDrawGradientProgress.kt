package com.angcyo.animcheckviewdemo

import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.IntDef
import android.util.AttributeSet
import android.view.View

/**
 * Created by angcyo on 2018/11/17 23:14
 */
class RDrawGradientProgress(view: View, attributeSet: AttributeSet? = null) : BaseDraw(view, attributeSet) {

    companion object {
        /*什么都不画*/
        const val STATUS_NONE = 0
        /*绘制渐变进度*/
        const val STATUS_GRADIENT = 1
        /*完成, 无渐变的进度. 也不会绘制小ico*/
        const val STATUS_FINISH = 2
        /*STATUS_FINISH 一致, 但是会绘制小ico*/
        const val STATUS_SUCCEED = 3
    }

    /*进度的宽度*/
    var progressWidth = 4 * density()

    /*绘制背景*/
    var drawBg = true

    /*背景圈的颜色*/
    var bgProgressColor = Color.parseColor("#454848")

    /*进度渐变开始的颜色*/
    var gradientStartColor =
        Color.parseColor("#403E6476") //Color.TRANSPARENT  //Color.RED ////Color.parseColor("#3E6476")
    /*进度渐变结束的颜色, 也是非渐变状态下进度条的颜色*/
    var gradientEndColor = Color.parseColor("#4D95D8") //Color.GREEN

    private var gradientRectF = RectF()

    private var gradientBitmap: Bitmap? = null

    /*进度状态*/
    @ProgressStatus
    var progressStatus = STATUS_NONE
        set(value) {
            if (field != value) {
                succeedDrawableAlpha = 0
            }
            field = value
            postInvalidateOnAnimation()
        }

    /*成功后, 绘制的ico, 不受padding 的限制.
    可以用padding 来控制ico和进度条之间的距离*/
    var succeedDrawable: Drawable? = null
        set(value) {
            field = value
            if (progressStatus == STATUS_SUCCEED) {
                postInvalidateOnAnimation()
            }
        }

    /*距离微调, 相对于右下角的偏移*/
    var succeedDrawableOffsetX = -2 * density().toInt()
        set(value) {
            field = value
            if (progressStatus == STATUS_SUCCEED) {
                postInvalidateOnAnimation()
            }
        }
    var succeedDrawableOffsetY = -2 * density().toInt()
        set(value) {
            field = value
            if (progressStatus == STATUS_SUCCEED) {
                postInvalidateOnAnimation()
            }
        }

    //透明度动画
    private var succeedDrawableAlpha = 0
        set(value) {
            if (value >= 255) {
                field = 255
            } else {
                field = value
                if (progressStatus == STATUS_SUCCEED) {
                    postInvalidateOnAnimation()
                }
            }
        }

    /*可以用来控制开始时的旋转角度*/
    var startAngle = 90f
        set(value) {
            field = if (value >= 360) {
                0f
            } else {
                value
            }
            if (progressStatus == STATUS_GRADIENT) {
                postInvalidateOnAnimation()
            }
        }

    /*旋转速率, 值越大转的越快, 负数反向旋转*/
    var rotateSpeed = 6

    init {
        initAttribute(attributeSet)
    }

    override fun initAttribute(attr: AttributeSet?) {
        val array = obtainStyledAttributes(attr, R.styleable.RDrawGradientProgress)
        succeedDrawable = array.getDrawable(R.styleable.RDrawGradientProgress_r_anim_check_succeed_ico)
        succeedDrawableOffsetX = array.getDimensionPixelOffset(
            R.styleable.RDrawGradientProgress_r_anim_check_succeed_offset_x,
            succeedDrawableOffsetX
        )
        succeedDrawableOffsetY = array.getDimensionPixelOffset(
            R.styleable.RDrawGradientProgress_r_anim_check_succeed_offset_y,
            succeedDrawableOffsetY
        )
        progressWidth = array.getDimensionPixelOffset(
            R.styleable.RDrawGradientProgress_r_anim_check_progress_width,
            progressWidth.toInt()
        ).toFloat()
        progressStatus = array.getInt(R.styleable.RDrawGradientProgress_r_anim_check_status, progressStatus)
        startAngle =
                array.getInt(R.styleable.RDrawGradientProgress_r_anim_check_start_angle, startAngle.toInt()).toFloat()
        rotateSpeed = array.getInt(R.styleable.RDrawGradientProgress_r_anim_check_rotate_speed, rotateSpeed)

        drawBg = array.getBoolean(R.styleable.AnimCheckView_r_anim_check_draw_bg, drawBg)
        bgProgressColor = array.getColor(R.styleable.AnimCheckView_r_anim_check_bg_color, bgProgressColor)
        gradientStartColor =
                array.getColor(R.styleable.AnimCheckView_r_anim_check_gradient_start_color, gradientStartColor)
        gradientEndColor = array.getColor(R.styleable.AnimCheckView_r_anim_check_gradient_end_color, gradientEndColor)

        array.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        createGradientBitmap()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val maxDrawSquare = maxDrawSquareF()

        mBasePaint.strokeWidth = progressWidth
        mBasePaint.style = Paint.Style.STROKE
        mBasePaint.shader = null

        if (drawBg) {
            mBasePaint.color = bgProgressColor

            canvas.drawCircle(
                maxDrawSquare.centerX(),
                maxDrawSquare.centerY(), maxDrawSquare.width() / 2 - progressWidth / 2, mBasePaint
            )
        }

        if (progressStatus == STATUS_NONE) {
            if (isInEditMode) {
                progressStatus = STATUS_SUCCEED
            }
        } else if (progressStatus == STATUS_GRADIENT) {
            gradientRectF.set(
                -maxDrawSquare.width() / 2, -maxDrawSquare.height() / 2,
                maxDrawSquare.width() / 2, maxDrawSquare.height() / 2
            )
            canvas.save()
            canvas.translate(maxDrawSquare.centerX(), maxDrawSquare.centerY())
            canvas.rotate(startAngle)
            canvas.drawBitmap(gradientBitmap, null, gradientRectF, mBasePaint)
            canvas.restore()

            //不需要自动执行动画, 可以注释此行代码
            startAngle += rotateSpeed
        } else if (progressStatus == STATUS_FINISH || progressStatus == STATUS_SUCCEED) {
            mBasePaint.color = gradientEndColor

            canvas.drawCircle(
                maxDrawSquare.centerX(),
                maxDrawSquare.centerY(), maxDrawSquare.width() / 2 - progressWidth / 2, mBasePaint
            )

            if (progressStatus == STATUS_SUCCEED) {
                //绘制小ico
                succeedDrawable?.let {
                    if (isInEditMode) {
                        it.alpha = 255
                    } else {
                        it.alpha = succeedDrawableAlpha
                    }
                    it.setBounds(
                        viewWidth - it.intrinsicWidth + succeedDrawableOffsetX,
                        viewHeight - it.intrinsicHeight + succeedDrawableOffsetY,
                        viewWidth + succeedDrawableOffsetX,
                        viewHeight + succeedDrawableOffsetY
                    )
                    it.draw(canvas)
                    succeedDrawableAlpha += 10 //动画速率
                }
            }
        }
    }

    /**创建渐变进度圆弧Bitmap*/
    fun createGradientBitmap() {
        gradientBitmap?.recycle()

        val maxDrawSquare = maxDrawSquareF()

        gradientBitmap = Bitmap.createBitmap(
            maxDrawSquare.width().toInt(),
            maxDrawSquare.height().toInt(),
            Bitmap.Config.ARGB_4444
        )
        val canvas = Canvas(gradientBitmap)

        mBasePaint.strokeWidth = progressWidth
        mBasePaint.style = Paint.Style.STROKE

        /*为了预览能成功, 尽量使用3个颜色*/
        mBasePaint.shader = SweepGradient(
            maxDrawSquare.width() / 2, maxDrawSquare.height() / 2,
            intArrayOf(gradientStartColor, gradientEndColor, Color.TRANSPARENT), floatArrayOf(0f, 0.5f, 1f)
        )

        gradientRectF.set(
            progressWidth / 2, progressWidth / 2,
            maxDrawSquare.width() - progressWidth / 2, maxDrawSquare.height() - progressWidth / 2
        )

        canvas.drawArc(gradientRectF, 0f, 180f, false, mBasePaint)
    }

    @IntDef(STATUS_NONE, STATUS_GRADIENT, STATUS_FINISH, STATUS_SUCCEED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ProgressStatus
}