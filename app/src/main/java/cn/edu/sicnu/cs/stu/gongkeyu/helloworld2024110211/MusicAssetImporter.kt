package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log

/**
 * 把 assets/music 里随 App 打包的音频释放到系统媒体库。
 *
 * 为什么这么做：实验要求用 ContentResolver 查 MediaStore 拿歌单，而 MediaStore 只认
 * 共享存储里被索引过的音频，存在 APK 内部（assets / res/raw）的文件是查不到的。
 * 所以首次启动时把 assets 里的音频插进 MediaStore（相当于"资源管理器里拷到 Music 文件夹"），
 * 之后再照常 query，既满足 ContentResolver 的要求，音频又跟着项目走。
 */
object MusicAssetImporter {

    private const val ASSET_DIR = "music"
    private const val PREF_NAME = "music_asset_import"
    private const val KEY_IMPORTED = "imported_assets"

    /** 把 assets/music 下还没导入过的音频写进媒体库，返回成功导入的数量 */
    fun importIfNeeded(context: Context): Int {
        val assets = runCatching { context.assets.list(ASSET_DIR) }
            .getOrNull()
            .orEmpty()
            .filter { !it.startsWith(".") }
        if (assets.isEmpty()) return 0

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val done = (prefs.getStringSet(KEY_IMPORTED, null) ?: emptySet()).toMutableSet()
        val pending = assets.filter { it !in done }
        if (pending.isEmpty()) return 0

        var imported = 0
        for (name in pending) {
            val ok = runCatching { copyToMediaStore(context, name) }
                .onFailure { Log.w("MusicAssetImporter", "导入 $name 失败", it) }
                .getOrDefault(false)
            if (ok) {
                done.add(name)
                imported++
            }
        }
        prefs.edit().putStringSet(KEY_IMPORTED, done).apply()
        return imported
    }

    /** 把单个 asset 复制成媒体库里的一条记录 */
    private fun copyToMediaStore(context: Context, assetName: String): Boolean {
        val resolver = context.contentResolver
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // 已经有同名记录就不再插入，避免反复启动出现一堆重复歌
        val exists = runCatching {
            resolver.query(
                collection,
                arrayOf(MediaStore.Audio.Media._ID),
                "${MediaStore.Audio.Media.DISPLAY_NAME} = ?",
                arrayOf(assetName),
                null
            )?.use { it.count > 0 } ?: false
        }.getOrDefault(false)
        if (exists) return true

        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, assetName)
            put(MediaStore.Audio.Media.MIME_TYPE, mimeTypeOf(assetName))
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val uri: Uri = resolver.insert(collection, values) ?: return false
        resolver.openOutputStream(uri)?.use { out ->
            context.assets.open("$ASSET_DIR/$assetName").use { it.copyTo(out) }
        } ?: return false

        // 写完再取消挂起状态，系统才会把它当正式音频去索引
        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return true
    }

    private fun mimeTypeOf(fileName: String): String =
        when (fileName.substringAfterLast('.', "").lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/x-wav"
            "m4a", "aac" -> "audio/mp4"
            "flac" -> "audio/flac"
            "ogg" -> "audio/ogg"
            else -> "audio/*"
        }
}
