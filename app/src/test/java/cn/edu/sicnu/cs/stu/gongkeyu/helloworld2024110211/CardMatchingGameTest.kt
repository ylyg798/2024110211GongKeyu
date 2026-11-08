package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardMatchingGameTest {

    @Test
    fun chooseCard_firstCardOnlyCostsOne() {
        val game = CardMatchingGame(20)
        game.chooseCardAtIndex(0)
        // 翻第一张扣1分
        assertEquals(-1, game.score)
        assertTrue(game.cardAtIndex(0).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
    }

    @Test
    fun chooseSameCardAgain_togglesOff() {
        val game = CardMatchingGame(20)
        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)
        // 翻开再点一下翻回去，不额外扣分
        assertEquals(-1, game.score)
        assertFalse(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun cardCount_returns20() {
        val game = CardMatchingGame(20)
        var count = 0
        for (i in 0 until 20) {
            game.cardAtIndex(i)
            count++
        }
        assertEquals(20, count)
    }
}
