import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import play.api.libs.json._

object DownloadFlags {
  val browser = JsoupBrowser()

  def parseCountryNames(doc: Document): Seq[String] = {
    val t = (doc >> elementList("#mw-content-text > div > table > tbody > tr > td:nth-child(2) > a")).filterNot(_.text == "Wikisource")
    t.map(_.text)
  }

  def parseCountryUrls(doc: Document): Seq[String] = {
    val t = (doc >> elementList("#mw-content-text > div > table > tbody > tr > td:nth-child(2) > a")).filterNot(_.text == "Wikisource")
    t.map(x => {
      val res = x >> attr("href")
      s"https://en.wikipedia.org${res}"
    })
  }

  def getCountryDoc(url: String): Document = {
    browser.get(s"https://en.wikipedia.org${url}")
  }

  def parseFlagSrc(doc: Document): Option[String] = {
    val e = (doc >?> element("#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > div:nth-child(1) > a > img"))
                              //#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(2) > td > div:nth-child(1) > div > a > img



    e.flatMap(x => (x >?> attr("src")).map { x =>
      val replaced = x.replaceAll("thumb/", "")
      val splitStr = if(replaced.contains("125px")) "/125px" else "/102px"
      replaced.split(splitStr)(0)
    }).orElse {
      val e = (doc >?> element("#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(2) > td > div:nth-child(1) > div > a > img")).flatMap { x =>
        (x >?> attr("src")).map { x =>
          (x.replaceAll("thumb/", "").split("/125px")(0))
        }
      }
      println(s"e : ${e}")
      e
    }
  }

  def parseCapital(doc: Document, countryName: String): String = {
    val trs = (doc >> elementList("#mw-content-text > div > table.infobox.geography.vcard > tbody > tr"))
    trs.map(tr => {
      if(tr.text.contains("Capital")) (tr >?> text("td > a"))
      else None
    }).flatten.head
  }

  def main(args: Array[String]): Unit = {
    val doc = browser.get("https://en.wikipedia.org/wiki/Member_states_of_the_United_Nations")

    val countryNames = parseCountryNames(doc)

    val countryUrls = parseCountryUrls(doc)

    val cDocs = countryUrls.map(browser.get(_))
    val flagSrcs = cDocs.map(parseFlagSrc)

    val capitals = cDocs.zip(countryNames).map(x => parseCapital(x._1, x._2))

    var i = 0
    val jsonList: scala.collection.mutable.ArrayBuffer[JsObject] = scala.collection.mutable.ArrayBuffer()
    while(i < capitals.length) {
      val (countryUrl, flagSrc, countryName, capital) = (countryUrls(i), flagSrcs(i), countryNames(i), capitals(i))
      //println(s"${countryUrl},${flagSrc},${countryName},${capital}")
      val j = Json.obj(
        "countryUrl" -> countryUrl,
        "flagSrc" -> flagSrc,
        "countryName" -> countryName,
        "capital" -> capital
      )
      jsonList += j
      i += 1
    }

    val jsonString = JsArray(jsonList.toList).toString
    import java.nio.file.{Paths, Files}
    import java.nio.charset.StandardCharsets

    Files.write(Paths.get("server/public/json/countries.json"), jsonString.getBytes(StandardCharsets.UTF_8))
  }
}
