package com.zxj.compose

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.zxj.compose.ui.theme.HighAndroidTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    companion object : View.OnClickListener {

        val a = 0

        override fun onClick(v: View?) {
            TODO("Not yet implemented")
        }

    }

    var name by mutableStateOf("rengwuxian")

    val nums = mutableStateListOf(1, 2, 3)
    val map = mutableStateMapOf(1 to "One")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            Box(
                Modifier
                    .background(Color.Blue)
                    .padding(40.dp)) {
                Text(text = "111")
            }
        }
//        setContent {
//            // WrapperFunction start
//            Text(text = "固定")
//
//            Test()
//            println(" ------- refresh ------- ")
//            lifecycleScope.launch {
//                delay(3000L)
//                name = "朱凯"
//            }
//            // WrapperFunction end
//        }

//        setContent {
//            nums.forEach { it.and(2) }
//            // WrapFunction start
//            var name by remember { mutableStateOf("rengwuxian") }
//            Text(name)
//            lifecycleScope.launch {
//                delay(3000)
//                name = "朱凯"
//            }
//            // WrapFunction end
//        }
    }

    // 每个compose是单独执行的
    @Composable
    fun Test() {
        // 当前函数全部会重新走一次
        // WrapperFunction start
        Column {
            println(" ------- refresh column ------- ")
            Text(text = name)
        }
        // WrapperFunction end
    }


    @Composable
    fun Greeting(name: String) {
        val methods = this::class.java.methods
        methods.forEach {
            if (it.name == "Greet") {
                it
            }
        }
        Text(
            text = "Hello $name!",
            Modifier
                .padding(8.dp)
                .clickable {
                    Toast
                        .makeText(
                            this,
                            "Button Modifier second",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
                .clickable {
                    Toast
                        .makeText(
                            this,
                            "Button Modifier first",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
//            .padding(8.dp)
        )
    }


}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HighAndroidTheme {
//        Greeting("Android")
    }
}