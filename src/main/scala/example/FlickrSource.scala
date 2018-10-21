package example

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import example.PagedSource.{Page, PagedSource}
import io.circe.generic.auto._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

case class FlickrSearchResult(photos: FlickrSearchPage)

case class FlickrSearchPage(page: Int, pages: Int, perpage: Int, total: Int, photo: List[FlickrPhoto])

case class FlickrPhoto(id: String, owner: String, title: String, farm: Int, server: String, secret: String, datetaken: String, url_o: String, latitude: Float, longitude: Float, pathalias: String, tags: String) {
  val uri: String = "https://www.flickr.com/photos/%s/%s/" format (pathalias, id)
  val thumbnail: String = "http://farm%d.staticflickr.com/%s/%s_%s_%s.jpg" format (farm, server, id, secret, "m" )
}

case class FlickrPageKey(apiKey: String, userId: String, page: Int, total: Int = 0)

object FlickrSource extends FailFastCirceSupport {

  private val log = LoggerFactory.getLogger(getClass)
  val apiKey = "d789a54e10d43196999e1bd64d8a1a72"

  def apply(userId: String)(implicit executor: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer): PagedSource[FlickrPhoto] = {

    val firstKey = FlickrPageKey(apiKey, userId, 1, 1)

    def toPage(page: FlickrSearchPage): Page[FlickrPhoto, FlickrPageKey] = {
      val nextKey = if (page.page < page.pages) Some(FlickrPageKey(apiKey, userId, page.page + 1, page.total)) else None
      page.photo.foreach(println)
      Page(page.photo, nextKey)
    }

    def nextPage(key: FlickrPageKey): Future[Page[FlickrPhoto, FlickrPageKey]] = {
      val flickrPhotoSearchUrl = s"https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=${key.apiKey}&user_id=${key.userId}&format=json&nojsoncallback=1&per_page=20&page=${key.page}&extras=date_taken,geo,url_o,tags,path_alias"

      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = flickrPhotoSearchUrl, method = HttpMethods.GET))

      val promise = Promise[Page[FlickrPhoto, FlickrPageKey]]()

      responseFuture.onComplete {
        case Success(response) =>
          val contentBody = response.entity.withContentType(ContentTypes.`application/json`)
          val unmarshalled: Future[FlickrSearchResult] = Unmarshal(contentBody).to[FlickrSearchResult]
          unmarshalled.onComplete {
            case Success(flickrSearchResult) => promise.success(toPage(flickrSearchResult.photos))
            case Failure(e) =>
              log.error("No valid JSON", e)
              promise.failure(e)
          }
        case Failure(e) =>
          log.error("Flickr search failed", e)
          promise.failure(e)
      }

      promise.future
    }

    PagedSource(firstKey)(nextPage)
  }
}
