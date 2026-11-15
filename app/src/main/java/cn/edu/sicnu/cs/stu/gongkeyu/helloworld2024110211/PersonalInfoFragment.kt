package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.FragmentPersonalInfoBinding
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

// 第1个Fragment：个人信息
// 默认是“首页”观感：展示个人信息 + 修改个人信息 / 玩翻牌游戏两个按钮
// 点“修改个人信息”才切换到输入框，点“玩翻牌游戏”跳到第2个Fragment
// 个人信息用序列化保存，退出重进可恢复
class PersonalInfoFragment : Fragment() {

    private lateinit var binding: FragmentPersonalInfoBinding
    private var info: PlayerInfo = PlayerInfo()

    // true 表示当前处于编辑框状态
    private var editing = false

    private val dataFileName = "player_info.dat"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 先从文件反序列化读出历史信息
        info = loadInfo()
        savedInstanceState?.let { editing = it.getBoolean(KEY_EDITING, false) }

        fillForm()
        updateDisplay()
        if (editing) showEditMode() else showDisplayMode()

        // 修改个人信息：切到输入框
        binding.btnEdit.setOnClickListener { showEditMode() }

        // 玩翻牌游戏：跳到第2个Fragment（2张牌）
        binding.btnPlay.setOnClickListener {
            (requireActivity() as MainActivity).openTwoCardGame()
        }

        // 保存：写回文件，回到展示状态
        binding.btnSave.setOnClickListener {
            info = collectInfo()
            saveInfo()
            updateDisplay()
            showDisplayMode()
            Toast.makeText(requireContext(), R.string.save_success, Toast.LENGTH_SHORT).show()
        }
    }

    // 编辑状态下离开页面，先把输入的内容存下来，避免丢数据
    override fun onPause() {
        super.onPause()
        if (editing) {
            info = collectInfo()
            saveInfo()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_EDITING, editing)
    }

    private fun showDisplayMode() {
        editing = false
        binding.groupDisplay.isVisible = true
        binding.groupEdit.isVisible = false
    }

    private fun showEditMode() {
        editing = true
        fillForm()
        binding.groupDisplay.isVisible = false
        binding.groupEdit.isVisible = true
    }

    // 把读到的信息填进输入框
    private fun fillForm() {
        binding.etName.setText(info.name)
        binding.etStudentId.setText(info.studentId)
        binding.etPhone.setText(info.phone)
        binding.etEmail.setText(info.email)
        if (info.gender == getString(R.string.gender_male)) {
            binding.rbMale.isChecked = true
        } else {
            binding.rbFemale.isChecked = true
        }
        val hobbyList = info.hobbies.split(",")
        binding.cbTravel.isChecked = hobbyList.contains(getString(R.string.hobby_travel))
        binding.cbReading.isChecked = hobbyList.contains(getString(R.string.hobby_reading))
        binding.cbGame.isChecked = hobbyList.contains(getString(R.string.hobby_game))
        binding.cbShopping.isChecked = hobbyList.contains(getString(R.string.hobby_shopping))
    }

    // 把读到的信息显示在只读区域
    private fun updateDisplay() {
        binding.tvShowName.text =
            getString(R.string.field_name, textOrEmpty(info.name))
        binding.tvShowStudentId.text =
            getString(R.string.field_student_id, textOrEmpty(info.studentId))
        binding.tvShowPhone.text =
            getString(R.string.field_phone, textOrEmpty(info.phone))
        binding.tvShowEmail.text =
            getString(R.string.field_email, textOrEmpty(info.email))
        binding.tvShowGender.text =
            getString(R.string.field_gender, textOrEmpty(info.gender))
        binding.tvShowHobby.text =
            getString(R.string.field_hobby, hobbyText())
    }

    private fun textOrEmpty(value: String): String =
        if (value.isBlank()) getString(R.string.empty_value) else value

    private fun hobbyText(): String {
        if (info.hobbies.isBlank()) return getString(R.string.empty_value)
        val list = info.hobbies.split(",").filter { it.isNotBlank() }
        if (list.isEmpty()) return getString(R.string.empty_value)
        return list.joinToString(getString(R.string.hobby_separator))
    }

    // 从输入框读出信息
    private fun collectInfo(): PlayerInfo {
        val result = PlayerInfo()
        result.name = binding.etName.text.toString()
        result.studentId = binding.etStudentId.text.toString()
        result.phone = binding.etPhone.text.toString()
        result.email = binding.etEmail.text.toString()
        result.gender = if (binding.rbMale.isChecked) {
            getString(R.string.gender_male)
        } else {
            getString(R.string.gender_female)
        }

        val list = mutableListOf<String>()
        if (binding.cbTravel.isChecked) list.add(getString(R.string.hobby_travel))
        if (binding.cbReading.isChecked) list.add(getString(R.string.hobby_reading))
        if (binding.cbGame.isChecked) list.add(getString(R.string.hobby_game))
        if (binding.cbShopping.isChecked) list.add(getString(R.string.hobby_shopping))
        result.hobbies = list.joinToString(",")
        return result
    }

    // 序列化写入文件
    private fun saveInfo() {
        try {
            val output = requireActivity().openFileOutput(dataFileName, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(output)
            objectOutputStream.writeObject(info)
            objectOutputStream.close()
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 从文件反序列化读取
    private fun loadInfo(): PlayerInfo {
        try {
            val input = requireActivity().openFileInput(dataFileName)
            val objectInputStream = ObjectInputStream(input)
            val result = objectInputStream.readObject() as PlayerInfo
            objectInputStream.close()
            input.close()
            return result
        } catch (e: Exception) {
            return PlayerInfo()
        }
    }

    companion object {
        private const val KEY_EDITING = "key_editing"

        @JvmStatic
        fun newInstance(): PersonalInfoFragment = PersonalInfoFragment()
    }
}
