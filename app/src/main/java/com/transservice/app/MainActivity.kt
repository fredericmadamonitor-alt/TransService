package com.transservice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transservice.app.data.AppDatabase
import com.transservice.app.data.Repository
import com.transservice.app.ui.TransServiceApp
import com.transservice.app.ui.theme.TransServiceTheme
import com.transservice.app.viewmodel.TransServiceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        val repository = Repository(db)

        setContent {
            TransServiceTheme {
                val viewModel: TransServiceViewModel = viewModel(
                    factory = TransServiceViewModel.factory(repository)
                )
                TransServiceApp(viewModel)
            }
        }
    }
}
