package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLevelProgressionCalculation() {
        val state0 = AppState(totalXp = 0)
        assertEquals(1, state0.userLevel)
        assertEquals(0f, state0.currentLevelProgress, 0.01f)
        assertEquals(150, state0.xpForNextLevel)

        val stateLevel2 = AppState(totalXp = 150)
        assertEquals(2, stateLevel2.userLevel)
        assertEquals(0f, stateLevel2.currentLevelProgress, 0.01f)

        val stateLevel2Half = AppState(totalXp = 225)
        assertEquals(2, stateLevel2Half.userLevel)
        assertEquals(0.5f, stateLevel2Half.currentLevelProgress, 0.01f)
        assertEquals(75, stateLevel2Half.xpForNextLevel)
    }

    @Test
    fun testModeUnlockLogic() {
        val defaultState = AppState(coins = 25)
        // Core free modes are unlocked by default
        assertTrue(defaultState.isModeUnlocked(PersonaType.MALA_JAAP))
        assertTrue(defaultState.isModeUnlocked(PersonaType.GAMES))
        assertTrue(defaultState.isModeUnlocked(PersonaType.LEADERBOARD))
        assertTrue(defaultState.isModeUnlocked(PersonaType.PROFILE))

        // Premium modes require 10 coins daily pass
        val lockedState = AppState(coins = 5, unlockedModes = emptySet(), dailyUnlockedUntil = emptyMap())
        assertFalse(lockedState.isModeUnlocked(PersonaType.BUSINESS))
        assertFalse(lockedState.isModeUnlocked(PersonaType.STUDENT))

        // Daily pass active for 24 hours
        val activePassState = lockedState.copy(
            dailyUnlockedUntil = mapOf(PersonaType.BUSINESS.name to System.currentTimeMillis() + 86400000L)
        )
        assertTrue(activePassState.isModeUnlocked(PersonaType.BUSINESS))

        // Expired daily pass
        val expiredPassState = lockedState.copy(
            dailyUnlockedUntil = mapOf(PersonaType.BUSINESS.name to System.currentTimeMillis() - 1000L)
        )
        assertFalse(expiredPassState.isModeUnlocked(PersonaType.BUSINESS))
    }

    @Test
    fun testInvoiceGstCalculation() {
        val item1 = InvoiceLineItem("Product A", quantity = 2.0, rate = 500.0, gstRatePercent = 18.0)
        assertEquals(1000.0, item1.subtotal, 0.01)
        assertEquals(180.0, item1.taxAmount, 0.01)
        assertEquals(1180.0, item1.totalAmount, 0.01)

        val invoice = Invoice(
            invoiceNumber = "INV-01",
            customerName = "Test Client",
            dateEpochMs = System.currentTimeMillis(),
            items = listOf(item1)
        )
        assertEquals(1000.0, invoice.totalSubtotal, 0.01)
        assertEquals(180.0, invoice.totalTax, 0.01)
        assertEquals(1180.0, invoice.grandTotal, 0.01)
    }

    @Test
    fun testMalaJaapBeadCycle() {
        val mala = MalaJaapState(currentBead = 107, completedMalas = 0)
        assertEquals(107, mala.currentBead)
        val nextBead = mala.currentBead + 1
        assertEquals(108, nextBead)
        // When 108 is reached, a mala is completed
        val finishedMala = mala.copy(currentBead = 0, completedMalas = mala.completedMalas + 1)
        assertEquals(0, finishedMala.currentBead)
        assertEquals(1, finishedMala.completedMalas)
    }
}
