package example

import java.io.{File, OutputStream}
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.FileIO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object FlickrDownloadMap extends FailFastCirceSupport {

  private val log = LoggerFactory.getLogger(getClass)

  def download(targetDirRoot: String)(photoExif: (FlickrPhoto, List[FlickrExif]))(implicit executor: ExecutionContext, system: ActorSystem, materializer: ActorMaterializer): Future[(FlickrPhoto, List[FlickrExif])] = {

    val (photo, exif) = photoExif
    val promise = Promise[(FlickrPhoto, List[FlickrExif])]()

    val responseFuture = Http().singleRequest(HttpRequest(uri = photo.url_o, method = HttpMethods.GET))
    responseFuture.onComplete {
      case Success(response) =>
        val year :: month :: day :: Nil = photo.datetaken.split("\\s").head.split("-").toList
        log.info(s"year: $year, month: $month, day: $day")
        log.info(s"URL original: ${photo.url_o}")
        val path = Paths.get(targetDirRoot, year, month, day)
        log.info(s"Path: ${path.toFile.getAbsolutePath}")
        path.toFile.mkdirs()
        val file = path.resolve(s"${photo.id}.jpg")
        val byteStringFut = response.entity.withoutSizeLimit().dataBytes.runWith(FileIO.toPath(file))
        // byteStringFut.failed.foreach(println)
        byteStringFut.onComplete {
          case any =>
            log.info(s"Completed download: $any")
            promise.success(photo, exif)
        }

      case Failure(e) =>
        log.error("Could not download or save file: ", e)
        promise.success(photo, exif)
    }

    promise.future
  }

}
