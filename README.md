# StatusBarCleaner

小米澎湃OS 3 (Android 14+) 截图自动隐藏状态栏 LSPosed 模块。

## 功能

- 每次截图时,自动把状态栏设为**完全透明**
- app 内容(本就延伸到状态栏后面)自然透过来
- 背景颜色**不变**,只隐藏状态栏内容(时钟、电池、信号图标等)

## 效果对比

**普通截图**:
```
┌──────────────────────────┐
│ 12:34  📶 90%      🔋   │  ← 状态栏
│                          │
│    app 内容              │
│                          │
└──────────────────────────┘
```

**使用本模块后**:
```
┌──────────────────────────┐
│                          │  ← 空白(app 内容透过来)
│                          │
│    app 内容              │
│                          │
└──────────────────────────┘
```

## 使用方法

1. 安装 APK
2. 在 **LSPosed** 中启用本模块
3. 作用域勾选 **`com.android.systemui`**
4. 重启系统 UI
5. 截图测试

## 实现原理

- Hook `com.android.systemui.screenshot.ScreenshotController.takeScreenshot`
- 截图前:遍历 SystemUI 的 view,把所有"状态栏相关"的 view 的 `alpha` 设为 0
- 截图后:恢复 `alpha` 为原始值

## 技术细节

- LSPosed API: 102 (`io.github.libxposed.api`)
- 最小 SDK: 31 (Android 12)
- 编译 SDK: 34 (Android 14)

## 自用声明

仅供个人学习使用,不公开分发。