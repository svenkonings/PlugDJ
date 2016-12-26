package dj.plug.plugdj

import org.json.JSONArray

package object rooms {
  def concat(arrays: JSONArray*): JSONArray = {
    val result = new JSONArray()
    arrays.foreach(array => for (i <- 0 until array.length()) result.put(array.get(i)))
    result
  }
}
