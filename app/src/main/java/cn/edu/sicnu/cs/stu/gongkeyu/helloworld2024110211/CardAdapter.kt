package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private val cards: List<Card>,
    private val onCardClick: (Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    class CardViewHolder(val button: Button) : RecyclerView.ViewHolder(button)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val button = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false) as Button
        return CardViewHolder(button)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
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
            }
            else -> {
                button.text = ""
                button.setBackgroundResource(R.drawable.card_back)
            }
        }

        button.setOnClickListener { onCardClick(position) }
    }

    override fun getItemCount(): Int = cards.size
}
