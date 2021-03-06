/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js API               **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013, LAMP/EPFL        **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-lang.org/     **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */


/**
 * All doc-comments marked as "MDN" are by Mozilla Contributors,
 * distributed under the Creative Commons Attribution-ShareAlike license from
 * https://developer.mozilla.org/en-US/docs/Web/Reference/API
 */
package scala.scalajs.js

import scala.language.implicitConversions

import scala.scalajs.js

import scala.collection.mutable
import scala.collection.generic.CanBuildFrom

/** Root of the hierarchy of JavaScript types.
 *
 *  Subtypes of [[Any js.Any]] are JavaScript types, which have different
 *  semantics and guarantees than Scala types (subtypes of [[AnyRef]] and
 *  [[AnyVal]]). Operations on JavaScript types behave as the corresponding
 *  operations in the JavaScript language.
 *
 *  You can implement JavaScript types in Scala.js. The implementation
 *  (i.e., the method and constructor bodies) will follow Scala semantics, but
 *  the constructor and methods will be called using JavaScript semantics
 *  (e.g., runtime dispatch).
 *
 *  A JavaScript type that is annotated with [[native @js.native]] is a facade
 *  type to APIs implemented in JavaScript code. Its implementation is
 *  irrelevant and never emitted. As such, all members must be defined with
 *  their right-hand-side being [[native js.native]].
 *  Further, native JavaScript types must be annotated with one of
 *  [[annotation.JSGlobal @JSGlobal]], [[annotation.JSImport @JSImport]],
 *  [[annotation.JSGlobalScope @JSGlobalScope]] to specify where to fetch it
 *  from.
 *
 *  In most cases, you should not directly extend this trait, but rather extend
 *  [[Object js.Object]].
 *
 *  It is not possible to define traits or classes that inherit both from this
 *  trait and a strict subtype of [[AnyRef]]. In fact, you should think of
 *  [[Any js.Any]] as a third direct subclass of [[scala.Any]], besides
 *  [[scala.AnyRef]] and [[scala.AnyVal]].
 *
 *  See the [[http://www.scala-js.org/doc/js-interoperability.html JavaScript
 *  interoperability guide]] of Scala.js for more details.
 */
trait Any extends scala.AnyRef

/** Provides implicit conversions from Scala values to JavaScript values. */
object Any extends js.LowPrioAnyImplicits {
  @inline implicit def fromUnit(value: Unit): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromBoolean(value: Boolean): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromByte(value: Byte): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromShort(value: Short): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromInt(value: Int): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromFloat(value: Float): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromDouble(value: Double): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromString(s: String): js.Any =
    s.asInstanceOf[js.Any]

  /* The following overload makes sure that the developer does not
   * inadvertently convert a Long to a Double to fit it in a js.Any.
   */
  @deprecated(
      "A Long is converted to Double to be cast to js.Any. " +
      "This is almost certainly not what you want. " +
      "Use `.toDouble` explicitly if you need it.",
      "forever")
  @inline
  implicit def fromLong(value: Long): js.Any =
    value.toDouble.asInstanceOf[js.Any]

  implicit def jsArrayOps[A](array: js.Array[A]): js.ArrayOps[A] =
    new js.ArrayOps(array)

  implicit def canBuildFromArray[A]: CanBuildFrom[Array[_], A, js.Array[A]] = {
    @inline
    class CanBuildFromArray extends CanBuildFrom[Array[_], A, js.Array[A]] {
      def apply(from: js.Array[_]): mutable.Builder[A, js.Array[A]] =
        new js.ArrayOps[A]
      def apply(): mutable.Builder[A, js.Array[A]] =
        new js.ArrayOps[A]
    }
    new CanBuildFromArray
  }

  // scalastyle:off line.size.limit

  /* identity() is important! It prevents the tail-rec treatment in the absence
   * of SAM treatment.
   *
   * When compiling without SAM treatment, the implicit expansion results in a
   * self-recursive call to the implicit conversion. Without `identity()`, this
   * recursive call is in tail position, and the tailcalls phase will convert
   * it into an infinite loop.
   *
   * With `identity()`, the recursive call is not in tail position. The
   * back-end therefore receives a call to the implicit conversion, *which is
   * a primitive*, and therefore applies its primitive treatment,
   * short-circuiting the infinite recursion.
   *
   * With proper SAM treatment, none of this happens, since there is no
   * implicit materialization of a recursive call in the first place.
   */
  implicit def fromFunction0[R](f: scala.Function0[R]): js.Function0[R] = identity(() => f())
  implicit def fromFunction1[T1, R](f: scala.Function1[T1, R]): js.Function1[T1, R] = identity((x1: T1) => f(x1))
  implicit def fromFunction2[T1, T2, R](f: scala.Function2[T1, T2, R]): js.Function2[T1, T2, R] = identity((x1: T1, x2: T2) => f(x1, x2))
  implicit def fromFunction3[T1, T2, T3, R](f: scala.Function3[T1, T2, T3, R]): js.Function3[T1, T2, T3, R] = identity((x1: T1, x2: T2, x3: T3) => f(x1, x2, x3))
  implicit def fromFunction4[T1, T2, T3, T4, R](f: scala.Function4[T1, T2, T3, T4, R]): js.Function4[T1, T2, T3, T4, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4) => f(x1, x2, x3, x4))
  implicit def fromFunction5[T1, T2, T3, T4, T5, R](f: scala.Function5[T1, T2, T3, T4, T5, R]): js.Function5[T1, T2, T3, T4, T5, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5) => f(x1, x2, x3, x4, x5))
  implicit def fromFunction6[T1, T2, T3, T4, T5, T6, R](f: scala.Function6[T1, T2, T3, T4, T5, T6, R]): js.Function6[T1, T2, T3, T4, T5, T6, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6) => f(x1, x2, x3, x4, x5, x6))
  implicit def fromFunction7[T1, T2, T3, T4, T5, T6, T7, R](f: scala.Function7[T1, T2, T3, T4, T5, T6, T7, R]): js.Function7[T1, T2, T3, T4, T5, T6, T7, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7) => f(x1, x2, x3, x4, x5, x6, x7))
  implicit def fromFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R](f: scala.Function8[T1, T2, T3, T4, T5, T6, T7, T8, R]): js.Function8[T1, T2, T3, T4, T5, T6, T7, T8, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8) => f(x1, x2, x3, x4, x5, x6, x7, x8))
  implicit def fromFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f: scala.Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]): js.Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9))
  implicit def fromFunction10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](f: scala.Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]): js.Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10))
  implicit def fromFunction11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](f: scala.Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]): js.Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11))
  implicit def fromFunction12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](f: scala.Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]): js.Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12))
  implicit def fromFunction13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](f: scala.Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]): js.Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13))
  implicit def fromFunction14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](f: scala.Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]): js.Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14))
  implicit def fromFunction15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](f: scala.Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]): js.Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15))
  implicit def fromFunction16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](f: scala.Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]): js.Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15, x16: T16) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16))
  implicit def fromFunction17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](f: scala.Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]): js.Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15, x16: T16, x17: T17) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17))
  implicit def fromFunction18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](f: scala.Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]): js.Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15, x16: T16, x17: T17, x18: T18) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18))
  implicit def fromFunction19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](f: scala.Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]): js.Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15, x16: T16, x17: T17, x18: T18, x19: T19) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19))
  implicit def fromFunction20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](f: scala.Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]): js.Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15, x16: T16, x17: T17, x18: T18, x19: T19, x20: T20) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20))
  implicit def fromFunction21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](f: scala.Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]): js.Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15, x16: T16, x17: T17, x18: T18, x19: T19, x20: T20, x21: T21) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21))
  implicit def fromFunction22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](f: scala.Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]): js.Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R] = identity((x1: T1, x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12, x13: T13, x14: T14, x15: T15, x16: T16, x17: T17, x18: T18, x19: T19, x20: T20, x21: T21, x22: T22) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21, x22))

  implicit def toFunction0[R](f: js.Function0[R]): scala.Function0[R] = () => f()
  implicit def toFunction1[T1, R](f: js.Function1[T1, R]): scala.Function1[T1, R] = (x1) => f(x1)
  implicit def toFunction2[T1, T2, R](f: js.Function2[T1, T2, R]): scala.Function2[T1, T2, R] = (x1, x2) => f(x1, x2)
  implicit def toFunction3[T1, T2, T3, R](f: js.Function3[T1, T2, T3, R]): scala.Function3[T1, T2, T3, R] = (x1, x2, x3) => f(x1, x2, x3)
  implicit def toFunction4[T1, T2, T3, T4, R](f: js.Function4[T1, T2, T3, T4, R]): scala.Function4[T1, T2, T3, T4, R] = (x1, x2, x3, x4) => f(x1, x2, x3, x4)
  implicit def toFunction5[T1, T2, T3, T4, T5, R](f: js.Function5[T1, T2, T3, T4, T5, R]): scala.Function5[T1, T2, T3, T4, T5, R] = (x1, x2, x3, x4, x5) => f(x1, x2, x3, x4, x5)
  implicit def toFunction6[T1, T2, T3, T4, T5, T6, R](f: js.Function6[T1, T2, T3, T4, T5, T6, R]): scala.Function6[T1, T2, T3, T4, T5, T6, R] = (x1, x2, x3, x4, x5, x6) => f(x1, x2, x3, x4, x5, x6)
  implicit def toFunction7[T1, T2, T3, T4, T5, T6, T7, R](f: js.Function7[T1, T2, T3, T4, T5, T6, T7, R]): scala.Function7[T1, T2, T3, T4, T5, T6, T7, R] = (x1, x2, x3, x4, x5, x6, x7) => f(x1, x2, x3, x4, x5, x6, x7)
  implicit def toFunction8[T1, T2, T3, T4, T5, T6, T7, T8, R](f: js.Function8[T1, T2, T3, T4, T5, T6, T7, T8, R]): scala.Function8[T1, T2, T3, T4, T5, T6, T7, T8, R] = (x1, x2, x3, x4, x5, x6, x7, x8) => f(x1, x2, x3, x4, x5, x6, x7, x8)
  implicit def toFunction9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f: js.Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]): scala.Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9)
  implicit def toFunction10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](f: js.Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]): scala.Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10)
  implicit def toFunction11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](f: js.Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]): scala.Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11)
  implicit def toFunction12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](f: js.Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]): scala.Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12)
  implicit def toFunction13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](f: js.Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]): scala.Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13)
  implicit def toFunction14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](f: js.Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]): scala.Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14)
  implicit def toFunction15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](f: js.Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]): scala.Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15)
  implicit def toFunction16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](f: js.Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]): scala.Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16)
  implicit def toFunction17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](f: js.Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]): scala.Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17)
  implicit def toFunction18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](f: js.Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]): scala.Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18)
  implicit def toFunction19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](f: js.Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]): scala.Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19)
  implicit def toFunction20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](f: js.Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]): scala.Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20)
  implicit def toFunction21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](f: js.Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]): scala.Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21)
  implicit def toFunction22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](f: js.Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]): scala.Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R] = (x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21, x22) => f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21, x22)
  // scalastyle:on line.size.limit

  @inline implicit def fromJBoolean(value: java.lang.Boolean): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromJByte(value: java.lang.Byte): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromJShort(value: java.lang.Short): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromJInteger(value: java.lang.Integer): js.Any =
    value.asInstanceOf[js.Any]

  /* The following overload makes sure that the developer does not
   * inadvertently convert a Long to a Double to fit it in a js.Any.
   */
  @deprecated(
      "A Long is converted to Double to be cast to js.Any. " +
      "This is almost certainly not what you want. " +
      "Use `.toDouble` explicitly if you need it.",
      "forever")
  @inline
  implicit def fromJLong(value: java.lang.Long): js.Any =
    if (value eq null) null
    else value.doubleValue.asInstanceOf[js.Any]

  @inline implicit def fromJFloat(value: java.lang.Float): js.Any =
    value.asInstanceOf[js.Any]
  @inline implicit def fromJDouble(value: java.lang.Double): js.Any =
    value.asInstanceOf[js.Any]
}

sealed trait LowPrioAnyImplicits extends js.LowestPrioAnyImplicits {
  this: js.Any.type =>

  implicit def wrapArray[A](array: js.Array[A]): js.WrappedArray[A] =
    new js.WrappedArray(array)
  implicit def wrapDictionary[A](dict: js.Dictionary[A]): js.WrappedDictionary[A] =
    new js.WrappedDictionary(dict)
}

sealed trait LowestPrioAnyImplicits {
  this: js.Any.type =>

  implicit def iterableOps[A](iterable: js.Iterable[A]): js.IterableOps[A] =
    new js.IterableOps(iterable)
}
