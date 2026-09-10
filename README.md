# StatusBarCleaner

小米澎湃OS 3 (Android 14+) 截图自动隐藏状态栏 LSPosed 模块。

## 功能

- 每次截图后,自动在状态栏位置画一条黑色遮罩
- 状态栏位置 = 屏幕顶部 28dp 高度
- 支持 PNG / JPG 格式

## 使用方法

1. 安装 APK
2. 在 **LSPosed** 中启用本模块
3. 作用域勾选 **`com.android.systemui`**
4. 重启系统 UI
5. 截图测试,状态栏位置应是黑色

## 技术细节

- LSPosed API: 102 (io.github.libxposed.api)
- 最小 SDK: 31 (Android 12)
- 编译 SDK: 34 (Android 14)
- 仅在 `com.android.systemui.screenshot.ScreenshotController.saveScreenshot` 之后触发

## 局限

- 状态栏高度固定 28dp(实际设备可能略有差异)
- 仅处理系统截图,某些 App 内置截图功能不生效

## 自用声明

仅供个人学习使用,不公开分发。