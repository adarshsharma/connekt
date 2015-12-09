package com.flipkart.connekt.receptors.routes.pn

import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.stream.ActorMaterializer
import com.flipkart.connekt.commons.factories.{ConnektLogger, LogFile, ServiceFactory}
import com.flipkart.connekt.commons.iomodels._
import com.flipkart.connekt.receptors.routes.BaseHandler

import scala.collection.immutable.Seq
import scala.util.{Failure, Success}

/**
 *
 *
 * @author durga.s
 * @version 11/26/15
 */
class Unicast(implicit _am: ActorMaterializer) extends BaseHandler {
  implicit val am = _am

  val unicast =
    sniffHeaders { headers =>
      isAuthenticated(Some(headers)) {
        path("v1" / "push" / "unicast" / Segment / Segment / Segment) {
          (appPlatform: String, appName:String, deviceId: String) =>
            post {
              entity(as[ConnektRequest]) { r =>
                val pnData = r.channelData.asInstanceOf[PNRequestData].copy(appName = appName, platform = appPlatform, deviceId = deviceId)
                val unicastRequest = r.copy(channelData = pnData, channelStatus = PNStatus("QUEUED", ""))

                ConnektLogger(LogFile.SERVICE).debug("Received unicast PN request with payload: ${r.toString}")
                def enqueue = ServiceFactory.getMessageService.persistRequest(unicastRequest, "fk-connekt-pn", isCrucial = true)
                async(enqueue) {
                  case Success(t) => t match {
                    case Success(requestId) =>
                      complete(respond[GenericResponse](
                        StatusCodes.Created, Seq.empty[HttpHeader],
                        GenericResponse(StatusCodes.OK.intValue, null, Response("PN Request en-queued successfully for %s".format(requestId), null))
                      ))
                    case Failure(e) =>
                      complete(respond[GenericResponse](
                        StatusCodes.InternalServerError, Seq.empty[HttpHeader],
                        GenericResponse(StatusCodes.InternalServerError.intValue, null, Response("PN Request processing failed: %s".format(e.getMessage), null))
                      ))
                  }
                  case Failure(e) =>
                    complete(respond[GenericResponse](
                      StatusCodes.InternalServerError, Seq.empty[HttpHeader],
                      GenericResponse(StatusCodes.InternalServerError.intValue, null, Response("PN Request processing failed: %s".format(e.getMessage), null))
                    ))
                }
              }
            }
        }
      }
    }
}