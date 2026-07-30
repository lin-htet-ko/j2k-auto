package com.linhtetko.j2k_auto_android_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.linhtetko.j2k_auto_android_sample.ui.screen.MainScreen
import com.linhtetko.j2k_auto_android_sample.ui.theme.J2kautoandroidsampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            J2kautoandroidsampleTheme {
                MainScreen()
            }
        }
    }
}
