package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class FlickrSourceSpec extends FlatSpec with Matchers {

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer()

//  "The FlickrSource object" should "get the first photo from Flickr" in {
//    val source = FlickrSource("48503061@N00")
//    val head = source.runWith(Sink.head)
//    val result = Await.result(head, 5 seconds)
//    println(result.thumbnail)
//    println(result.uri)
//    println(result.url_o)
//    // head.thumbnail should equal "hi"
//  }

//  "The FlickrSource object" should "get the exif data for the first photo from Flickr" in {
//    val source = FlickrSource("48503061@N00")
//    val sourceWithExif = source.mapAsync(1)(FlickrExifMap.addExif)
//    val head = sourceWithExif.runWith(Sink.head)
//    val result = Await.result(head, 5 seconds)
//    println(result._1.thumbnail)
//    println(result._1.uri)
//    println(result._1.url_o)
//    println(result._2.foreach(println))
//    // head.thumbnail should equal "hi"
//  }

  "The FlickrSource object" should "get the exif data for the first photo from Flickr" in {
    val source = FlickrSource("48503061@N00")
    val sourceWithExifDownloaded = source.mapAsync(1)(FlickrExifMap.addExif).mapAsync(1)(FlickrDownloadMap.download("/tmp/flickr"))
    val all = sourceWithExifDownloaded.runWith(Sink.seq)

    Await.result(all, Duration.Inf)

//    val head = sourceWithExifDownloaded.runWith(Sink.head)
//    val result = Await.result(head, 20 seconds)
//    println(result._1.thumbnail)
//    println(result._1.uri)
//    println(result._1.url_o)
//    // println(result._2.foreach(println))
//    // head.thumbnail should equal "hi"
//    Thread.sleep(10000)
  }

}
