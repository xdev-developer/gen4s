package io.gen4s.generators.render

private[generators] sealed trait Random[+T]:
  protected val rand = scala.util.Random()

  def generate(minLength: Int, maxLength: Int): T

  protected def randomLength(minLength: Int, maxLength: Int): Int =
    if (minLength == maxLength) minLength
    else rand.between(minLength, maxLength)

private[generators] object RandomAnySymbols extends Random[String]:

  def generate(minLength: Int, maxLength: Int): String =
    (for (_ <- 1 to randomLength(minLength, maxLength)) yield rand.nextPrintableChar()).mkString

private[generators] object RandomWord extends Random[String]:

  private val words = scala.io.Source
    .fromResource("words_alpha.txt")
    .getLines
    .toVector
    .groupBy(_.size)
    .toMap

  // the longest word in the dictionary
  private val maxWordLength = 31
  private val minWordLength = 1

  def generate(minLength: Int, maxLength: Int): String = {
    val wordSize   = randomLength(Math.max(minLength, minWordLength), Math.min(maxWordLength, maxLength))
    val sizedWords = words(wordSize)

    val index = rand.between(0, sizedWords.size)

    sizedWords(index)
  }

private[generators] object RandomNumber extends Random[Double]:

  def generate(minLength: Int, maxLength: Int): Double = rand.between(minLength, maxLength + 1)

private[generators] object RandomHEX extends Random[String]:

  private val hexNumbers = "0123456789ABCDEF"

  def generate(minLength: Int, maxLength: Int): String =
    (for (_ <- 1 to randomLength(minLength, maxLength)) yield hexNumbers(rand.nextInt(16))).mkString

private[generators] object RandomIPv4 extends Random[String]:

  // format: "%n{2}.%n{2}.%n{2}.%n{2}."
  def generate(minLength: Int, maxLength: Int): String =
    (for (_ <- 1 to 4) yield rand.nextInt(253) + 1).mkString(".")

private[generators] object RandomIPv6 extends Random[String]:

  // format: "#{4}:#{4}:#{4}:#{4}:#{4}:#{4}:#{4}:#{4}"
  def generate(minLength: Int, maxLength: Int): String =
    (for (_ <- 1 to 8) yield RandomHEX.generate(4, 4)).mkString(":").toLowerCase()

private[generators] object RandomMacAddress extends Random[String]:

  // format: "#{2}:#{2}:#{2}:#{2}:#{2}:#{2}"
  def generate(minLength: Int, maxLength: Int): String =
    (for (_ <- 1 to 6) yield RandomHEX.generate(2, 2)).mkString(":")
