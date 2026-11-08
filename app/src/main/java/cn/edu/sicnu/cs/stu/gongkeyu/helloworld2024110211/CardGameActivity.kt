package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.ActivityCardGameBinding

class CardGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardGameBinding
    private lateinit var game: CardMatchingGame
    private lateinit var adapter: CardAdapter
    private val cardCount = 24

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startNewGame()

        binding.btnRestart.setOnClickListener { startNewGame() }
        binding.btnExit.setOnClickListener { finish() }
    }

    private fun startNewGame() {
        game = CardMatchingGame(cardCount)
        // 竖屏4列，横屏6列，都是24张牌
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 4
        // 明确设置垂直滚动方向
        val layoutManager = GridLayoutManager(this, spanCount, GridLayoutManager.VERTICAL, false)
        adapter = CardAdapter(getCards()) { position ->
            game.chooseCardAtIndex(position)
            updateUI()
        }
        binding.recyclerCards.layoutManager = layoutManager
        binding.recyclerCards.adapter = adapter
        updateUI()
    }

    private fun getCards(): List<Card> {
        val list = mutableListOf<Card>()
        for (i in 0 until cardCount) {
            list.add(game.cardAtIndex(i))
        }
        return list
    }

    private fun updateUI() {
        binding.tvScore.text = getString(R.string.score_label, game.score)
        if (game.score > Player.highScore) {
            Player.highScore = game.score
        }
        adapter.notifyDataSetChanged()
    }
}
