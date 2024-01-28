package matt.sys.loopthread

import matt.async.thread.namedThread
import matt.lang.atomic.AtomicInt
import matt.log.profile.err.StructuredExceptionHandler
import matt.model.flowlogic.controlflowstatement.ControlFlow
import matt.model.flowlogic.controlflowstatement.ControlFlow.BREAK
import matt.model.flowlogic.controlflowstatement.ControlFlow.CONTINUE
import matt.model.flowlogic.startstop.Startable
import matt.model.flowlogic.startstop.Stoppable
import kotlin.time.Duration

class MutableRefreshTimeDaemonLoop(
    sleepInterval: Duration,
    isDaemon: Boolean = true,
    op: DaemonLoop.() -> ControlFlow,
    finalize: () -> Unit = {},
    uncaughtExceptionHandler: StructuredExceptionHandler? = null
) : DaemonLoop(
    sleepInterval,
    isDaemon = isDaemon,
    op = op,
    finalize = finalize,
    uncaughtExceptionHandler = uncaughtExceptionHandler
) {
    public override var sleepInterval = super.sleepInterval
}

open class DaemonLoop(
    protected open val sleepInterval: Duration,
    isDaemon: Boolean = true,
    private val op: DaemonLoop.() -> ControlFlow,
    private val finalize: () -> Unit = {},
    uncaughtExceptionHandler: StructuredExceptionHandler? = null,
) : Stoppable, Startable {

    companion object {
        private val nextID = AtomicInt(0)
    }

    private val id = nextID.getAndIncrement()

    private var shouldContinue: Boolean = true

    final override fun sendStopSignal() {
        shouldContinue = false
    }

    final override fun stopAndJoin() {
        sendStopSignal()
        myThread.join()
    }

    fun interrupt() = myThread.interrupt()

    private val myThread by lazy {
        namedThread(
            start = false, isDaemon = isDaemon, name = "DaemonLoop $id"
        ) {

            if (sleepInterval == Duration.ZERO) {
                while (shouldContinue) {
                    when (op()) {
                        CONTINUE -> continue
                        BREAK    -> break
                    }
                }
            } else {
                while (shouldContinue) {
                    when (op()) {
                        CONTINUE -> {
                            sleep(sleepInterval) {
                                if (shouldContinue) throw it
                            }
                        }

                        BREAK    -> break
                    }
                }
            }









            finalize()
        }.apply {
            uncaughtExceptionHandler?.let {
                this.uncaughtExceptionHandler = it
            }
        }
    }

    final override fun sendStartSignal() {
        myThread.start()
    }

    final override fun startAndJoin() {
        myThread.start()
    }
}


fun sleep(
    duration: Duration,
    onInterrupt: (InterruptedException) -> Unit
) {
    try {
        Thread.sleep(duration.inWholeMilliseconds)
    } catch (e: InterruptedException) {
        onInterrupt(e)
    }
}