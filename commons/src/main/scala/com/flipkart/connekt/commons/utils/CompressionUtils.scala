/*
 *         -╥⌐⌐⌐⌐            -⌐⌐⌐⌐-
 *      ≡╢░░░░⌐\░░░φ     ╓╝░░░░⌐░░░░╪╕
 *     ╣╬░░`    `░░░╢┘ φ▒╣╬╝╜     ░░╢╣Q
 *    ║╣╬░⌐        ` ╤▒▒▒Å`        ║╢╬╣
 *    ╚╣╬░⌐        ╔▒▒▒▒`«╕        ╢╢╣▒
 *     ╫╬░░╖    .░ ╙╨╨  ╣╣╬░φ    ╓φ░╢╢Å
 *      ╙╢░░░░⌐"░░░╜     ╙Å░░░░⌐░░░░╝`
 *        ``˚¬ ⌐              ˚˚⌐´
 *
 *      Copyright © 2016 Flipkart.com
 */
package com.flipkart.connekt.commons.utils

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Base64
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.flipkart.connekt.commons.core.Wrappers._
import org.apache.commons.io.IOUtils

import scala.util.Try
import scala.util.control.NoStackTrace

object CompressionUtils {

  def inflate(deflatedTxt: String): Try[String] = Try_ {
    val bytes = Base64.getUrlDecoder.decode(deflatedTxt)
    try {
      val zipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))
      IOUtils.toString(zipInputStream)
    } catch {
      case ex:java.util.zip.ZipException =>
        throw new Exception(ex.getMessage) with NoStackTrace
    }
  }

  def deflate(txt: String): Try[String] = Try_ {
    val arrOutputStream = new ByteArrayOutputStream()
    val zipOutputStream = new GZIPOutputStream(arrOutputStream)
    zipOutputStream.write(txt.getBytes)
    zipOutputStream.close()
    Base64.getUrlEncoder.encodeToString(arrOutputStream.toByteArray)
  }

  implicit class StringCompress(val s: String) {
    def compress: Try[String] = deflate(s)
    def decompress: Try[String] = inflate(s)
  }


}
