package com.github.nullptroma.wallenc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.nullptroma.wallenc.presentation.WallencUi
import dagger.hilt.android.AndroidEntryPoint

class Container<T>(val value: T)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        val sdk = YandexAuthSdk.create(YandexAuthOptions(applicationContext, true))
//        val launcher =
//            registerForActivityResult(sdk.contract) { result -> handleResult(result) }
//        val loginOptions = YandexAuthLoginOptions(LoginType.CHROME_TAB)
        val cont1 = Container(true)
        var cont2 = Container<Boolean>(true)
        setContent {
            WallencUi()
        }
    }

//    private fun handleResult(result: YandexAuthResult) {
//        when (result) {
//            is YandexAuthResult.Success -> Toast.makeText(applicationContext, "Success: ${result.token}", Toast.LENGTH_SHORT).show()
//            is YandexAuthResult.Failure -> Toast.makeText(applicationContext, "Success: ${result.exception}", Toast.LENGTH_SHORT).show()
//            YandexAuthResult.Cancelled -> Toast.makeText(applicationContext, "Cancel", Toast.LENGTH_SHORT).show()
//        }
//    }
}
