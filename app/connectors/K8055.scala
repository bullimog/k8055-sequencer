package connectors


import connectors.Configuration._
import model.{Device, DeviceState}
import play.Logger
import play.api.http.HttpVerbs
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws.{WS, WSRequest, WSResponse}
import play.mvc.Http.HeaderNames._
import play.mvc.Http.MimeTypes._
import scala.concurrent.Future

//had to add these two in, to get WS stuff to work
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global



object K8055 extends K8055

trait K8055{
  def doGet(url: String): Option[Future[WSResponse]] = {
    val holder: WSRequest = WS.url(url)
    try {Some(holder.get())}
    catch {
      case _: java.lang.NullPointerException => None
    }
  }


  def doPut(url: String, body:DeviceState): Option[Future[WSResponse]] = {
    val request: WSRequest = WS.url(url).withMethod(HttpVerbs.PUT).withHeaders(CONTENT_TYPE -> JSON)
    try {Some(request.put(Json.toJson(body)))}
    catch {
      case e: java.lang.NullPointerException => {
        Logger.error(s"Null pointer in K8055Connector.doPut: url:$url; body:$body; request: $request; exception: $e")
        None
      }
    }
  }

  def patchDeviceState(deviceState: DeviceState): Unit = {
    doPut(k8055Host + k8055DeviceState, deviceState).fold(Future(false)) {
      theFuture => theFuture.flatMap { wsresponse =>    // get the WSResponse out of the Future using map
        wsresponse.status match {                       // match on the response status code (int)
          case OK => Future(true)
          case _ => {
            Logger.error(s"K8055Connector: Status response from K8055 was $wsresponse")
            Future(false)}
        }
      }
    }
  }

  //TODO: Need to tidy this up - default devices are not ideal
  def getDevice(deviceId: String):Future[Device]  = {
     doGet(k8055Host + k8055Device + deviceId).fold(Future(Device("0", "No response from server",0, 0))) {
      theFuture => theFuture.map { wsresponse =>           // get the WSResponse out of the Future using map
        wsresponse.status match {                          // match on the response status code (int)
          case OK => parseDevice(wsresponse.body).getOrElse(Device("0", "Device incompatible",0, 0))
          case NOT_FOUND => Device("0", "*Not found*",0, 0)
          case _ => Device("0", "Error - unknown device type",0, 0)
        }
      }
    }
  }

  def parseDevice(jsonSource:String):Option[Device] = {
    val json: JsValue = Json.parse(jsonSource)
    json.validate[Device] match {
      case s: JsSuccess[Device] => Some(s.getOrElse{
        Logger.error(s"K8055Connector.parseDevice: parsed Json, but it was empty: $jsonSource")
        Device("0", "Bad Device 5",0, 0)
      })
      case e: JsError => None
    }
  }

}
