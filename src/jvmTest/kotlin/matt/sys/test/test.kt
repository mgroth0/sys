package matt.sys.test


import matt.sys.idgen.IDGenerator
import matt.test.JupiterTestAssertions.assertRunsInOneMinute
import kotlin.test.Test

class SysTests() {
    @Test
    fun genSomeIds() = assertRunsInOneMinute {
        IDGenerator(listOf(1, 2, 3)).next()
    }
}