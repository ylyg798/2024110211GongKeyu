package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import java.io.Serializable

// 玩家信息，实现序列化用于文件存储
class PlayerInfo(
    var name: String = "龚柯予",
    var studentId: String = "2024110211",
    var gender: String = "男",
    var phone: String = "13800000000",
    var email: String = "gongkeyu@example.com",
    var hobbies: String = "阅读,游戏",
    var highScore: Int = 0
) : Serializable {

    companion object {
        private const val serialVersionUID = 3L
    }
}
