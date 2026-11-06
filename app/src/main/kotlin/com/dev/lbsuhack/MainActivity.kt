package com.dev.lbsuhack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.dev.lbsuhack.core.localization.language.LanguageController
import com.dev.lbsuhack.feature.activities.navigation.ActivityLink
import com.dev.lbsuhack.ui.AppRootScreen
import com.dev.lbsuhack.ui.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private var pendingActivityId by mutableStateOf<String?>(null)

    private val languageController: LanguageController by inject()

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingActivityId = if (savedInstanceState != null) {
            savedInstanceState.getString("pendingActivityId")
        } else {
            ActivityLink.parse(intent?.dataString)
        }
        languageController.refresh()
        requestNotificationPermission()

        enableEdgeToEdge()

        setContent {
            AppTheme {
                AppRootScreen(
                    pendingActivityId = pendingActivityId,
                    onActivityLinkConsumed = {
                        pendingActivityId = null
                        intent.data = null
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ActivityLink.parse(intent.dataString)?.let { pendingActivityId = it }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingActivityId", pendingActivityId)
        super.onSaveInstanceState(outState)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
