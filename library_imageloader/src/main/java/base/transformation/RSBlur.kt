package base.transformation

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.*
import android.renderscript.RenderScript.RSMessageHandler

/**
 * Project Name: wanandroid
 * File Name:    base.transformation.RSBlur.java
 * ClassName:    base.transformation.RSBlur
 *
 * Description:
 *
 * @author 唐晓辉
 * @date 2021年09月16日 16:16
 */
object RSBlur {
    @Throws(RSRuntimeException::class)
    fun blur(context: Context?, bitmap: Bitmap, radius: Int): Bitmap {
        var rs: RenderScript? = null
        var input: Allocation? = null
        var output: Allocation? = null
        var blur: ScriptIntrinsicBlur? = null
        try {
            rs = RenderScript.create(context)
            rs.messageHandler = RSMessageHandler()
            input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT)
            output = Allocation.createTyped(rs, input.type)
            blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            blur.setInput(input)
            blur.setRadius(radius.toFloat())
            blur.forEach(output)
            output.copyTo(bitmap)
        } finally {
            if (rs != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    RenderScript.releaseAllContexts()
                } else {
                    rs.destroy()
                }
            }
            input?.destroy()
            output?.destroy()
            blur?.destroy()
        }
        return bitmap
    }
}