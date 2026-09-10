package com.example.flasher

import android.os.Handler

class TorchPatternPlayer(
    private val handler: Handler,
    private val setTorch: (Boolean) -> Unit
) {

    private var generation = 0

    fun start(pattern: TorchPattern) {
        stop()
        val myGeneration = generation
        val steps = PatternSequencer.sequenceFor(pattern)
        val loop = PatternSequencer.loops(pattern)
        runStep(steps, 0, loop, myGeneration)
    }

    fun stop() {
        generation++
        handler.removeCallbacksAndMessages(null)
        setTorch(false)
    }

    private fun runStep(steps: List<PatternStep>, index: Int, loop: Boolean, myGeneration: Int) {
        if (myGeneration != generation || steps.isEmpty()) return
        val step = steps[index]
        setTorch(step.torchOn)

        val nextIndex = index + 1
        val hasNext = nextIndex < steps.size || loop
        if (!hasNext) return

        handler.postDelayed({
            if (myGeneration != generation) return@postDelayed
            val next = if (nextIndex < steps.size) nextIndex else 0
            runStep(steps, next, loop, myGeneration)
        }, step.durationMs)
    }
}
