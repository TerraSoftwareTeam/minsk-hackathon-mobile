package com.dev.lbsuhack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dev.lbsuhack.ui.AppRootScreen
import com.dev.lbsuhack.ui.theme.LbsuhackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LbsuhackTheme {
                AppRootScreen()
            }
        }
    }
}
