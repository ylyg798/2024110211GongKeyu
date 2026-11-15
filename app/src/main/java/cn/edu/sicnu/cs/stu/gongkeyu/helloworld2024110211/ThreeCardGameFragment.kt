package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

// 第3个Fragment：3张牌翻牌游戏
class ThreeCardGameFragment : BaseGameFragment() {

    override val cardsPerMatch: Int = 3
    override val dataFileName: String = "game_three.dat"
    override val titleRes: Int = R.string.game_three_title

    companion object {
        @JvmStatic
        fun newInstance(): ThreeCardGameFragment = ThreeCardGameFragment()
    }
}
