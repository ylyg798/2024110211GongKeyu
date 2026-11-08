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
}
