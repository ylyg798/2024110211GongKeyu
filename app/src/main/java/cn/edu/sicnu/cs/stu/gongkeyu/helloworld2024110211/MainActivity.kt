package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityMainBinding

// 主Activity：底部导航切换三个Fragment
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val personalFragment = PersonalInfoFragment.newInstance()
    private val twoCardFragment = TwoCardGameFragment.newInstance()
    private val threeCardFragment = ThreeCardGameFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 首次启动显示个人信息Fragment
        if (savedInstanceState == null) {
            switchTo(personalFragment)
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_personal -> switchTo(personalFragment)
                R.id.nav_two -> switchTo(twoCardFragment)
                R.id.nav_three -> switchTo(threeCardFragment)
            }
            true
        }
    }

    private fun switchTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    // 供个人信息Fragment里的“玩翻牌游戏”按钮调用，跳到2张牌游戏
    fun openTwoCardGame() {
        binding.bottomNav.selectedItemId = R.id.nav_two
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_about)
            .setMessage(R.string.about_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
