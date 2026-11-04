package com.dev.lbsuhack

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.dev.lbsuhack.core.localization.language.LanguageController
import com.dev.lbsuhack.ui.AppRootScreen
import com.dev.lbsuhack.ui.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val languageController: LanguageController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        languageController.refresh()

        enableEdgeToEdge()

        setContent {
            AppTheme {
                AppRootScreen()
            }
        }
    }
}
