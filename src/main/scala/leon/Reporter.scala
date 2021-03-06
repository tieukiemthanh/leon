/* Copyright 2009-2013 EPFL, Lausanne */

package leon

import purescala.Definitions.Definition
import purescala.Trees.Expr
import purescala.PrettyPrinter

abstract class Reporter(enabledSections: Set[ReportingSection]) {
  def infoFunction(msg: Any) : Unit
  def warningFunction(msg: Any) : Unit
  def errorFunction(msg: Any) : Unit
  def fatalErrorFunction(msg: Any) : Nothing
  def debugFunction(msg: Any) : Unit

  // This part of the implementation is non-negociable.
  private var _errorCount : Int = 0
  private var _warningCount : Int = 0

  final def errorCount : Int = _errorCount
  final def warningCount : Int = _warningCount

  final def info(msg: Any) = infoFunction(msg)
  final def warning(msg: Any) = {
    _warningCount += 1
    warningFunction(msg)
  }
  final def error(msg: Any) = {
    _errorCount += 1
    errorFunction(msg)
  }
  final def fatalError(msg: Any) = {
    _errorCount += 1
    fatalErrorFunction(msg)
  }

  val debugMask = enabledSections.foldLeft(0){ _ | _.mask }

  final def debug(section: ReportingSection)(msg: => Any) = {
    ifDebug(section) { debugFunction(msg) }
  }

  final def ifDebug(section: ReportingSection)(body: => Unit) = {
    if ((debugMask & section.mask) == section.mask) {
      body
    }
  }
}


class DefaultReporter(enabledSections: Set[ReportingSection] = Set()) extends Reporter(enabledSections) {
  protected val errorPfx   = "[ Error ] "
  protected val warningPfx = "[Warning] "
  protected val infoPfx    = "[ Info  ] "
  protected val fatalPfx   = "[ Fatal ] "
  protected val debugPfx   = "[ Debug ] "

  def output(msg: String) : Unit = println(msg)

  protected def reline(pfx: String, msg: String) : String = {
    val color = if(pfx == errorPfx || pfx == fatalPfx) {
      Console.RED
    } else if(pfx == warningPfx) {
      Console.YELLOW
    } else if(pfx == debugPfx) {
      Console.MAGENTA
    } else {
      Console.BLUE
    }
    "[" + color + pfx.substring(1, pfx.length-2) + Console.RESET + "] " +
    msg.replaceAll("\n", "\n" + (" " * (pfx.size)))
  }

  def errorFunction(msg: Any) = output(reline(errorPfx, msg.toString))
  def warningFunction(msg: Any) = output(reline(warningPfx, msg.toString))
  def infoFunction(msg: Any) = output(reline(infoPfx, msg.toString))
  def fatalErrorFunction(msg: Any) = { output(reline(fatalPfx, msg.toString)); throw LeonFatalError() }
  def debugFunction(msg: Any) = output(reline(debugPfx, msg.toString))
}

sealed abstract class ReportingSection(val name: String, val mask: Int)

case object ReportingSolver    extends ReportingSection("solver",    1 << 0)
case object ReportingSynthesis extends ReportingSection("synthesis", 1 << 1)
case object ReportingTimers    extends ReportingSection("timers",    1 << 2)
case object ReportingOptions   extends ReportingSection("options",   1 << 3)

object ReportingSections {
  val all = Set[ReportingSection](
    ReportingSolver,
    ReportingSynthesis,
    ReportingTimers,
    ReportingOptions
  )
}
