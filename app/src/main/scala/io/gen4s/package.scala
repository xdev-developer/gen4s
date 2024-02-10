package io

// $COVERAGE-OFF$
package object gen4s {
  def greenOut(msg: String): String = s"${scala.Console.GREEN}$msg${scala.Console.RESET}"
  def redOut(msg: String): String   = s"${scala.Console.RED}- $msg${scala.Console.RESET}"
}
// $COVERAGE-ON$
