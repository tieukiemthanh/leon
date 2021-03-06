/* Copyright 2009-2013 EPFL, Lausanne */

package leon

class StopwatchCollection(name: String) {
  var acc: Long = 0L

  def +=(sw: Stopwatch) = synchronized { acc += sw.getMillis }

  def getMillis = acc

  override def toString = "%20s: %5dms".format(name, acc)
}

/** Implements a stopwatch for profiling purposes */
class Stopwatch(name: String = "Stopwatch") {
  var beginning: Long = 0L
  var end: Long = 0L
  var acc: Long = 0L

  def start: this.type = {
    beginning = System.currentTimeMillis
    end       = 0L
    this
  }

  def stop {
    end        = System.currentTimeMillis
    acc       += (end - beginning)
    beginning  = 0L
  }

  def getMillis: Long = {
    if (isRunning) {
      acc + (System.currentTimeMillis-beginning)
    } else {
      acc
    }
  }

  def isRunning = beginning != 0L

  override def toString = "%20s: %5d%sms".format(name, getMillis, if (isRunning) "..." else "")
}

object StopwatchCollections {
  private var all = Map[String, StopwatchCollection]()

  def get(name: String): StopwatchCollection = all.getOrElse(name, {
    val sw = new StopwatchCollection(name)
    all += name -> sw
    sw
  })

  def getAll = all
}
