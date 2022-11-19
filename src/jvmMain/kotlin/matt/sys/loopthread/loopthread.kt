package matt.sys.loopthread

import matt.log.profile.err.StructuredExceptionHandler
import matt.model.flowlogic.controlflowstatement.ControlFlow
import matt.model.flowlogic.controlflowstatement.ControlFlow.BREAK
import matt.model.flowlogic.controlflowstatement.ControlFlow.CONTINUE
import matt.model.flowlogic.startstop.Stoppable
import kotlin.time.Duration

class MutableRefreshTimeDaemonLoop(
  sleepInterval: Duration,
  op: DaemonLoop.()->ControlFlow,
  finalize: ()->Unit = {},
  uncaughtExceptionHandler: StructuredExceptionHandler? = null
): DaemonLoop(
  sleepInterval,
  op = op,
  finalize = finalize,
  uncaughtExceptionHandler = uncaughtExceptionHandler
) {
  public override var sleepInterval = super.sleepInterval
}

open class DaemonLoop(
  protected open val sleepInterval: Duration,
  private val op: DaemonLoop.()->ControlFlow,
  private val finalize: ()->Unit = {},
  uncaughtExceptionHandler: StructuredExceptionHandler? = null
): Thread(), Stoppable {


  private var shouldContinue: Boolean = true

  override fun sendStopSignal() {
	shouldContinue = false
  }

  override fun stopAndJoin() {
	sendStopSignal()
	join()
  }

  init {
	isDaemon = true
	uncaughtExceptionHandler?.let {
	  this.uncaughtExceptionHandler = it
	}
  }

  final override fun run() {
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
	finalize()
  }
}


fun sleep(duration: Duration, onInterrupt: (InterruptedException)->Unit) {
  try {
	Thread.sleep(duration.inWholeMilliseconds)
  } catch (e: InterruptedException) {
	onInterrupt(e)
  }
}