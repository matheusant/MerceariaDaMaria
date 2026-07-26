package com.example.merceariadamaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.merceariadamaria.ui.navigation.MerceariaApp
import com.example.merceariadamaria.ui.theme.MerceariaDaMariaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MerceariaDaMariaTheme {
                MerceariaApp()
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MerceariaApp()
}