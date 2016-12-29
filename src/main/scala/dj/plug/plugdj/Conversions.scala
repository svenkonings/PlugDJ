package dj.plug.plugdj

import android.net.Uri
import org.json.JSONObject

import scala.language.implicitConversions

object Conversions {
  implicit def stringToJson(string: String): JSONObject = new JSONObject(string)

  implicit def jsonToString(json: JSONObject): String = json.toString

  implicit def stringToUri(string: String): Uri = Uri.parse(string)

  implicit def uriToString(uri: Uri): String = uri.toString
}
