package de.seuhd.worldcup

import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BettingServiceTest {

    private fun match(id: Int, home: String, away: String, hs: Int?, aws: Int?) =
        Match(
            matchId = id,
            round = "Matchday 1",
            date = "2026-06-01",
            homeTeam = home,
            awayTeam = away,
            homeScore = hs,
            awayScore = aws,
            ground = "Test Stadium"
        )

    @BeforeTest
    fun resetBets() {
        BettingService.clear()
    }

    // ── evaluateBonus ──────────────────────────────────────────────────────────

    @Test
    fun `evaluateBonus awards 3 points for an exact score prediction`() {
        val matches = listOf(
            match(1, "AAA", "BBB", 2, 0)
        )

        BettingService.placeBet(Bet(1, Prediction.HOME_WIN, 2, 0)) // bonus = 3
        val result = BettingService.evaluateBonus(matches)

        assertEquals(result, 3)
    }

    @Test
    fun `evaluateBonus awards 1 point for correct outcome without exact score`() {
        val matches = listOf(
            match(1, "AAA", "BBB", 2, 0)
        )

        BettingService.placeBet(Bet(1, Prediction.HOME_WIN)) // bonus = 1
        val result = BettingService.evaluateBonus(matches)

        assertEquals(result, 1)
    }

    @Test
    fun `evaluateBonus awards 0 points for a wrong prediction`() {
        val matches = listOf(
            match(1, "AAA", "BBB", 2, 0)
        )

        BettingService.placeBet(Bet(1, Prediction.AWAY_WIN)) // bonus = 0
        val result = BettingService.evaluateBonus(matches)

        assertEquals(result, 0)
    }

    @Test
    fun `evaluateBonus ignores unplayed matches`() {
        val matches = listOf(
            match(1, "AAA", "BBB", 2, 0),
            match(2, "BBB", "AAA", null, null)
        )

        BettingService.placeBet(Bet(1, Prediction.HOME_WIN))
        val result = BettingService.evaluateBonus(matches)

        assertEquals(result, 1)
    }

    // ── removeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `removeBet removes an existing bet so it no longer affects evaluation`() {
        val matches = listOf(
            match(1, "AAA", "BBB", 2, 0)
        )

        BettingService.placeBet(Bet(1, Prediction.HOME_WIN)) // bonus = 1
        BettingService.removeBet(1)
        val result = BettingService.evaluateBonus(matches)

        assertEquals(result, 0)
    }

    @Test
    fun `removeBet does nothing when no bet exists for that matchId`() {
        val matches = listOf(
            match(1, "AAA", "BBB", 2, 0)
        )

        BettingService.placeBet(Bet(1, Prediction.HOME_WIN)) // bonus = 1
        BettingService.removeBet(2)
        val result = BettingService.evaluateBonus(matches)

        assertEquals(result, 1)
    }

    // ── changeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `changeBet updates the prediction for an existing bet`() {
        val matches = listOf(
            match(1, "AAA", "BBB", 2, 0)
        )

        BettingService.placeBet(Bet(1, Prediction.HOME_WIN)) // bonus = 1
        BettingService.changeBet(Bet(1, Prediction.HOME_WIN, 2, 0)) // bonus = 3
        val result = BettingService.evaluateBonus(matches)

        assertEquals(result, 3)
    }

    @Test
    fun `changeBet throws when no bet exists for that matchId`() {
        assertThrows<IllegalArgumentException> {
            BettingService.changeBet(Bet(1, Prediction.HOME_WIN))
            require(false) { "ERRORRRRRR" }
        }
    }
}