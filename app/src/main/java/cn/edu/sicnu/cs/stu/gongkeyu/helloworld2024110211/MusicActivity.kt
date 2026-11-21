package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.Manifest
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityMusicBinding
import java.util.Locale

/**
 * 实验七：音乐播放器
 *
 * Activity 负责：申请权限 → 用 ContentResolver 查 MediaStore 得到歌单 → 绑定 Service → 刷新界面。
 * 真正播放的活儿全在 MusicService 里，Activity 只通过 Binder 读状态。
 */
class MusicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusicBinding
    private lateinit var adapter: MusicAdapter
    private val musicList = mutableListOf<MusicItem>()

    // 作业要求(2)b：Activity 通过绑定服务拿到 Binder，再从 Binder 读取歌名、播放进度
    private var musicBinder: MusicService.MusicBinder? = null
    private var bound = false

    private val musicService: MusicService?
        get() = musicBinder?.getService()

    // 后台线程：每 500ms 读一次播放进度（作业要求(3) 多线程）
    private lateinit var progressThread: HandlerThread
    private lateinit var progressHandler: Handler

    // 用户正在拖动 SeekBar 时暂停自动刷新，避免进度条和手指打架
    private var userSeeking = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicBinder = binder
            bound = true

            // 服务可能还在后台放歌，本地歌单还没查出来时不要清空服务里的歌单
            if (musicList.isNotEmpty()) binder.getService().setPlaylist(musicList)

            refreshSongInfo()
            updateProgress()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicBinder = null
            bound = false
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            // 通知权限被拒绝不影响播放，只要拿到音频读权限就加载歌单
            if (result.values.all { it } || hasMediaPermission()) {
                prepareSongs()
            } else {
                Toast.makeText(this, R.string.music_permission_denied, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSeekBar()
        setupButtons()
        refreshSongInfo()

        progressThread = HandlerThread("music-progress").apply { start() }
        progressHandler = Handler(progressThread.looper)

        requestPermissionIfNeeded()
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MusicService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        startProgressLoop()
    }

    override fun onStop() {
        super.onStop()
        // 只解绑，不停止服务：用户按 Home 键离开后歌照常播放，通知栏也还在
        stopProgressLoop()
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
        musicBinder = null
    }

    override fun onDestroy() {
        progressThread.quitSafely()
        super.onDestroy()
    }

    // ---------------- 权限与歌单 ----------------

    private fun mediaPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 作业提示：SDK 13+ 读音频要用 READ_MEDIA_AUDIO
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private fun hasMediaPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, mediaPermission()) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissionIfNeeded() {
        val required = mutableListOf<String>()
        if (!hasMediaPermission()) required.add(mediaPermission())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            required.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (required.isEmpty()) prepareSongs() else permissionLauncher.launch(required.toTypedArray())
    }

    /**
     * 先把 assets/music 里打包的音频导进媒体库（子线程做文件拷贝），再回主线程查歌单。
     * assets 为空时这一步什么都不做，直接走查询。
     */
    private fun prepareSongs() {
        Thread {
            MusicAssetImporter.importIfNeeded(this)
            runOnUiThread { loadMusic() }
        }.start()
    }

    /** 作业要求(1)：用 ContentResolver 查 MediaStore，得到音乐文件列表 */
    private fun loadMusic() {
        if (!hasMediaPermission()) {
            Toast.makeText(this, R.string.music_permission_denied, Toast.LENGTH_LONG).show()
            return
        }

        musicList.clear()
        val collection: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        // 只取歌曲类音频；部分机型的媒体库会把 is_music 记成 NULL，一起查避免漏歌
        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 " +
            "OR ${MediaStore.Audio.Media.IS_MUSIC} IS NULL)"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        runCatching {
            contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val rawTitle = cursor.getString(titleCol).orEmpty().trim()
                    val rawArtist = cursor.getString(artistCol).orEmpty().trim()
                    val title = if (rawTitle.isEmpty() || rawTitle == "<unknown>") {
                        getString(R.string.music_unknown_song)
                    } else {
                        rawTitle
                    }
                    val artist = if (rawArtist.isEmpty() || rawArtist == "<unknown>") {
                        getString(R.string.music_unknown_artist)
                    } else {
                        rawArtist
                    }
                    musicList.add(
                        MusicItem(
                            id = id,
                            title = title,
                            artist = artist,
                            duration = cursor.getLong(durationCol),
                            uri = ContentUris.withAppendedId(collection, id)
                        )
                    )
                }
            }
        }.onFailure { e ->
            Log.e("MusicActivity", getString(R.string.music_load_failed), e)
            Toast.makeText(this, R.string.music_load_failed, Toast.LENGTH_LONG).show()
        }

        adapter.notifyDataSetChanged()
        if (musicList.isNotEmpty()) musicService?.setPlaylist(musicList)

        if (musicList.isEmpty()) {
            Toast.makeText(this, R.string.music_empty, Toast.LENGTH_LONG).show()
        }
    }

    // ---------------- 界面 ----------------

    private fun setupRecyclerView() {
        adapter = MusicAdapter(musicList) { index ->
            musicService?.play(index)
            refreshSongInfo()
        }
        binding.rvMusic.layoutManager = LinearLayoutManager(this)
        binding.rvMusic.adapter = adapter
    }

    private fun setupSeekBar() {
        binding.sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvPosition.text = formatTime(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                userSeeking = true
            }

            // 作业要求(3)：拖动 SeekBar 调整当前播放进度
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                musicService?.seekTo((seekBar?.progress ?: 0).toLong())
                userSeeking = false
            }
        })
    }

    private fun setupButtons() {
        binding.btnPlayPause.setOnClickListener {
            musicService?.resumeOrPause()
            refreshSongInfo()
        }
        binding.btnStop.setOnClickListener {
            musicService?.stop()
            refreshSongInfo()
            updateProgress()
        }
        binding.btnPrevious.setOnClickListener {
            musicService?.previous()
            refreshSongInfo()
        }
        binding.btnNext.setOnClickListener {
            musicService?.next()
            refreshSongInfo()
        }
        binding.btnRefresh.setOnClickListener {
            // 刷新时先补导 assets 里新增的音频，再重新查歌单
            prepareSongs()
        }
    }

    /** 刷新歌名、歌手和播放按钮文字 */
    private fun refreshSongInfo() {
        val binder = musicBinder ?: return
        applySongInfo(binder, force = true)
    }

    /**
     * 把服务里的状态同步到界面。
     * force=false 时只在值变化了才动控件，避免每 500ms 都刷新列表造成闪烁。
     */
    private fun applySongInfo(binder: MusicService.MusicBinder, force: Boolean) {
        val title = binder.getCurrentTitle().ifEmpty { getString(R.string.music_no_selected) }
        if (force || binding.tvTitle.text.toString() != title) {
            binding.tvTitle.text = title
            binding.tvArtist.text = binder.getCurrentArtist()
            adapter.setPlayingIndex(binder.getCurrentIndex())
        }

        val playLabel = getString(if (binder.isPlaying()) R.string.btn_pause else R.string.btn_play)
        if (force || binding.btnPlayPause.text.toString() != playLabel) {
            binding.btnPlayPause.text = playLabel
        }
    }

    /** 由后台线程定时回调到主线程，刷新 SeekBar 和时间文本 */
    private fun updateProgress() {
        val binder = musicBinder ?: return
        val duration = binder.getDuration().coerceAtLeast(0L)
        val position = binder.getPosition().coerceIn(0L, duration)

        binding.sbProgress.max = duration.toInt().coerceAtLeast(1)
        if (!userSeeking) {
            binding.sbProgress.progress = position.toInt()
            binding.tvPosition.text = formatTime(position)
        }
        binding.tvDuration.text = formatTime(duration)

        // 自动连播时歌名、播放按钮状态也要跟着变
        applySongInfo(binder, force = false)
    }

    private fun startProgressLoop() {
        progressHandler.removeCallbacksAndMessages(null)
        progressHandler.post(progressRunnable)
    }

    private fun stopProgressLoop() {
        progressHandler.removeCallbacksAndMessages(null)
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            runOnUiThread { if (bound) updateProgress() }
            progressHandler.postDelayed(this, 500)
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        return String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }
}
