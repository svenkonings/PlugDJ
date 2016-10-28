package dj.plug.plugdj

object Log {
  def v(ref: AnyRef, msg: String): Int = android.util.Log.v(ref.getClass.getSimpleName, msg)

  def v(ref: AnyRef, msg: String, tr: Throwable): Int = android.util.Log.v(ref.getClass.getSimpleName, msg, tr)

  def d(ref: AnyRef, msg: String): Int = android.util.Log.d(ref.getClass.getSimpleName, msg)

  def d(ref: AnyRef, msg: String, tr: Throwable): Int = android.util.Log.d(ref.getClass.getSimpleName, msg, tr)

  def i(ref: AnyRef, msg: String): Int = android.util.Log.i(ref.getClass.getSimpleName, msg)

  def i(ref: AnyRef, msg: String, tr: Throwable): Int = android.util.Log.i(ref.getClass.getSimpleName, msg, tr)

  def w(ref: AnyRef, msg: String): Int = android.util.Log.w(ref.getClass.getSimpleName, msg)

  def w(ref: AnyRef, msg: String, tr: Throwable): Int = android.util.Log.w(ref.getClass.getSimpleName, msg, tr)

  def e(ref: AnyRef, msg: String): Int = android.util.Log.e(ref.getClass.getSimpleName, msg)

  def e(ref: AnyRef, msg: String, tr: Throwable): Int = android.util.Log.e(ref.getClass.getSimpleName, msg, tr)
}
