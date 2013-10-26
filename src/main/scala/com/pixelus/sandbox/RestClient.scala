import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import org.apache.http.impl.client.{BasicResponseHandler, HttpClientBuilder}
import org.apache.http.message.{BasicNameValuePair, BasicHeader}

object RestClient {
  var params: Map[String, List[String]] = _;
  var url: String = _;

  def parseArgs(args: Array[String]): Map[String, List[String]] = {

    def nameValuePair(paramName: String) = {
      def values(commaSeparatedValues: String) = commaSeparatedValues.split(",").toList

      val index = args.indexWhere(_ == paramName)
      (paramName, if (index == -1) Nil else values(args(index + 1)))
    }

    Map(nameValuePair("-d"), nameValuePair("-h"))
  }

  def splitByEqual(nameValue: String): Array[String] = nameValue.split("=")

  def headers = for (nameValue <- params("-h")) yield {
    def tokens = splitByEqual(nameValue)
    new BasicHeader(tokens(0), tokens(1))
  }

  def formEntity = {
    def toJavaList(scalaList: List[BasicNameValuePair]) = {
      java.util.Arrays.asList(scalaList.toArray:_*)
    }

    def formParams = for (nameValue <- params("-d")) yield {
      def tokens = splitByEqual(nameValue)
      new BasicNameValuePair(tokens(0), tokens(1))
    }

    def formEntity = new UrlEncodedFormEntity(toJavaList(formParams), "UTF-8")
    formEntity
  }

  def handleGetRequest = {
    val query = params("-d").mkString("&")
    val httpGet = new HttpGet(s"${url}?${query}")
    headers.foreach {
      httpGet.addHeader(_)
    }

    val responseBody = HttpClientBuilder.create().build().execute(httpGet, new BasicResponseHandler())
    println(responseBody)
  }

  def handlePostRequest = {
    val httpPost = new HttpPost(url);
    headers.foreach {
      httpPost.addHeader(_)
    }
    httpPost.setEntity(formEntity)
    val responseBody = HttpClientBuilder.create().build().execute(httpPost, new BasicResponseHandler())
    println(responseBody)
  }

  def main(args: Array[String]) {
    require(args.size >= 2, "at least you should specify action[get,post] and url")
    val command = args.head
    params = parseArgs(args)
    url = args.last

    command match {
      case "get"  => handleGetRequest
      case "post" => handlePostRequest
    }
  }
}