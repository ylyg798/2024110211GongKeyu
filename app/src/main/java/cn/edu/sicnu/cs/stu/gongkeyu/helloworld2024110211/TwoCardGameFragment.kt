package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

// 第2个Fragment：2张牌翻牌游戏
class TwoCardGameFragment : BaseGameFragment() {

    override val cardsPerMatch: Int = 2
    override val dataFileName: String = "game_two.dat"
    override val titleRes: Int = R.string.game_two_title

    companion object {
        @JvmStatic
        fun newInstance(): TwoCardGameFragment = TwoCardGameFragment()
    }
}
