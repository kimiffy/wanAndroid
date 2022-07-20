package base

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.TextUtils
import java.util.*


/**
 * Project Name: wanandroid
 * File Name:    Utils.java
 * ClassName:    Utils
 *
 * Description:
 *
 * @author 唐晓辉
 * @date 2021年09月18日 15:00
 */
object Utils {

    /**
     * 判断对象是否已Destroy
     *
     * @param cxt
     * @return true 已销毁
     */
    fun isDestroy(cxt: Context?): Boolean {
        var cxt = cxt
        return if (cxt is Activity) {
            isDestroy(cxt as Activity?)
        } else if (cxt is ContextWrapper) {
            cxt = cxt.baseContext
            if (cxt is Activity) {
                isDestroy(cxt as Activity?)
            } else {
                false
            }
        } else {
            false
        }
    }

    /**
     * 判断对象是否已Destroy
     *
     * @param activity
     * @return true 已销毁
     */
    private fun isDestroy(activity: Activity?): Boolean {
        if (activity == null) {
            return true
        }
        return if (Build.VERSION.SDK_INT >= 17) {
            activity.isFinishing || activity.isDestroyed
        } else {
            activity.isFinishing
        }
    }


    fun isHttpUrl(url: String?): Boolean {
        return if (TextUtils.isEmpty(url)) {
            false
        } else url?.toLowerCase(Locale.ROOT)?.startsWith("http") == true
    }


    /**
     * WIFI网络是否连接了
     */
    fun isWifiConnected(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val manager = context.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = manager.activeNetwork ?: return false
            val activeNetwork = manager.getNetworkCapabilities(network) ?: return false
            return when {
                // 是否有wifi网络
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    true
                }
                else -> {
                    false
                }
            }
        } else {
            manager.activeNetworkInfo ?: return false
            val networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return networkInfo?.isAvailable ?: false
        }
    }
}