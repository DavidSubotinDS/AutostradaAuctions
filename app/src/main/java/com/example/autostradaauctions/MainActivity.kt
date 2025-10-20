package com.example.autostradaauctions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.autostradaauctions.ui.navigation.SimpleNavigation
import com.example.autostradaauctions.ui.theme.AutostradaAuctionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 🚨 CRITICAL DEBUG: MainActivity onCreate called
        println("🚨🚨🚨 MAINACTIVITY ONCREATE CALLED")
        android.util.Log.d("AutostradaDebug", "🚨🚨🚨 MAINACTIVITY ONCREATE CALLED")
        
        enableEdgeToEdge()
        setContent {
            println("🚨 MAINACTIVITY SETCONTENT EXECUTING")
            android.util.Log.d("AutostradaDebug", "🚨 MAINACTIVITY SETCONTENT EXECUTING")
            
            AutostradaAuctionsTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    println("🚨 MAINACTIVITY ABOUT TO CALL SIMPLENAVIGATION")
                    android.util.Log.d("AutostradaDebug", "🚨 MAINACTIVITY ABOUT TO CALL SIMPLENAVIGATION")
                    
                    SimpleNavigation(
                        navController = navController
                    )
                }
            }
        }
    }
}
