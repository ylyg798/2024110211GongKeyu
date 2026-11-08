package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityPersonalInfoBinding

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 回显当前玩家信息
        binding.etName.setText(Player.name)
        binding.etStudentId.setText(Player.studentId)
        binding.etPhone.setText(Player.phone)
        binding.etEmail.setText(Player.email)
        if (Player.gender == getString(R.string.gender_male)) {
            binding.rbMale.isChecked = true
        } else {
            binding.rbFemale.isChecked = true
        }
        val hobbyList = Player.hobbies.split(",")
        binding.cbTravel.isChecked = hobbyList.contains(getString(R.string.hobby_travel))
        binding.cbReading.isChecked = hobbyList.contains(getString(R.string.hobby_reading))
        binding.cbGame.isChecked = hobbyList.contains(getString(R.string.hobby_game))
        binding.cbShopping.isChecked = hobbyList.contains(getString(R.string.hobby_shopping))

        // 保存
        binding.btnSave.setOnClickListener {
            Player.name = binding.etName.text.toString().ifEmpty { getString(R.string.hint_name) }
            Player.studentId = binding.etStudentId.text.toString()
            Player.phone = binding.etPhone.text.toString()
            Player.email = binding.etEmail.text.toString()
            Player.gender = if (binding.rbMale.isChecked) getString(R.string.gender_male) else getString(R.string.gender_female)

            val list = mutableListOf<String>()
            if (binding.cbTravel.isChecked) list.add(getString(R.string.hobby_travel))
            if (binding.cbReading.isChecked) list.add(getString(R.string.hobby_reading))
            if (binding.cbGame.isChecked) list.add(getString(R.string.hobby_game))
            if (binding.cbShopping.isChecked) list.add(getString(R.string.hobby_shopping))
            Player.hobbies = list.joinToString(",")

            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show()
            finish()
        }

        // 返回
        binding.btnBack.setOnClickListener { finish() }
    }
}
