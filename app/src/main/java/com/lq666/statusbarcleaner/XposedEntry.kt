/*
 * StatusBarCleaner - LSPosed 入口 (libxposed-api 102)
 * Hook ScreenshotController.saveScreenshot,截图保存后自动给状态栏区域加黑色遮罩
 */
package com.lq666.statusbarcleaner

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.io.File
import java.io.FileOutputStream

class XposedEntry : XposedModule() {

    companion object {
        private const val TAG = "StatusBarCleaner"
        private const val SCREENSHOT_DIR = "/sdcard/Pictures/Screenshots/"
        private const val STATUS_BAR_HEIGHT_DP = 28
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != "com.android.systemui") return

        try {
            val clazz = param.classLoader.loadClass(
                "com.android.systemui.screenshot.ScreenshotController"
            )

            // 找到所有名为 saveScreenshot 的方法
            val methods = clazz.declaredMethods.filter { it.name == "saveScreenshot" }

            if (methods.isEmpty()) {
                Log.w(TAG, "未找到 saveScreenshot 方法")
                return
            }

            methods.forEach { method ->
                hook(method).intercept { chain ->
                    val result = chain.proceed()
                    // 1.5 秒后处理截图(确保文件已写入)
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            processLatestScreenshot()
                        } catch (e: Throwable) {
                            Log.e(TAG, "处理截图失败: ${e.message}", e)
                        }
                    }, 1500)
                    result
                }
            }
            Log.i(TAG, "已 hook ${methods.size} 个 saveScreenshot 方法")
        } catch (e: Throwable) {
            Log.e(TAG, "hook 失败: ${e.message}", e)
        }
    }

    /**
     * 处理最新一张截图:在状态栏位置画黑色矩形覆盖
     */
    private fun processLatestScreenshot() {
        val dir = File(SCREENSHOT_DIR)
        if (!dir.exists() || !dir.isDirectory) {
            Log.w(TAG, "截图目录不存在: $SCREENSHOT_DIR")
            return
        }

        // 找出最近修改的截图
        val latest = dir.listFiles()
            ?.filter { it.isFile && (it.name.endsWith(".png") || it.name.endsWith(".jpg") || it.name.endsWith(".jpeg")) }
            ?.maxByOrNull { it.lastModified() }
            ?: run {
                Log.w(TAG, "未找到截图文件")
                return
            }

        // 检查是否是 10 秒内创建的（避免处理旧图)
        val age = System.currentTimeMillis() - latest.lastModified()
        if (age > 10000) {
            Log.d(TAG, "跳过太旧的截图: ${latest.name} (${age}ms ago)")
            return
        }

        Log.i(TAG, "处理截图: ${latest.name}")

        // 读取原图
        val bitmap = BitmapFactory.decodeFile(latest.absolutePath) ?: return

        // 计算状态栏像素高度
        val density = Resources.getSystem().displayMetrics.density
        val statusBarHeightPx = (STATUS_BAR_HEIGHT_DP * density).toInt()

        // 在顶部画黑色矩形
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            0f, 0f,
            bitmap.width.toFloat(), statusBarHeightPx.toFloat(),
            paint
        )

        // 保存覆盖
        val format = if (latest.name.endsWith(".png")) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        FileOutputStream(latest).use { out ->
            bitmap.compress(format, 100, out)
        }

        bitmap.recycle()
        Log.i(TAG, "已处理截图: ${latest.name}")
    }
}