import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import play.api.libs.json._

object DownloadFlags {
  val browser = JsoupBrowser()

  def parseCountryNames(doc: Document): Seq[String] = {
    //val t = (doc >> elementList("#mw-content-text > div > table > tbody > tr > td:nth-child(2) > a")).filterNot(_.text == "Wikisource")
    val t = (doc >> elementList("#mw-content-text > div > table > tbody > tr > td:nth-child(2) > a")).filterNot(_.text == "Wikisource")
    //t >> attr("href")
    //t >> attr("href")
    t.map(_.text)
  }

  def parseCountryUrls(doc: Document): Seq[String] = {
    //val t = (doc >> elementList("#mw-content-text > div > table > tbody > tr > td:nth-child(2) > a")).filterNot(_.text == "Wikisource")
    val t = (doc >> elementList("#mw-content-text > div > table > tbody > tr > td:nth-child(2) > a")).filterNot(_.text == "Wikisource")
    //t >> attr("href")
    //t >> attr("href")
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
    //#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > div:nth-child(1) > a > img
    //t.map(_ >> attr("src"))
    e.flatMap(x => (x >?> attr("src")).map((_.replaceAll("thumb/", "").split("/125px")(0))))
  }

  def parseCapital(doc: Document, countryName: String): String = {
    //val e = (doc >?> element("#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(6) > td > a"))
    //val e = (doc >?> element("table.infobox:nth-child(2) > tbody:nth-child(1) > tr:nth-child(7) > td:nth-child(2) > a:nth-child(1)"))
    //val f = e.orElse(doc >?> element("table.infobox:nth-child(2) > tbody:nth-child(1) > tr:nth-child(7) > td:nth-child(2) > a:nth-child(1)"))
    //#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > div:nth-child(1) > a > img
    //t.map(_ >> attr("src"))

    //val e = (doc >?> element("#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(6) > td > a"))

    //println(s"doc : ${doc}")
    println(s"countryName : ${countryName}")

    //"#mw-content-text > div > table.infobox.geography.vcard > tbody > tr:nth-child(5) > td > a"
    val trs = (doc >> elementList("#mw-content-text > div > table.infobox.geography.vcard > tbody > tr"))
    println(s"trs : ${trs}")
    trs.map(tr => {
      //if(tr.children.toList.head.text.contains("Capital")) Some((tr >> element("td > a")).text)
      println(s"tr : ${tr.text}")
      if(tr.text.contains("Capital")) (tr >?> text("td > a"))
      else None
    }).flatten.head
  }

  // Flag src on wiki

  def main(args: Array[String]): Unit = {
    //val doc = browser.parseFile("core/src/test/resources/example.html")
    val doc = browser.get("https://en.wikipedia.org/wiki/Member_states_of_the_United_Nations")
    //println(s"Yo: ${doc}")
    //val t = doc >> text(".sortable > tbody:nth-child(2) > tr:nth-child(1) > td:nth-child(2) > a:nth-child(1)")
    //val t = doc >> text(".sortable > tbody:nth-child(2) > tr:nth-child(1) > td:nth-child(2) > a:nth-child(1)")
    //val t = doc >> attr("#mw-content-text > div > table.sortable.wikitable.jquery-tablesorter > tbody > tr:nth-child(1) > td:nth-child(1) > span > a")
    //val t = doc >> text("#mw-content-text > div > table.sortable.wikitable.jquery-tablesorter > tbody > tr:nth-child(1) > td:nth-child(2) > a")

    //println(s"DOC: ${doc}")

    val countryNames = parseCountryNames(doc).slice(1, 150)

    val countryUrls = parseCountryUrls(doc).slice(1,150)
    //println(s"cUrls : ${countryUrls}")

    val cDocs = countryUrls.map(browser.get(_))
    val flagSrcs = cDocs.map(parseFlagSrc)
    //println(s"flagSrcs : ${flagSrcs}")

    val capitals = cDocs.zip(countryNames).map(x => parseCapital(x._1, x._2))
    //println(s"capitals : ${capitals}")

    println("countryUrl,flagSrc,countryName,capital")
    //for((countryUrl, flagSrc, countryName, capital)  <- (countryUrls, flagSrcs, countryNames, capitals).zipped) {
    var i = 0
    val jsonList: scala.collection.mutable.ArrayBuffer[JsObject] = scala.collection.mutable.ArrayBuffer()
    while(i < capitals.length) {
      val (countryUrl, flagSrc, countryName, capital) = (countryUrls(i), flagSrcs(i), countryNames(i), capitals(i))
      println(s"${countryUrl},${flagSrc},${countryName},${capital}")
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
    println(s"json: ${jsonString}")
    //new PrintWriter(") { write(Json.arr(jsonList).toString); close }
    import java.nio.file.{Paths, Files}
    import java.nio.charset.StandardCharsets

    Files.write(Paths.get("server/public/json/countries.json"), jsonString.getBytes(StandardCharsets.UTF_8))

    /*
    val countryNameFlagSrcs = parseCountryNames(doc).zip(parseFlagSrcs(doc))
    countryNameFlagSrcs.foreach {
      println(_)
    }
    */

    /*
    // Need to chop off "Wikisource" but that's it
    val t = (doc >> elementList("#mw-content-text > div > table > tbody > tr > td:nth-child(2) > a")).filterNot(_.text == "Wikisource")
    println(s"Thing : ${t.map(_.text).length}")
    t.foreach { c =>
      println(s"Thing : ${c.text}")
    }
    */

  }
}
