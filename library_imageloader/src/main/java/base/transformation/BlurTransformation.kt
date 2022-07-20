package base.transformation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.renderscript.RSRuntimeException
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import java.security.MessageDigest


/**
 * Project Name: wanandroid
 * File Name:    BlurTransformation.java
 * ClassName:    BlurTransformation
 *
 * Description: 模糊变换
 *
 * @author 唐晓辉
 * @date 2021年09月16日 16:15
 *
 */
class BlurTransformation @JvmOverloads constructor(private val radius: Int = MAX_RADIUS, private val sampling: Int = DEFAULT_DOWN_SAMPLING) : BitmapTransformation() {
    override fun transform(context: Context, pool: BitmapPool,
                           toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val scaledWidth = width / sampling
        val scaledHeight = height / sampling
        var bitmap = pool[scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888]
        setCanvasBitmapDensity(toTransform, bitmap)
        val canvas = Canvas(bitmap)
        canvas.scale(1 / sampling.toFloat(), 1 / sampling.toFloat())
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        try {
            bitmap = RSBlur.blur(context, bitmap, radius)
        } catch (e: RSRuntimeException) {
            val blur = FastBlur.blur(bitmap, radius, true)
            if (null != blur) {
                bitmap = blur
            }
        }
        return bitmap
    }

    override fun toString(): String {
        return "BlurTransformation(radius=$radius, sampling=$sampling)"
    }

    override fun equals(other: Any?): Boolean {
        return other is BlurTransformation && other.radius == radius && other.sampling == sampling
    }

    override fun hashCode(): Int {
        return ID.hashCode() + radius * 1000 + sampling * 10
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius + sampling).toByteArray(Key.CHARSET))
    }

    companion object {
        private const val ID = "glide.transformations.BlurTransformation"
        private const val MAX_RADIUS = 25
        private const val DEFAULT_DOWN_SAMPLING = 1
    }
}