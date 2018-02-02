object DownloadFlags extends App {

  val browser = JsoupBrowser()
  //val doc = browser.parseFile("core/src/test/resources/example.html")
  val doc2 = browser.get("https://en.wikipedia.org/wiki/Gallery_of_sovereign_state_flags")


  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
}
