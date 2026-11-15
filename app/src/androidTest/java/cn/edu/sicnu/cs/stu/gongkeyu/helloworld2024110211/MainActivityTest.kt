package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun bottomNavDisplayed() {
        // 底部导航要显示
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun personalInfoFragmentDisplayed() {
        // 启动后默认显示个人信息Fragment的“首页”形态：两个按钮可见
        onView(withId(R.id.btn_edit)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_play)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_show_name)).check(matches(isDisplayed()))
    }
}
