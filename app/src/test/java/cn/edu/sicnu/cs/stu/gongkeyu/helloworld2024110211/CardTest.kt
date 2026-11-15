package cn.edu.sicnu.cs.stu.gongkeyu.helloworld2024110211

import org.junit.Assert.assertEquals
import org.junit.Test

class CardTest {

    @Test
    fun matchSameRank_returns4() {
        val a = Card("♥", "A")
        val b = Card("♦", "A")
        assertEquals(4, a.match(arrayOf(b)))
    }

    @Test
    fun matchSameSuit_returns1() {
        val a = Card("♥", "A")
        val b = Card("♥", "K")
        assertEquals(1, a.match(arrayOf(b)))
    }

    @Test
    fun matchNoMatch_returns0() {
        val a = Card("♥", "A")
        val b = Card("♦", "K")
        assertEquals(0, a.match(arrayOf(b)))
    }

    @Test
    fun toString_returnsSuitPlusRank() {
        val a = Card("♠", "10")
        assertEquals("♠10", a.toString())
    }

    @Test
    fun matchThreeSameSuit_returns3() {
        // 3张牌模式：三张同花色即匹配，不看点数
        val a = Card("♥", "A")
        val b = Card("♥", "K")
        val c = Card("♥", "2")
        assertEquals(3, a.match(arrayOf(b, c)))
    }

    @Test
    fun matchThreeSameRankOnly_returns0() {
        // 3张牌模式：只有点数相同、花色不同，不算匹配
        val a = Card("♥", "A")
        val b = Card("♦", "A")
        val c = Card("♠", "A")
        assertEquals(0, a.match(arrayOf(b, c)))
    }
}
