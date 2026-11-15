package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import java.io.Serializable

class Card(
    private var suit: String,
    private var rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) : Serializable {

    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
        private const val serialVersionUID = 1L
    }

    override fun toString(): String {
        return suit + rank
    }

    // 2张牌模式：同点数得4分基础分，同花色得1分基础分
    // 3张牌模式：三张同花色得3分基础分
    fun match(otherCards: Array<Card>): Int {
        var score = 0
        when (otherCards.size) {
            1 -> {
                val other = otherCards[0]
                if (this.rank == other.rank) {
                    score = 4
                } else if (this.suit == other.suit) {
                    score = 1
                }
            }
            2 -> {
                val c1 = otherCards[0]
                val c2 = otherCards[1]
                if (this.suit == c1.suit && c1.suit == c2.suit) {
                    score = 3
                }
            }
        }
        return score
    }
}
