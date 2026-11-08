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
    fun allButtonsDisplayed() {
        // 两个按钮都要显示
        onView(withId(R.id.btn_edit_info)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_play_game)).check(matches(isDisplayed()))
    }

    @Test
    fun playerInfoDisplayed() {
        // 玩家信息TextView要显示
        onView(withId(R.id.tv_name)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_highscore)).check(matches(isDisplayed()))
    }
}
