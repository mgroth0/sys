package matt.sys.idgen

class IDGenerator(
  taken: List<Int> = listOf()
): Iterator<Int> {

  private val newIDs = (1..Int.MAX_VALUE)
	.asSequence()
	.filter { it !in taken }
	.iterator()

  override fun hasNext(): Boolean {
	return newIDs.hasNext()
  }

  override fun next(): Int {
	return newIDs.next()
  }

}