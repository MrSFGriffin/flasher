package com.example.flasher

// Pure timing logic, no Android dependencies, so it's testable via plain JVM unit tests.
object PatternSequencer {

    const val STROBE_INTERVAL_MS = 300L

    // Standard Morse code timing, expressed in dot units.
    const val DOT_MS = 200L
    const val DASH_MS = DOT_MS * 3
    const val INTRA_LETTER_GAP_MS = DOT_MS
    const val INTER_LETTER_GAP_MS = DOT_MS * 3
    const val WORD_GAP_MS = DOT_MS * 7

    // Whether the sequence should repeat from index 0 once it finishes.
    fun loops(pattern: TorchPattern): Boolean = pattern != TorchPattern.STEADY

    fun sequenceFor(pattern: TorchPattern): List<PatternStep> = when (pattern) {
        TorchPattern.STEADY -> listOf(PatternStep(torchOn = true, durationMs = 0L))
        TorchPattern.STROBE -> listOf(
            PatternStep(true, STROBE_INTERVAL_MS),
            PatternStep(false, STROBE_INTERVAL_MS)
        )
        TorchPattern.SOS -> sosSequence()
    }

    private fun sosSequence(): List<PatternStep> {
        val dot = PatternStep(true, DOT_MS)
        val dash = PatternStep(true, DASH_MS)
        val intraGap = PatternStep(false, INTRA_LETTER_GAP_MS)
        val letterGap = PatternStep(false, INTER_LETTER_GAP_MS)
        val wordGap = PatternStep(false, WORD_GAP_MS)

        return listOf(
            // S
            dot, intraGap, dot, intraGap, dot, letterGap,
            // O
            dash, intraGap, dash, intraGap, dash, letterGap,
            // S
            dot, intraGap, dot, intraGap, dot, wordGap
        )
    }
}
