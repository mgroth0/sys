package matt.sys.loopthread

import matt.log.profile.StructuredExceptionHandler
import matt.model.flowlogic.controlflowstatement.ControlFlow
import matt.model.flowlogic.controlflowstatement.ControlFlow.BREAK
import matt.model.flowlogic.controlflowstatement.ControlFlow.CONTINUE
import matt.model.idea.ProceedingIdea
import kotlin.time.Duration

class MutableRefreshTimeDaemonLoop(
  sleep: Duration,
  op: DaemonLoop.()->ControlFlow,
  finalize: ()->Unit = {},
  uncaughtExceptionHandler: StructuredExceptionHandler? = null
): DaemonLoop(sleep, op = op, finalize = finalize, uncaughtExceptionHandler = uncaughtExceptionHandler) {
  public override var sleepInterval = super.sleepInterval
}

open class DaemonLoop(
  protected open val sleepInterval: Duration,
  private val op: DaemonLoop.()->ControlFlow,
  private val finalize: ()->Unit = {},
  uncaughtExceptionHandler: StructuredExceptionHandler? = null
): Thread(), ProceedingIdea {


  private var shouldContinue: Boolean = true

  val wasSignaledToStop get() = !shouldContinue

  fun signalToStop() {
	shouldContinue = false
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
	  finalize()
	}
  }
}


fun sleep(duration: Duration, onInterrupt: (InterruptedException)->Unit) {
  try {
	Thread.sleep(duration.inWholeMilliseconds)
  } catch (e: InterruptedException) {
	onInterrupt(e)
  }
}