/*
 * StatusBarCleaner - LSPosed 入口 (libxposed-api 102)
 * 截图时把状态栏设为完全透明,app 内容显示出来
 *
 * 实现原理:
 * - Hook com.android.systemui.screenshot.ScreenshotController.takeScreenshot
 * - 截图前:把 SystemUI 的状态栏相关 view 的 alpha 设为 0 (完全透明)
 * - 截图后:延迟恢复 alpha 为 1
 *
 * 因为状态栏背景默认是透明的(只是叠加在 app 上面),app 内容本身就延伸到
 * 状态栏位置后面,所以状态栏 alpha=0 时 app 内容自然透过来,实现"无状态栏"效果
 */
package com.lq666.statusbarcleaner

import android.app.ActivityThread
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManagerGlobal
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.lang.reflect.Field

class XposedEntry : XposedModule() {

    companion object {
        private const val TAG = "StatusBarCleaner"
    }

    // 记录被改动的 View 和原始 alpha, 用于恢复
    private val hiddenViews = mutableMapOf<View, Float>()

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != "com.android.systemui") return

        try {
            val controllerClass = param.classLoader.loadClass(
                "com.android.systemui.screenshot.ScreenshotController"
            )

            // 找到所有 takeScreenshot 相关方法
            val screenshotMethods = controllerClass.declaredMethods.filter {
                it.name.startsWith("takeScreenshot")
            }

            if (screenshotMethods.isEmpty()) {
                Log.w(TAG, "未找到 takeScreenshot 方法")
                return
            }

            screenshotMethods.forEach { method ->
                hook(method).intercept { chain ->
                    Log.d(TAG, "截图触发: ${method.name}")
                    // 1. 隐藏状态栏
                    hideStatusBarViews()
                    // 2. 执行原方法(触发截图)
                    val result = chain.proceed()
                    // 3. 延迟恢复 (1 秒后,确保截图捕获完成)
                    Handler(Looper.getMainLooper()).postDelayed({
                        restoreStatusBarViews()
                    }, 1000)
                    result
                }
            }

            Log.i(TAG, "已 hook ${screenshotMethods.size} 个 takeScreenshot 方法")
        } catch (e: Throwable) {
            Log.e(TAG, "hook 失败: ${e.message}", e)
        }
    }

    /**
     * 把所有状态栏相关 view 设为完全透明 (alpha = 0)
     * 同时隐藏 children (时钟、电池、信号图标)
     */
    private fun hideStatusBarViews() {
        synchronized(hiddenViews) {
            hiddenViews.clear()
            try {
                val views = findSystemUIViews()
                for (view in views) {
                    if (isStatusBarView(view)) {
                        // 记录原始 alpha 和 visibility, 之后恢复
                        if (view.alpha != 0f) {
                            hiddenViews[view] = view.alpha
                            view.alpha = 0f
                        }
                    }
                }
                Log.d(TAG, "已隐藏 ${hiddenViews.size} 个状态栏 view")
            } catch (e: Throwable) {
                Log.e(TAG, "隐藏状态栏失败: ${e.message}", e)
            }
        }
    }

    /**
     * 恢复状态栏 view 的 alpha 和 visibility
     */
    private fun restoreStatusBarViews() {
        synchronized(hiddenViews) {
            try {
                hiddenViews.forEach { (view, originalAlpha) ->
                    try {
                        view.alpha = originalAlpha
                    } catch (e: Throwable) {
                        // view 可能已被销毁,忽略
                    }
                }
                Log.d(TAG, "已恢复 ${hiddenViews.size} 个状态栏 view")
                hiddenViews.clear()
            } catch (e: Throwable) {
                Log.e(TAG, "恢复状态栏失败: ${e.message}", e)
            }
        }
    }

    /**
     * 通过 WindowManagerGlobal 获取所有系统 window 的 view
     */
    private fun findSystemUIViews(): List<View> {
        return try {
            val wmGlobal = WindowManagerGlobal.getInstance()
            // 通过反射调用 getViews() (隐藏 API)
            val viewsField: Field = WindowManagerGlobal::class.java.getDeclaredField("mViews").apply {
                isAccessible = true
            }
            @Suppress("UNCHECKED_CAST")
            val views = viewsField.get(wmGlobal) as? ArrayList<View> ?: return emptyList()
            views.toList()
        } catch (e: Throwable) {
            Log.w(TAG, "无法通过 WindowManagerGlobal 获取 view: ${e.message}")
            // 退而求其次:尝试 ActivityThread 当前 activity
            findSystemUIViewsFallback()
        }
    }

    private fun findSystemUIViewsFallback(): List<View> {
        return try {
            val thread = ActivityThread.currentActivityThread()
            val activities = thread::class.java.getDeclaredMethod("getActivities").apply {
                isAccessible = true
            }.invoke(thread) as? List<*> ?: return emptyList()
            activities.mapNotNull { (it as? android.app.Activity)?.window?.decorView }
        } catch (e: Throwable) {
            Log.w(TAG, "fallback 也失败: ${e.message}")
            emptyList()
        }
    }

    /**
     * 判断 view 是否是状态栏相关
     * 启发式判断:view 的类名包含 "StatusBar" 或 "NotificationIconContainer" 等
     */
    private fun isStatusBarView(view: View): Boolean {
        val name = view.javaClass.name
        return name.contains("StatusBar") ||
            name.contains("NotificationIcon") ||
            name.contains("BatteryMeter") ||
            name == "com.android.systemui.statusbar.policy.Clock"
    }
}