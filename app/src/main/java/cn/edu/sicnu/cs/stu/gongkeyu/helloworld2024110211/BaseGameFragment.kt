package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211.databinding.FragmentGameBinding
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

// 翻牌游戏Fragment基类：负责界面、适配器、序列化存取和重置
abstract class BaseGameFragment : Fragment() {

    protected lateinit var binding: FragmentGameBinding
    lateinit var game: CardMatchingGame
        private set
    private lateinit var adapter: CardAdapter

    // 每局匹配的牌数：2或3
    protected abstract val cardsPerMatch: Int

    // 序列化保存的文件名，两个游戏分开存
    protected abstract val dataFileName: String

    // 界面标题
    protected abstract val titleRes: Int

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.setText(titleRes)

        // 先从文件读取上次的游戏，读不到就新建一局
        game = loadData() ?: CardMatchingGame(CARD_COUNT, cardsPerMatch)

        adapter = CardAdapter(game)
        adapter.setOnCardClickListener { index ->
            game.chooseCardAtIndex(index)
            updateUI()
        }
        binding.recyclerCards.adapter = adapter

        applyLayoutManager()
        updateUI()

        binding.btnReset.setOnClickListener {
            game.reset()
            updateUI()
            saveData()
        }
    }

    // 横屏6列，竖屏4列，始终24张牌，靠垂直滑动看全部
    private fun applyLayoutManager() {
        val spanCount =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 6 else 4
        binding.recyclerCards.layoutManager =
            GridLayoutManager(requireContext(), spanCount, GridLayoutManager.VERTICAL, false)
    }

    protected fun updateUI() {
        binding.tvScore.text = getString(R.string.score_label, game.score)
        adapter.notifyDataSetChanged()
    }

    // 切走或退出时保存当前游戏状态
    override fun onPause() {
        super.onPause()
        saveData()
    }

    private fun saveData() {
        if (!this::game.isInitialized) return
        try {
            val output = requireActivity().openFileOutput(dataFileName, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(output)
            objectOutputStream.writeObject(game)
            objectOutputStream.close()
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadData(): CardMatchingGame? {
        try {
            val input = requireActivity().openFileInput(dataFileName)
            val objectInputStream = ObjectInputStream(input)
            val result = objectInputStream.readObject() as CardMatchingGame
            objectInputStream.close()
            input.close()
            return result
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        const val CARD_COUNT = 24
    }
}
