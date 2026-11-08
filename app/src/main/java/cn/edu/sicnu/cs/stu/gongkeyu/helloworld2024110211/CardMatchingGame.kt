package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

class CardMatchingGame(count: Int) {
    var score = 0
        private set

    private var cards: MutableList<Card>
    private var cardCount = count

    init {
        cards = mutableListOf()
        reset()
    }

    // 重新开始一局：重新发牌，分数归零
    fun reset() {
        score = 0
        cards.clear()
        val deck = Deck()
        for (i in 1..cardCount) {
            val card: Card? = deck.drawRandomCard()
            if (card != null) {
                cards.add(card)
            }
        }
    }

    fun cardAtIndex(index: Int): Card {
        return cards.get(index)
    }

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    // 3张牌匹配逻辑：翻第3张时和前两张判断
    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (!card.isMatched) {
            if (card.isChosen) {
                card.isChosen = false
                return
            }

            // 统计当前已翻开但未匹配的牌
            var chosenCount = 0
            val chosenCards = mutableListOf<Card>()
            for (otherCard in cards) {
                if (otherCard.isChosen && !otherCard.isMatched) {
                    chosenCount++
                    chosenCards.add(otherCard)
                }
            }

            when (chosenCount) {
                0 -> {
                    // 第一张：翻开
                    score -= COST_TO_CHOOSE
                    card.isChosen = true
                }
                1 -> {
                    // 第二张：只翻开，不判断
                    score -= COST_TO_CHOOSE
                    card.isChosen = true
                }
                2 -> {
                    // 第三张：和前两张一起判断
                    val matchScore = card.match(chosenCards.toTypedArray())
                    if (matchScore > 0) {
                        score += matchScore * MATCH_BONUS
                        card.isMatched = true
                        chosenCards[0].isMatched = true
                        chosenCards[1].isMatched = true
                    } else {
                        score -= MISMATCH_PENALTY
                        chosenCards[0].isChosen = false
                        chosenCards[1].isChosen = false
                        card.isChosen = true
                    }
                }
            }
        }
    }
}
