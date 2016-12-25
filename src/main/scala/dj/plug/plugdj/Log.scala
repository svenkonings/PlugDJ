package dj.plug.plugdj

import android.util

object Log {
  def v(ref: AnyRef, msg: String): Int = util.Log.v(ref.getClass.getSimpleName, msg)

  def v(ref: AnyRef, msg: String, tr: Throwable): Int = util.Log.v(ref.getClass.getSimpleName, msg, tr)

  def d(ref: AnyRef, msg: String): Int = util.Log.d(ref.getClass.getSimpleName, msg)

  def d(ref: AnyRef, msg: String, tr: Throwable): Int = util.Log.d(ref.getClass.getSimpleName, msg, tr)

  def i(ref: AnyRef, msg: String): Int = util.Log.i(ref.getClass.getSimpleName, msg)

  def i(ref: AnyRef, msg: String, tr: Throwable): Int = util.Log.i(ref.getClass.getSimpleName, msg, tr)

  def w(ref: AnyRef, msg: String): Int = util.Log.w(ref.getClass.getSimpleName, msg)

  def w(ref: AnyRef, msg: String, tr: Throwable): Int = util.Log.w(ref.getClass.getSimpleName, msg, tr)

  def e(ref: AnyRef, msg: String): Int = util.Log.e(ref.getClass.getSimpleName, msg)

  def e(ref: AnyRef, msg: String, tr: Throwable): Int = util.Log.e(ref.getClass.getSimpleName, msg, tr)
}
