package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityThirdBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThirdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThirdBinding

    // 相机拍照回调：从返回数据 extras 中取出缩略图 Bitmap
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                binding.ivPhoto.setImageBitmap(bitmap)
                val path = saveBitmapToFile(bitmap)
                Toast.makeText(
                    this,
                    getString(R.string.third_saved_to) + path,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // 相机运行时权限请求回调
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, R.string.third_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTakePhoto.setOnClickListener {
            // 先检查相机权限，已授权再发起隐式Intent
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                dispatchTakePictureIntent()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // 采用隐式Intent调用系统相机（参考文档写法）
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        } else {
            // 没有相机应用时，用模拟照片兜底，保证界面有图可显示
            val bitmap = createSimulatedPhoto()
            binding.ivPhoto.setImageBitmap(bitmap)
            val path = saveBitmapToFile(bitmap)
            Toast.makeText(
                this,
                getString(R.string.third_no_camera) + "\n" + getString(R.string.third_saved_to) + path,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 没有相机时画一张模拟照片：蓝色背景 + 镜头圆 + 当前时间
    private fun createSimulatedPhoto(): Bitmap {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(Color.parseColor("#4A90D9"))

        paint.color = Color.WHITE
        paint.alpha = 60
        canvas.drawCircle(width / 2f, height / 2f, 150f, paint)

        paint.color = Color.WHITE
        paint.alpha = 255
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("SIMULATED PHOTO", width / 2f, height / 2f - 100f, paint)
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText(time, width / 2f, height / 2f + 140f, paint)

        return bitmap
    }

    // 把Bitmap保存到应用私有图片目录
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val dir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photos")
        if (!dir.exists()) dir.mkdirs()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "IMG_$timeStamp.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }
}
