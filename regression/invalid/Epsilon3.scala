import leon.Utils._

object Epsilon3 {

  def posWrong(): Int = {
    epsilon((y: Int) => y >= 0)
  } ensuring(_ > 0)

}
