package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import java.io.Serializable

// cardsPerMatch：2表示两张牌一局，3表示三张牌一局
class CardMatchingGame(val count: Int, val cardsPerMatch: Int = 2) : Serializable {

    var score = 0
        private set

    private val cards: MutableList<Card> = mutableListOf()

    private val MISMATCH_PENALTY = 2
    private val MATCH_BONUS = 4
    private val COST_TO_CHOOSE = 1

    init {
        reset()
    }

    // 重新发牌，分数归零
    fun reset() {
        score = 0
        cards.clear()
        val deck = Deck()
        while (cards.size < count) {
            val card = deck.drawRandomCard() ?: break
            cards.add(card)
        }
    }

    val cardCount: Int
        get() = cards.size

    fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (card.isMatched) return
        if (card.isChosen) {
            card.isChosen = false
            return
        }

        // 统计当前已翻开且未匹配的牌
        val chosenCards = mutableListOf<Card>()
        for (other in cards) {
            if (other.isChosen && !other.isMatched) {
                chosenCards.add(other)
            }
        }

        if (chosenCards.size < cardsPerMatch - 1) {
            // 还没翻够，先记一次翻牌代价
            score -= COST_TO_CHOOSE
            card.isChosen = true
        } else {
            // 翻够牌数，开始判断匹配
            val matchScore = card.match(chosenCards.toTypedArray())
            if (matchScore > 0) {
                score += matchScore * MATCH_BONUS
                card.isMatched = true
                for (chosen in chosenCards) {
                    chosen.isMatched = true
                }
            } else {
                score -= MISMATCH_PENALTY
                for (chosen in chosenCards) {
                    chosen.isChosen = false
                }
                card.isChosen = true
            }
        }
    }

    companion object {
        private const val serialVersionUID = 2L
    }
}
