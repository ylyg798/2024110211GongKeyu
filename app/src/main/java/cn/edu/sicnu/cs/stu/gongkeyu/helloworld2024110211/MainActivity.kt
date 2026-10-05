package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // ViewBinding 生成的绑定类，直接拿控件实例，告别 findViewById
    private lateinit var binding: ActivityMainBinding

    // Model 层：建议数据源（MVC 中的 M）
    private lateinit var model: ProgramAdviserModel

    // 区1：记录已动态添加的 TextView 条数
    private var dynamicCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ① 用 ViewBinding 加载布局
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ② 初始化 Model
        model = ProgramAdviserModel()

        // ③ 区1：点击按钮，用代码动态 new 一个 TextView 塞进 dynamicContainer
        binding.btnAddText.setOnClickListener {
            dynamicCount++
            val tv = TextView(this).apply {
                text = getString(R.string.dynamic_text, dynamicCount)
                textSize = 16f
                setPadding(24, 24, 24, 24)
            }
            binding.dynamicContainer.addView(tv)
        }

        // ④ 区2：点击查询按钮 → 从 Model 拿建议 → 刷新到 View
        binding.btnQuery.setOnClickListener {
            val adviceResId = model.nextAdvice()
            binding.tvAdvise.setText(adviceResId)
        }
    }
}
