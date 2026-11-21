package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * 实验七：音乐播放服务
 *
 * 作业要求(2)a：所有与播放相关的功能（播放、暂停、停止、上一首、下一首、拖动进度）全部在服务里实现。
 * Activity 不直接碰 MediaPlayer，只通过 Binder 读取歌名、歌手、播放进度等状态。
 *
 * 服务同时是“已启动 + 已绑定”的：
 * - 已启动：Activity 退出（解绑）后歌还能继续放；
 * - 已绑定：Activity 通过 Binder 拿到服务和状态；
 * - 播放时进入前台服务，通知栏常驻显示当前歌曲名（作业要求(2)b、(4)）。
 */
class MusicService : Service() {

    companion object {
        private const val CHANNEL_ID = "music_playback_channel"
        private const val NOTIFICATION_ID = 1001

        // 通知栏按钮的动作
        const val ACTION_PREVIOUS = "cn.edu.sicnu.music.PREVIOUS"
        const val ACTION_PLAY_PAUSE = "cn.edu.sicnu.music.PLAY_PAUSE"
        const val ACTION_NEXT = "cn.edu.sicnu.music.NEXT"

        private const val REQ_CONTENT = 100
        private const val REQ_PREVIOUS = 101
        private const val REQ_PLAY_PAUSE = 102
        private const val REQ_NEXT = 103
    }

    private lateinit var mediaPlayer: MediaPlayer

    private val playlist = mutableListOf<MusicItem>()
    private var currentIndex = -1
    private var playing = false
    private var inForeground = false

    /**
     * 暴露给 Activity 的 Binder。
     * 只提供“查状态”的方法，Activity 通过 Binder 拿歌名、进度（作业要求(2)b）。
     */
    inner class MusicBinder : Binder() {

        fun getService(): MusicService = this@MusicService

        fun getCurrentTitle(): String = playlist.getOrNull(currentIndex)?.title.orEmpty()

        fun getCurrentArtist(): String = playlist.getOrNull(currentIndex)?.artist.orEmpty()

        fun getCurrentIndex(): Int = currentIndex

        fun isPlaying(): Boolean = playing

        /** 当前播放位置（毫秒），Activity 在后台线程里定时读取 */
        fun getPosition(): Long =
            runCatching { mediaPlayer.currentPosition.toLong() }.getOrDefault(0L)

        /** 当前歌曲总时长（毫秒） */
        fun getDuration(): Long =
            runCatching { mediaPlayer.duration.toLong() }.getOrDefault(0L)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            // 一首放完自动下一首
            setOnCompletionListener { next() }
            setOnErrorListener { _, _, _ ->
                playing = false
                publishNotification()
                true
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = MusicBinder()

    /** 通知栏上的按钮通过 PendingIntent 回到这里 */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PREVIOUS -> previous()
            ACTION_PLAY_PAUSE -> resumeOrPause()
            ACTION_NEXT -> next()
        }
        return START_NOT_STICKY
    }

    /**
     * Activity 用 ContentResolver 查到歌单后交给服务。
     * 保留当前正在播放的那首歌的位置，避免 Activity 重新绑定后把进度弄丢。
     */
    fun setPlaylist(list: List<MusicItem>) {
        val playingUri = playlist.getOrNull(currentIndex)?.uri
        playlist.clear()
        playlist.addAll(list)
        currentIndex = if (playingUri == null) {
            -1
        } else {
            playlist.indexOfFirst { it.uri == playingUri }
        }
    }

    /** 播放第 index 首 */
    fun play(index: Int) {
        if (index !in playlist.indices) return

        // 第一次播放时把服务也变成“已启动”，这样 Activity 解绑后歌还能继续放
        if (!inForeground) {
            runCatching { startService(Intent(this, MusicService::class.java)) }
        }

        currentIndex = index
        playing = false

        runCatching {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, playlist[index].uri)
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
                playing = true
                // 准备完成后刷新通知里的播放/暂停状态
                publishNotification()
            }
            // 异步准备，不阻塞主线程
            mediaPlayer.prepareAsync()
        }.onFailure {
            playing = false
        }

        // 不用等 prepare 完成，先把歌名推到通知栏（作业要求(4)）
        publishNotification()
    }

    /** 播放 / 暂停 切换 */
    fun resumeOrPause() {
        if (currentIndex < 0) {
            // 还没选歌，默认从第一首开始
            if (playlist.isNotEmpty()) play(0)
            return
        }
        runCatching {
            if (playing) {
                mediaPlayer.pause()
                playing = false
            } else {
                mediaPlayer.start()
                playing = true
            }
        }
        publishNotification()
    }

    /** 停止：清掉播放位置和通知，服务也退出 */
    fun stop() {
        runCatching { mediaPlayer.reset() }
        playing = false
        currentIndex = -1

        if (inForeground) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
            inForeground = false
        }
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        stopSelf()
    }

    /** 下一首，最后一首之后回到第一首 */
    fun next() {
        if (playlist.isEmpty()) return
        val nextIndex = if (currentIndex + 1 >= playlist.size) 0 else currentIndex + 1
        play(nextIndex)
    }

    /** 上一首，第一首之前跳到最后一首 */
    fun previous() {
        if (playlist.isEmpty()) return
        val prevIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
        play(prevIndex)
    }

    /** SeekBar 拖动后调整播放位置 */
    fun seekTo(positionMs: Long) {
        runCatching { mediaPlayer.seekTo(positionMs.toInt()) }
    }

    // ---------------- 通知 ----------------

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notify_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notify_channel_desc)
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    /** 刷新通知栏：歌曲名 + 上一首/播放暂停/下一首 */
    private fun publishNotification() {
        if (currentIndex < 0) return
        val notification = buildNotification()
        if (!inForeground) {
            startForeground(NOTIFICATION_ID, notification)
            inForeground = true
        } else {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val song = playlist.getOrNull(currentIndex)
        val artist = song?.artist.orEmpty()

        val contentIntent = PendingIntent.getActivity(
            this,
            REQ_CONTENT,
            Intent(this, MusicActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            // 作业要求：新播放歌曲时在通知栏显示当前歌曲的名字
            .setContentTitle(song?.title ?: getString(R.string.music_no_selected))
            .setContentText(if (artist.isEmpty()) getString(R.string.notify_title) else artist)
            .setContentIntent(contentIntent)
            .addAction(0, getString(R.string.btn_previous), actionIntent(ACTION_PREVIOUS, REQ_PREVIOUS))
            .addAction(
                0,
                getString(if (playing) R.string.btn_pause else R.string.btn_play),
                actionIntent(ACTION_PLAY_PAUSE, REQ_PLAY_PAUSE)
            )
            .addAction(0, getString(R.string.btn_next), actionIntent(ACTION_NEXT, REQ_NEXT))
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, MusicService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    override fun onDestroy() {
        runCatching { mediaPlayer.release() }
        if (inForeground) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
            inForeground = false
        }
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        super.onDestroy()
    }
}
