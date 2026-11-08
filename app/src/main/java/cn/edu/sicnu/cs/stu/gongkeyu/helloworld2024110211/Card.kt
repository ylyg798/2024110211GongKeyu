package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

class Card(private var suit: String, private var rank: String, var isChosen: Boolean = false, var isMatched: Boolean = false) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
    }

    override fun toString(): String {
        return suit + rank
    }

    // 3张牌匹配：只要3张花色一样就算匹配，不管点数
    fun match(otherCards: Array<Card>): Int {
        var score = 0
        if (otherCards.size == 2) {
            val c1 = otherCards[0]
            val c2 = otherCards[1]
            // 三张牌都同花色就算匹配
            if (this.suit == c1.suit && c1.suit == c2.suit) {
                score = 3
            }
        }
        return score
    }
}
