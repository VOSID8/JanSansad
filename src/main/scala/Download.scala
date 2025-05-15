package com.jansansad

import scalaj.http._
import org.jsoup.Jsoup
import java.io._
import java.net.URL
import javax.net.ssl.{HttpsURLConnection, _}
import java.security.cert.X509Certificate
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors

object download {

  val baseUrl = "https://eparlib.nic.in"
  val outputBase = new File(".")
  val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  val executorService = Executors.newFixedThreadPool(16)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

  val sessionRanges: Seq[(String, LocalDate, LocalDate)] = Seq(
    ("17", LocalDate.parse("18-06-2019", formatter), LocalDate.parse("15-12-2023", formatter)),
    ("16", LocalDate.parse("04-06-2014", formatter), LocalDate.parse("06-02-2019", formatter)),
    ("15", LocalDate.parse("01-06-2009", formatter), LocalDate.parse("21-02-2014", formatter)),
    ("14", LocalDate.parse("02-06-2004", formatter), LocalDate.parse("26-02-2009", formatter)),
    ("13", LocalDate.parse("20-10-1999", formatter), LocalDate.parse("05-02-2004", formatter))
  )

  def trustAllCertificates(): Unit = {
    val trustAllCerts = Array[TrustManager](
      new X509TrustManager {
        override def getAcceptedIssuers(): Array[X509Certificate] = null
        override def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
        override def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = {}
      }
    )
    val sc = SSLContext.getInstance("SSL")
    sc.init(null, trustAllCerts, new SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
    HttpsURLConnection.setDefaultHostnameVerifier((_, _) => true)
  }

  def getPdfLink(detailPageUrl: String): Option[String] = {
    try {
      val response = Http(detailPageUrl).asString.body
      val doc = Jsoup.parse(response)
      val pdfLink = doc.select("a:contains(View/Open)").first()
      if (pdfLink != null) Some(baseUrl + pdfLink.attr("href")) else None
    } catch {
      case e: Exception =>
        println(s"Failed to fetch or parse $detailPageUrl: ${e.getMessage}")
        None
    }
  }

  def extractSessionAndFilename(pdfUrl: String): Option[(String, String)] = {
    val filename = pdfUrl.split("/").lastOption.getOrElse("").trim

    if (!filename.toLowerCase.startsWith("lsd")) {
      println(s"Skipped (filename does not start with 'lsd'): $filename")
      return None
    }

    val datePattern = """(\d{2}-\d{2}-\d{4})""".r
    datePattern.findFirstIn(filename).flatMap { dateStr =>
      val maybeDate = Try(LocalDate.parse(dateStr, formatter)).toOption
      maybeDate.flatMap { date =>
        sessionRanges.find { case (_, start, end) =>
          !date.isBefore(start) && !date.isAfter(end)
        }.map { case (session, _, _) =>
          (session, filename)
        }
      }
    }
  }

  def downloadPdf(url: String, session: String, filename: String): Unit = {
    val sessionDir = new File(outputBase, s"Session$session")
    sessionDir.mkdirs()

    val file = new File(sessionDir, filename)
    if (file.exists()) {
      println(s"File already exists: ${file.getPath}, skipping.")
      return
    }

    try {
      val connection = new URL(url).openConnection()
      val in = new BufferedInputStream(connection.getInputStream)
      val out = new BufferedOutputStream(new FileOutputStream(file))

      val buffer: Array[Byte] = new Array[Byte](64 * 1024)
      Iterator
        .continually(in.read(buffer))
        .takeWhile(_ != -1)
        .foreach(read => out.write(buffer, 0, read))

      out.flush()
      in.close()
      out.close()

      println(s"Downloaded: ${file.getPath}")
    } catch {
      case e: Exception =>
        println(s"Failed to download $url: ${e.getMessage}")
    }
  }

  def main(args: Array[String]): Unit = {
    println("Setting up SSL trust bypass...")
    trustAllCertificates()

    val startHandle = 2963000
    val endHandle = 2990000

    val futures = (startHandle to endHandle).map { handle =>
      Future {
        val detailUrl = s"$baseUrl/handle/123456789/$handle?view_type=browse"
        println(s"\n Processing handle: $handle")

        getPdfLink(detailUrl) match {
          case Some(pdfUrl) =>
            extractSessionAndFilename(pdfUrl) match {
              case Some((session, filename)) =>
                println(s"Valid for Session $session → $filename")
                downloadPdf(pdfUrl, session, filename)
              case None =>
                println(s"Skipped (no valid session date or prefix): $pdfUrl")
            }
          case None =>
            println(s"No PDF link found for handle: $handle")
        }
      }
    }

    Await.result(Future.sequence(futures), Duration.Inf)

    executorService.shutdown()
    println("\n All downloads attempted.")
  }
}
