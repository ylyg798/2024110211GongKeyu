package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

// 从RecyclerView.Adapter派生，直接持有CardMatchingGame模型
class CardAdapter(private val game: CardMatchingGame) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private var onCardClickListener: ((Int) -> Unit)? = null

    class CardViewHolder(val button: Button) : RecyclerView.ViewHolder(button)

    fun setOnCardClickListener(listener: (Int) -> Unit) {
        onCardClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val button = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false) as Button
        return CardViewHolder(button)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = game.cardAtIndex(position)
        val button = holder.button

        when {
            card.isMatched -> {
                button.text = card.toString()
                button.setBackgroundColor(Color.parseColor("#BBBBBB"))
                button.isEnabled = false
            }
            card.isChosen -> {
                button.text = card.toString()
                button.setBackgroundColor(Color.WHITE)
                button.isEnabled = true
            }
            else -> {
                button.text = ""
                button.setBackgroundResource(R.drawable.card_back)
                button.isEnabled = true
            }
        }

        button.setOnClickListener { onCardClickListener?.invoke(position) }
    }

    override fun getItemCount(): Int = game.cardCount
}
