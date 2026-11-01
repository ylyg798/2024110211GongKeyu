package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshInfo()

        // 修改个人信息
        binding.btnEditInfo.setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        // 进入翻牌游戏
        binding.btnPlayGame.setOnClickListener {
            startActivity(Intent(this, CardGameActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // 从信息修改页返回后刷新显示
        refreshInfo()
    }

    private fun refreshInfo() {
        binding.tvName.text = getString(R.string.label_name) + Player.name
        binding.tvGender.text = getString(R.string.label_gender) + Player.gender
        binding.tvPhone.text = getString(R.string.label_phone) + Player.phone
        binding.tvEmail.text = getString(R.string.label_email) + Player.email
        binding.tvHobby.text = getString(R.string.label_hobby) + Player.hobbies
        binding.tvHighscore.text = getString(R.string.label_highscore) + Player.highScore
    }
}
