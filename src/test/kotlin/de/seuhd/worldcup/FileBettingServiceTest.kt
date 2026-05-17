package de.seuhd.worldcup

import org.junit.jupiter.api.BeforeEach
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder

import java.util.UUID

/** Tests for [FileBettingService]. */
@TestMethodOrder(MethodOrderer.Random::class)
class FileBettingServiceTest {

    // flakky - due to thread1/2 running simultaneously, either wait for thread1 to finish before running thread2, or add a threadlock
    @Test
    fun `test file betting with threads`() {
        val file = createTempFile("bets", ".txt").toFile()
        val service = FileBettingService(file)

        val thread1 = Thread {
            repeat(50) { i -> service.placeBet(Bet(i, Prediction.HOME_WIN)) }
        }
        val thread2 = Thread {
            repeat(50) { i -> service.placeBet(Bet(i + 50, Prediction.AWAY_WIN)) }
        }

        thread1.start()
        thread1.join()
        thread2.start()
        thread2.join()

        // Each thread placed 50 unique bets → 100 total expected.
        assertEquals(100, service.getBets().size)

        file.delete()
    }

    companion object {
        // PID makes the filename unique per JVM launch.
        var SHARED_BET_FILE = File(
            System.getProperty("java.io.tmpdir"),
            "worldcup-shared-bets-${ProcessHandle.current().pid()}-${this::class::simpleName}-${object{}.javaClass.enclosingMethod?.name}-${UUID.randomUUID()}.txt"
        )
    }

    // code copilot - more unique identifiers
    @BeforeEach
    fun setup() {
        SHARED_BET_FILE = File(
            System.getProperty("java.io.tmpdir"),
            "worldcup-shared-bets-${ProcessHandle.current().pid()}-${this::class::simpleName}-${object{}.javaClass.enclosingMethod?.name}-${UUID.randomUUID()}.txt"
        )
    }

    @Test
    fun `save bets to the shared file`() {
        val service = FileBettingService(SHARED_BET_FILE)
        service.placeBet(Bet(1, Prediction.HOME_WIN))
        service.placeBet(Bet(2, Prediction.DRAW))
        service.placeBet(Bet(3, Prediction.AWAY_WIN))
        val bets = service.getBets()
        assertEquals(3, bets.size)
    }

    // flakky - make shared file more unique
    @Test
    fun `fresh service has no bets`() {
        val service = FileBettingService(SHARED_BET_FILE)
        assertEquals(0, service.getBets().size)
    }
}
