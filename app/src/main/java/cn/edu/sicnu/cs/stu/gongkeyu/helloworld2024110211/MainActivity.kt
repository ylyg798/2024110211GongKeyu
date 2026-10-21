package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 注册第二个Activity的返回回调：接收二级Activity回传的数据
    private val secondLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringExtra("return_data")
            binding.tvReturnResult.text = getString(R.string.return_result_hint) + data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 第1个按钮：正向传参启动第一个二级Activity
        binding.btnToFirst.setOnClickListener {
            val intent = Intent(this, FirstActivity::class.java)
            intent.putExtra("extra_data", getString(R.string.default_send_data))
            startActivity(intent)
        }

        // 第2个按钮：启动第二个二级Activity并等待返回结果
        binding.btnToSecond.setOnClickListener {
            secondLauncher.launch(Intent(this, SecondActivity::class.java))
        }

        // 第3个按钮：启动第三个二级Activity（相机拍照）
        binding.btnToThird.setOnClickListener {
            startActivity(Intent(this, ThirdActivity::class.java))
        }

        // 第4个按钮：启动第四个二级Activity（秒表）
        binding.btnToFourth.setOnClickListener {
            startActivity(Intent(this, FourthActivity::class.java))
        }
    }
}
