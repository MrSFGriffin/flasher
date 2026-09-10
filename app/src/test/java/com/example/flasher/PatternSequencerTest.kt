package com.example.flasher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternSequencerTest {

    @Test
    fun steady_isSingleOnStepAndDoesNotLoop() {
        val steps = PatternSequencer.sequenceFor(TorchPattern.STEADY)

        assertEquals(listOf(PatternStep(true, 0L)), steps)
        assertFalse(PatternSequencer.loops(TorchPattern.STEADY))
    }

    @Test
    fun strobe_alternatesOnOffAtFixedInterval() {
        val steps = PatternSequencer.sequenceFor(TorchPattern.STROBE)

        assertEquals(
            listOf(
                PatternStep(true, 300L),
                PatternStep(false, 300L)
            ),
            steps
        )
        assertTrue(PatternSequencer.loops(TorchPattern.STROBE))
    }

    @Test
    fun sos_matchesStandardMorseTimingForDotsDashesAndGaps() {
        val steps = PatternSequencer.sequenceFor(TorchPattern.SOS)
        val dot = 200L
        val dash = 600L
        val intraGap = 200L
        val letterGap = 600L
        val wordGap = 1400L

        val expected = listOf(
            // S: three dots
            PatternStep(true, dot), PatternStep(false, intraGap),
            PatternStep(true, dot), PatternStep(false, intraGap),
            PatternStep(true, dot), PatternStep(false, letterGap),
            // O: three dashes
            PatternStep(true, dash), PatternStep(false, intraGap),
            PatternStep(true, dash), PatternStep(false, intraGap),
            PatternStep(true, dash), PatternStep(false, letterGap),
            // S: three dots, then a word gap before the pattern repeats
            PatternStep(true, dot), PatternStep(false, intraGap),
            PatternStep(true, dot), PatternStep(false, intraGap),
            PatternStep(true, dot), PatternStep(false, wordGap)
        )

        assertEquals(expected, steps)
        assertTrue(PatternSequencer.loops(TorchPattern.SOS))
    }

    @Test
    fun sos_dashIsThreeTimesTheDotDuration() {
        val steps = PatternSequencer.sequenceFor(TorchPattern.SOS)
        val dotDuration = steps.first { it.torchOn }.durationMs
        val dashDuration = steps.first { it.torchOn && it.durationMs != dotDuration }.durationMs

        assertEquals(dotDuration * 3, dashDuration)
    }

    @Test
    fun simulatedPlaybackOfTwoStrobeCyclesProducesExpectedOnOffTimeline() {
        val steps = PatternSequencer.sequenceFor(TorchPattern.STROBE)
        val loop = PatternSequencer.loops(TorchPattern.STROBE)

        val timeline = simulateTimeline(steps, loop, cycles = 2)

        assertEquals(
            listOf(
                true to 300L, false to 300L,
                true to 300L, false to 300L
            ),
            timeline
        )
    }

    @Test
    fun simulatedPlaybackOfSosProducesCorrectTotalCycleDuration() {
        val steps = PatternSequencer.sequenceFor(TorchPattern.SOS)
        val loop = PatternSequencer.loops(TorchPattern.SOS)

        val timeline = simulateTimeline(steps, loop, cycles = 1)
        val totalDuration = timeline.sumOf { it.second }

        // 6 dots (200ms) + 3 dashes (600ms) + 6 intra-letter gaps (200ms, 2 per letter)
        // + 2 letter gaps (600ms) + 1 word gap (1400ms)
        val expectedTotal = 6 * 200L + 3 * 600L + 6 * 200L + 2 * 600L + 1 * 1400L
        assertEquals(expectedTotal, totalDuration)
    }

    /** Walks [steps] for [cycles] full loops the same way [TorchPatternPlayer] would. */
    private fun simulateTimeline(
        steps: List<PatternStep>,
        loop: Boolean,
        cycles: Int
    ): List<Pair<Boolean, Long>> {
        val timeline = mutableListOf<Pair<Boolean, Long>>()
        var index = 0
        var completedCycles = 0
        while (completedCycles < cycles) {
            val step = steps[index]
            timeline.add(step.torchOn to step.durationMs)
            index++
            if (index >= steps.size) {
                index = 0
                completedCycles++
                if (!loop) break
            }
        }
        return timeline
    }
}
