package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val langList = listOf(
        Triple("你好，世界！", R.drawable.cn, "切换语言"),
        Triple("Hello World!", R.drawable.us, "Switch Language"),
        Triple("Ciao Mondo!", R.drawable.it, "Cambia lingua")
    )
    private var currentIndex = 0

    // 这些变量写在这里！onCreate外面，类里面！
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var btnSwitch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 120, 60, 60)
            gravity = android.view.Gravity.CENTER
        }

        imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 40
            }
        }

        textView = TextView(this).apply {
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 60
            }
        }

        btnSwitch = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                currentIndex = (currentIndex + 1) % langList.size
                updateUI()
            }
        }

        rootLayout.addView(imageView)
        rootLayout.addView(textView)
        rootLayout.addView(btnSwitch)

        setContentView(rootLayout)
        updateUI()
    }

    private fun updateUI() {
        val (showText, imgId, btnText) = langList[currentIndex]
        textView.text = showText
        imageView.setImageResource(imgId)
        btnSwitch.text = btnText
    }
}