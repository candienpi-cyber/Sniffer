package com.example.stalkerplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.stalkerplayer.ui.navigation.StalkerNavGraph
import com.example.stalkerplayer.ui.theme.StalkerPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StalkerPlayerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StalkerNavGraph()
                }
            }
        }
    }
}
