package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityFourthBinding

class FourthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFourthBinding
    private lateinit var handler: Handler

    private var isRunning = false          // 秒表是否正在走
    private var elapsedTime = 0L           // 暂停时累计的毫秒数
    private var startTime = 0L             // 本轮开始的时间戳

    // 每100ms刷新一次界面
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val now = SystemClock.elapsedRealtime()
                updateTimeText(elapsedTime + (now - startTime))
                handler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFourthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler(Looper.getMainLooper())

        // 横竖屏切换后恢复计时状态
        if (savedInstanceState != null) {
            elapsedTime = savedInstanceState.getLong("elapsed_time")
            isRunning = savedInstanceState.getBoolean("is_running")
            if (isRunning) {
                // 重新计时：以当前时刻为新起点，从累计时间继续走
                startTime = SystemClock.elapsedRealtime()
                handler.post(updateRunnable)
            }
            updateTimeText(elapsedTime)
        } else {
            updateTimeText(0)
        }

        binding.btnStart.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                startTime = SystemClock.elapsedRealtime()
                handler.post(updateRunnable)
            }
        }

        binding.btnPause.setOnClickListener {
            if (isRunning) {
                isRunning = false
                elapsedTime += SystemClock.elapsedRealtime() - startTime
                handler.removeCallbacks(updateRunnable)
                updateTimeText(elapsedTime)
            }
        }

        binding.btnReset.setOnClickListener {
            isRunning = false
            elapsedTime = 0
            handler.removeCallbacks(updateRunnable)
            updateTimeText(0)
        }
    }

    // 把毫秒格式化成 HH:mm:ss
    private fun updateTimeText(ms: Long) {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        binding.tvTime.text = getString(R.string.fourth_time_format, hours, minutes, seconds)
    }

    // 横竖屏切换前保存计时状态
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isRunning) {
            elapsedTime += SystemClock.elapsedRealtime() - startTime
        }
        outState.putLong("elapsed_time", elapsedTime)
        outState.putBoolean("is_running", isRunning)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}
