package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

case class FlickrExifResult(photo: FlickrExifPhoto)
case class FlickrExifPhoto(exif: List[FlickrExif])
case class FlickrExif(tagspace: String, tag: String, raw: FlickrExifContent, clean: Option[FlickrExifContent])
case class FlickrExifContent(_content: String)

object FlickrExifMap extends FailFastCirceSupport {

  private val log = LoggerFactory.getLogger(getClass)

  def addExif(photo: FlickrPhoto)(implicit executor: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer): Future[(FlickrPhoto, List[FlickrExif])] = {
    val flickrPhotoExifUrl = s"https://api.flickr.com/services/rest/?method=flickr.photos.getExif&api_key=${FlickrSource.apiKey}&photo_id=${photo.id}&secret=${photo.secret}&format=json&nojsoncallback=1"

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = flickrPhotoExifUrl, method = HttpMethods.GET))

    val promise = Promise[(FlickrPhoto, List[FlickrExif])]()

    responseFuture.onComplete {
      case Success(response) =>
        val contentBody = response.entity.withContentType(ContentTypes.`application/json`)
        val unmarshalled: Future[FlickrExifResult] = Unmarshal(contentBody).to[FlickrExifResult]
        unmarshalled.onComplete {
          case Success(flickrExifResult) => promise.success((photo, flickrExifResult.photo.exif))
          case Failure(e) =>
            log.error("No valid JSON", e)
            promise.failure(e)
        }
      case Failure(e) =>
        log.error("Flickr exif failed", e)
        promise.failure(e)
    }

    promise.future
  }

}
