# 保留 LSPosed / Xposed hook 入口类
-keep class com.lq666.statusbarcleaner.XposedEntry { *; }
-keep class com.lq666.statusbarcleaner.XposedEntry$* { *; }

# 保留所有被反射加载的 Hook 类
-keep class com.lq666.statusbarcleaner.hook.** { *; }