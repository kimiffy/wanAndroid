import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import base.CornerType
import base.ImageLoaderGlobalConfig
import base.ScaleType
import base.Utils
import base.transformation.BlurTransformation
import base.transformation.CropCircleWithBorderTransformation
import base.transformation.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.kimiffy.library_imageloader.R
import java.io.File
import java.util.*


/**
 * Project Name: wanandroid
 * File Name:    ImageLoader.java
 * ClassName:    ImageLoader
 *
 * Description: glide 图片加载器
 *
 * @author  kimiffy
 * @date    2021年08月31日 14:59
 *
 */
class ImageLoader() {

    private var context: Context? = null

    private var loadUrl: String? = null

    private var loadUri: Uri? = null

    private var loadFile: File? = null

    private var loadResourceId: Int? = null

    private var loadDrawable: Drawable? = null

    private var loadBitmap: Bitmap? = null

    private var imageView: ImageView? = null

    //图片每个圆角的大小
    private var roundRadius = 0

    //圆角类型  四个角都是圆角
    private var cornerType = CornerType.ALL

    //高斯模糊值, 值越大模糊效果越大 (0~25)
    private var blurValue = 0

    //过渡图
    private var placeHolder = 0

    //过渡图
    private var placeHolderDrawable: Drawable? = null

    //错误图
    private var errorImage = 0

    //是否使用淡入淡出过渡动画
    private var withCrossFade = false

    //剪裁类型 CenterInside  CenterCrop  fitCenter
    private var scaleType: ScaleType? = null

    //是否将图片剪切为圆形
    private var isCircle = false

    //图片类型  默认drawable
    private var asImageType = 4

    //指定加载静态图片
    private val asBitmap = 1

    //指定加载动态图片
    private val asGif = 2

    private val asFile = 3

    private val asDrawable = 4

    //是否是带边框圆形图片
    private var withCircleBorder = false

    //圆形图片边框大小
    private var borderSize = 0

    //圆形图片边框颜色
    private var borderColor = Color.BLACK

    //变换
    private var transformations: Transformation<Bitmap?>? = null


    //缩略图比例 (0~1之间)
    private var sizeMultiplier = 1f

    //是否支持只在WIFI下才加载图片
    private var wifiLoad = false

    //优先级
    private var priority = Priority.NORMAL

    /**
     * 默认需要缓存
     */
    private val diskCacheAble = true

    /**
     * 是否只缓存原图
     */
    private val isOnlyCacheSource = false

    /**
     * 默认内存缓存
     */
    private val memoryCacheAble = true

    /**
     * 是否禁止gif动画
     */
    private var dontAnimate = false


    private constructor(context: Context) : this() {
        this.context = context
    }


    companion object {

        @JvmStatic
        fun with(context: Context): ImageLoader {
            return ImageLoader(context)
        }
    }

    /**
     * 清除内存缓存,需要在主线程执行
     */
    fun clearMemoryCache() {
        context?.let { Glide.get(it).clearMemory() }
    }

    /**
     * 清除磁盘缓存,需要在子线程执行
     */
    fun clearDiskCache() {
        context?.let { Glide.get(it).clearDiskCache() }
    }


    fun load(url: String?) = apply {
        this.loadUrl = url
    }

    fun load(uri: Uri?) = apply {
        this.loadUri = uri
    }

    fun load(file: File?) = apply {
        this.loadFile = file
    }

    fun load(@RawRes @DrawableRes resourceId: Int?) = apply {
        this.loadResourceId = resourceId
    }

    fun load(bitmap: Bitmap?) = apply {
        this.loadBitmap = bitmap
    }

    fun load(drawable: Drawable?) = apply {
        this.loadDrawable = drawable
    }

    /**
     * 加载静态图片
     */
    fun asBitMap() = apply {
        this.asImageType = this.asBitmap
    }

    /**
     * 加载动态图片
     */
    fun asGif() = apply {
        this.asImageType = this.asGif
    }

    fun asFile() = apply {
        this.asImageType = this.asFile
    }

    /**
     * 占位图
     */
    fun placeHolder(@DrawableRes resourceId: Int) = apply {
        this.placeHolder = resourceId
    }

    /**
     * 占位图
     */
    fun placeHolder(placeHolderDrawable: Drawable) = apply {
        this.placeHolderDrawable = placeHolderDrawable
    }

    /**
     * 错误图
     */
    fun error(@DrawableRes resourceId: Int) = apply {
        this.errorImage = resourceId
    }

    /**
     * 圆形图片
     */
    fun circle() = apply {
        this.isCircle = true
    }

    /**
     * 带边框圆形图片
     */
    fun circleWithBorder(borderSize: Int = 4, @ColorInt borderColor: Int = Color.BLACK) = apply {
        this.withCircleBorder = true
        this.borderSize = borderSize
        this.borderColor = borderColor
    }

    /**
     * 图片圆角
     */
    fun roundCorner(roundRadius: Int) = apply {
        this.roundRadius = roundRadius
    }

    /**
     * 图片圆角,部分圆角类型
     */
    fun roundCorner(roundRadius: Int, cornerType: CornerType) = apply {
        this.roundRadius = roundRadius
        this.cornerType = cornerType
    }

    /**
     * 模糊变换
     */
    fun blur(blurValue: Int) = apply {
        this.blurValue = blurValue
    }

    /**
     * 淡入淡出
     */
    fun withCrossFade() = apply {
        this.withCrossFade = true
    }

    /**
     * 图片缩放类型
     */
    fun scale(scaleType: ScaleType) = apply {
        this.scaleType = scaleType
    }

    /**
     * 变换
     */
    fun transform(transformations: Transformation<Bitmap?>) = apply {
        this.transformations = transformations
    }

    /**
     * 支持只在wifi下加载
     */
    fun wifiLoad() = apply {
        this.wifiLoad = true
    }

    /**
     * 缩略图  取值: 0f ~ 1f
     */
    fun thumbnail(sizeMultiplier: Float) = apply {
        this.sizeMultiplier = sizeMultiplier
    }

    /**
     * 优先级
     */
    fun priority(priority: Priority) = apply {
        this.priority = priority
    }

    /**
     * 禁止gif动画
     */
    fun dontAnimate(dontAnimate: Boolean) = apply {
        this.dontAnimate = dontAnimate
    }


    fun into(imageView: ImageView?): ImageLoader {
        if (imageView == null) {
            return this
        }
        var requestBuilder: RequestBuilder<*>? = null
        try {
            requestBuilder = requestPrepare()
        } catch (e: Throwable) {
        }
        if (requestBuilder == null) {
            if (errorImage > 0) {
                imageView.setImageResource(errorImage)
            } else if (placeHolder > 0) {
                imageView.setImageResource(placeHolder)
            } else if (placeHolderDrawable != null) {
                imageView.setImageDrawable(placeHolderDrawable)
            }
        } else {
            requestBuilder.into(imageView)
        }
        return this
    }


    @SuppressLint("CheckResult")
    private fun requestPrepare(): RequestBuilder<*>? {

        val requestBuilder: RequestBuilder<*> = (createRequestBuilder() ?: return null)

        val requestOptions = RequestOptions()
        if (diskCacheAble) {
            if (isOnlyCacheSource) {
                requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA)
            } else {
                requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
            }
        } else {
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE).signature(ObjectKey(UUID.randomUUID().toString()))
        }

        requestOptions.skipMemoryCache(!memoryCacheAble)

        //过渡图
        if (placeHolder != 0) {
            requestOptions.placeholder(placeHolder)
        }
        if (placeHolderDrawable != null) {
            requestOptions.placeholder(placeHolderDrawable)
        }
        //错误图
        if (errorImage != 0) {
            requestOptions.error(errorImage)
        }
        //圆形图片
        if (isCircle) {
            requestOptions.transform(CircleCrop())
        }
        //带边框圆形图片
        if (withCircleBorder) {
            requestOptions.transform(CropCircleWithBorderTransformation(borderSize, borderColor))
        }
        //圆角
        if (roundRadius != 0) {
            requestOptions.transform(RoundedCornersTransformation(roundRadius, 0, cornerType))
        }
        //淡入淡出动画
        if (withCrossFade) {
            if (asImageType == asDrawable) {
                val requestBuilder1 = requestBuilder as? RequestBuilder<Drawable>
                requestBuilder1?.transition(DrawableTransitionOptions.withCrossFade())
            }
        }
        //高斯模糊
        if (blurValue != 0) {
            requestOptions.transform(BlurTransformation(blurValue))
        }

        //剪裁类型
        if (scaleType != null) {
            if (scaleType === ScaleType.CENTER_CROP) {
                requestOptions.centerCrop()
            }
            if (scaleType === ScaleType.CENTER_INSIDE) {
                requestOptions.centerInside()
            }
            if (scaleType === ScaleType.FIT_CENTER) {
                requestOptions.fitCenter()
            }
        }

        // 缩略图
        requestBuilder.thumbnail(sizeMultiplier)

        //优先级
        requestOptions.priority(priority)

        //变换
        if (transformations != null) {
            requestOptions.transform(transformations)
        }

        if (dontAnimate) {
            requestOptions.dontAnimate()
        }

        requestBuilder.apply(requestOptions)
        return requestBuilder
    }


    @SuppressLint("CheckResult")
    private fun createRequestBuilder(): RequestBuilder<*>? {

        if (loadUrl.isNullOrEmpty() && null == loadFile && null == loadUri && null == loadBitmap && null == loadResourceId) {
            return null
        }
        if (null == context || Utils.isDestroy(context)) {
            return null
        }
        if (wifiLoad && !loadUrl.isNullOrEmpty() && Utils.isHttpUrl(loadUrl)) {
            if (ImageLoaderGlobalConfig.loadImageOnlyInWifi && !Utils.isWifiConnected(context)) {
                return null
            }
        }
        val requestBuilder: RequestBuilder<*>?
        val requestManager: RequestManager = Glide.with(context!!)

        requestBuilder = when (asImageType) {
            asFile -> {
                requestManager.asFile()
            }
            asBitmap -> {
                requestManager.asBitmap()
            }
            asGif -> {
                requestManager.asGif()
            }
            asDrawable -> {
                requestManager.asDrawable()
            }
            else -> {
                requestManager.asDrawable()
            }
        }

        when {
            loadUrl?.isNotEmpty() == true -> {
                requestBuilder.load(loadUrl)
            }
            null != loadUri -> {
                requestBuilder.load(loadUri)
            }
            null != loadFile -> {
                requestBuilder.load(loadFile)
            }
            null != loadBitmap -> {
                requestBuilder.load(loadBitmap)
            }
            null != loadDrawable -> {
                requestBuilder.load(loadDrawable)
            }
            null != loadResourceId -> {
                requestBuilder.load(loadResourceId)
            }
            else -> requestBuilder.load(loadUrl)
        }

        return requestBuilder
    }

}


