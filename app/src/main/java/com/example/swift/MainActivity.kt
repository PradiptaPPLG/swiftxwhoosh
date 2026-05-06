package com.example.swift

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.swift.navigation.SwiftNavigation
import com.example.swift.ui.theme.SwiftTheme

/**
 * MainActivity extends FragmentActivity (via AppCompatActivity chain) so that
 * androidx.biometric.BiometricPrompt can be constructed with a FragmentActivity host.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwiftTheme {
                SwiftNavigation()
            }
        }
    }
}