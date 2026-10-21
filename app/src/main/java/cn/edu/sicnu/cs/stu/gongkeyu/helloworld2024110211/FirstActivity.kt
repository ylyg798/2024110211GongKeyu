package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityFirstBinding

class FirstActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 接收MainActivity正向传递过来的参数并显示
        val data = intent.getStringExtra("extra_data")
        binding.tvReceived.text = getString(R.string.first_received_label) + data

        binding.btnBack.setOnClickListener { finish() }
    }
}
