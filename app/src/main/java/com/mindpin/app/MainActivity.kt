package com.mindpin.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.mindpin.app.notification.PersistentNotificationService
import com.mindpin.app.ui.MindPinApp
import com.mindpin.app.ui.viewmodel.MindPinViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MindPinViewModel by viewModels {
        val app = application as MindPinApplication
        MindPinViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PersistentNotificationService.start(this)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MindPinApp(viewModel = viewModel)
        }
    }
}
