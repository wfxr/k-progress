package com.github.wfxr.kprogress

import com.github.wfxr.kprogress.IProgressState.Companion.INDEFINITE
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.DurationFormatUtils
import java.io.PrintStream
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

class ProgressBar private constructor(
        val title: String?,
        speedFormat: String,
        val etaFormat: String,
        printStream: PrintStream,
        refreshInterval: Long,
        states: List<IProgressState>) : List<IProgressState> by states, AutoCloseable {

    private val refreshTimer: Timer
    private val terminal = DefaultTerminalFactory(printStream, System.`in`, Charset.defaultCharset())
        .setForceTextTerminal(true)
        .createTerminal()
    private val tui = terminal.newTextGraphics()
    private var pos = terminal.cursorPosition
    private val speedFormat = DecimalFormat(speedFormat)
    private val percentageFormat = DecimalFormat("00.00%")

    init {
        initTerminal()
        assert(refreshInterval > 0) { "refresh interval should be a positive integer" }
        refreshTimer = fixedRateTimer(period = refreshInterval) {
            refresh()
        }
    }

    constructor(tasks: List<Long> = listOf(INDEFINITE),
                title: String? = null,
                speedFormat: String = "0.00/s",
                etaFormat: String = "HH:mm:ss",
                printStream: PrintStream = System.err,
                refreshInterval: Long = 1000) :
            this(title, speedFormat, etaFormat, printStream, refreshInterval, tasks.map { ProgressState(it) })

    private fun initTerminal() {
        Runtime.getRuntime().addShutdownHook(thread(false) {
            close()
            synchronized(terminal) {
                terminal.putCharacter('\n')
                terminal.setCursorVisible(true)
            }
        })
        GlobalScope.launch {
            @Suppress("BlockingMethodInNonBlockingContext")
            while (true) {
                terminal.putCharacter(terminal.readInput().character)
            }
        }
        terminal.setCursorVisible(false)
        repeat(size - 1) {
            terminal.putCharacter('\n')
        }
        pos = terminal.cursorPosition.withRelativeRow(1 - size).withColumn(0)
    }

    override fun close() {
        refreshTimer.cancel()
        refresh()
    }

    private fun refresh() {
        val titles = titles()
        val titlesLen = titles.maxBy { it.length }!!.length
        val fractions = totals()
        val fractionsLen = fractions.maxBy { it.length }!!.length
        val speeds = speeds()
        val speedsLen = speeds.maxBy { it.length }!!.length
        val etas = etas()
        val etasLen = etas.maxBy { it.length }!!.length
        val percentages = percentages()
        val percentagesLen = percentages.maxBy { it.length }!!.length

        val progressesLen = terminal.terminalSize.columns - fractionsLen - titlesLen - speedsLen - etasLen - percentagesLen - 7
        val progresses = progresses(progressesLen)

        for (id in 0 until size) {
            val pos = pos.withRelativeRow(id).withColumn(0)
            synchronized(terminal) {
                tui.putString(pos,
                              "%${titlesLen}s %${fractionsLen}s %${speedsLen}s %${etasLen}s [%-${progressesLen}s] %${percentagesLen}s".format(
                                  titles[id],
                                  fractions[id],
                                  speeds[id],
                                  etas[id],
                                  progresses[id],
                                  percentages[id]))
            }
        }
    }

    private fun percentages() =
            this.map {
                when (val p = it.percentage()) {
                    null -> "???"
                    else -> percentageFormat.format(p)
                }

            }

    private fun totals() =
            this.map {
                when (val total = it.total) {
                    null -> "???"
                    else -> "$total"
                }
            }

    private fun titles() =
            (0 until size).map { id ->
                "$title-$id"
            }

    private fun speeds() =
            this.map {
                speedFormat.format(it.speed())
            }

    private fun etas() =
            this.map {
                val eta = it.eta()
                when (eta) {
                    null -> "???"
                    else -> {
                        val etaMilli = eta.toMillis()
                        if (etaMilli < 0) {
                            println(etaMilli)
                        }
                        DurationFormatUtils.formatDuration(eta.toMillis(), etaFormat, true)
                    }
                }
            }

    private fun progresses(size: Int) =
            this.map {
                val percentage = it.percentage()
                val p = when {
                    percentage == null -> return@map ">".repeat(size)
                    percentage < 0     -> 0.0
                    percentage > 1     -> 1.0
                    else               -> percentage
                }
                val count = (p * size).toInt()
                when {
                    count < size -> "${"#".repeat(count)}>"
                    count > 0    -> "#".repeat(count)
                    else         -> ""
                }
            }
}

