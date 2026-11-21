package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.net.Uri

/**
 * 一首歌曲的数据模型
 * 由 MusicActivity 通过 ContentResolver 查询 MediaStore 得到
 */
data class MusicItem(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri
)
