/*
 * StatusBarCleaner - 截图时自动隐藏状态栏
 * 主界面:开关
 */
package com.lq666.statusbarcleaner

import android.app.Activity
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchEnable = findViewById<Switch>(R.id.switchEnable)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val prefs = getSharedPreferences("statusbar_cleaner", MODE_PRIVATE)
        val initialEnabled = prefs.getBoolean("enabled", false)

        switchEnable.isChecked = initialEnabled
        tvStatus.text = if (initialEnabled) getString(R.string.enabled) else getString(R.string.disabled)

        switchEnable.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("enabled", isChecked).apply()
            tvStatus.text = if (isChecked) getString(R.string.enabled) else getString(R.string.disabled)
        }
    }
}