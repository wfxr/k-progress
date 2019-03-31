package com.github.wfxr.kprogress.demo

import com.github.wfxr.kprogress.IProgressState
import com.github.wfxr.kprogress.ProgressBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() = runBlocking<Unit> {
    val tasks = listOf<Long>(100, IProgressState.INDEFINITE, 500, 1010)
    val pb = ProgressBar(
        tasks = tasks,
        title = "Slice",
        refreshInterval = 50)

    val jobs = pb.map {
        launch {
            while (!it.finished()) {
                it.inc(Random.nextLong(0, 20))
                delay(Random.nextLong(0, 20))
            }
        }
    }
    jobs.forEach { it.join() }
    pb.close()
}
