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
package com.flipkart.connekt.busybees.streams

import akka.NotUsed
import akka.event.Logging
import akka.stream.scaladsl.{RunnableGraph, Flow, Sink, Source}
import akka.stream.{ActorAttributes, Attributes, Materializer}
import com.flipkart.connekt.commons.iomodels.{CallbackEvent, ConnektRequest}

trait ConnektTopology[E <: CallbackEvent] {

  type CheckPointGroup = String

  def source(checkpointGroup: CheckPointGroup): Source[ConnektRequest, NotUsed]

  def transformers: Map[CheckPointGroup, Flow[ConnektRequest, E, NotUsed]]

  def sink: Sink[E, NotUsed]

  def graphs(): List[RunnableGraph[NotUsed]] = {
    transformers.map(kv => {
      source(kv._1).withAttributes(ActorAttributes.dispatcher("akka.actor.default-pinned-dispatcher")).via(kv._2).to(sink)
        .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel, onFinish = Logging.InfoLevel, onFailure = Logging.ErrorLevel))
    }).toList
  }

  def runGraphs(implicit mat: Materializer) = graphs().foreach(_.run())

  def shutdown()

  def restart(implicit mat: Materializer): Unit = {
    shutdown()
    Thread.sleep(30 * 1000)
    runGraphs(mat)
  }
}
