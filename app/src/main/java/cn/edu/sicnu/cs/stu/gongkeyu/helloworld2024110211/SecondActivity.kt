package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 确定：把输入内容封装到Intent回传给MainActivity
        binding.btnConfirm.setOnClickListener {
            val input = binding.etInput.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(this, R.string.second_empty_warning, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val resultIntent = Intent()
            resultIntent.putExtra("return_data", input)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // 取消：返回RESULT_CANCELED
        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
