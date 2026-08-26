package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.MainScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JarvisViewModel
import com.example.viewmodel.JarvisViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels {
        JarvisViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = JarvisBackground
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
