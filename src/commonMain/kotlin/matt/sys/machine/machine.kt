package matt.sys.machine

import matt.lang.idea.ProceedingIdea
import matt.lang.shutdown.ShutdownContext
import matt.reflect.tostring.toStringBuilder


context(ShutdownContext)
abstract class Machine() : ProceedingIdea {

    private var didFirstBoot = false
    private var on: Boolean = false

    override fun toString() = toStringBuilder(::on)


    @Synchronized
    fun start() {
        if (!didFirstBoot) {
            duringShutdown {
                shutdown()
            }
        }
        if (!on) boot()
        didFirstBoot = true
        on = true
    }

    @Synchronized
    fun shutdown() {
        println("shutting down $this")
        if (on) unBoot()
        on = false
    }

    @Synchronized
    fun restart() {
        shutdown()
        start()
    }

    protected abstract fun boot()
    protected abstract fun unBoot()
}