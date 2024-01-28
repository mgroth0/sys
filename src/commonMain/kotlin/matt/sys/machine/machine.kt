package matt.sys.machine

import matt.lang.idea.ProceedingIdea
import matt.lang.shutdown.ShutdownContext
import matt.reflect.tostring.PropReflectingStringableClass


context(ShutdownContext)
abstract class Machine: PropReflectingStringableClass() , ProceedingIdea {

    private var didFirstBoot = false
    private var on: Boolean = false

    final override fun reflectingToStringProps() = setOf(::on)


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