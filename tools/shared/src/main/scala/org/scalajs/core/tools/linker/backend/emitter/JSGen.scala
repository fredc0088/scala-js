/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js tools             **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2017, LAMP/EPFL        **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-js.org/       **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */


package org.scalajs.core.tools.linker.backend.emitter

import scala.language.implicitConversions

import scala.annotation.tailrec

import org.scalajs.core.ir
import ir._
import ir.Types._
import ir.{Trees => irt}

import org.scalajs.core.tools.sem._
import org.scalajs.core.tools.linker.backend.{ModuleKind, OutputMode}
import org.scalajs.core.tools.javascript.Trees._

/** Collection of tree generators that are used accross the board.
 *  This class is fully stateless.
 *
 *  Also carries around config (semantics and outputMode).
 */
private[emitter] final class JSGen(val semantics: Semantics,
    val outputMode: OutputMode, val moduleKind: ModuleKind,
    internalOptions: InternalOptions,
    mentionedDangerousGlobalRefs: Set[String]) {

  import JSGen._

  def genZeroOf(tpe: Type)(implicit pos: Position): Tree = {
    tpe match {
      case BooleanType => BooleanLiteral(false)
      case IntType     => IntLiteral(0)
      case LongType    => genLongZero()
      case FloatType   => DoubleLiteral(0.0)
      case DoubleType  => DoubleLiteral(0.0)
      case StringType  => StringLiteral("")
      case UndefType   => Undefined()
      case _           => Null()
    }
  }

  def genLongZero()(implicit pos: Position): Tree = {
    genLongModuleApply(LongImpl.Zero)
  }

  def genLongModuleApply(methodName: String, args: Tree*)(
      implicit pos: Position): Tree = {
    import TreeDSL._
    Apply(
        genLoadModule(LongImpl.RuntimeLongModuleClass) DOT methodName,
        args.toList)
  }

  def genLet(name: Ident, mutable: Boolean, rhs: Tree)(
      implicit pos: Position): LocalDef = {
    outputMode match {
      case OutputMode.ECMAScript51Isolated =>
        VarDef(name, Some(rhs))
      case OutputMode.ECMAScript6 =>
        Let(name, mutable, Some(rhs))
    }
  }

  def genEmptyMutableLet(name: Ident)(implicit pos: Position): LocalDef = {
    outputMode match {
      case OutputMode.ECMAScript51Isolated =>
        VarDef(name, rhs = None)
      case OutputMode.ECMAScript6 =>
        Let(name, mutable = true, rhs = None)
    }
  }

  def genSelectStatic(className: String, item: irt.Ident)(
      implicit pos: Position): Tree = {
    envField("t", className + "__" + item.name)
  }

  def genIsInstanceOf(expr: Tree, typeRef: TypeRef)(
      implicit pos: Position): Tree =
    genIsAsInstanceOf(expr, typeRef, test = true)

  def genAsInstanceOf(expr: Tree, typeRef: TypeRef)(
      implicit pos: Position): Tree =
    genIsAsInstanceOf(expr, typeRef, test = false)

  private def genIsAsInstanceOf(expr: Tree, typeRef: TypeRef, test: Boolean)(
      implicit pos: Position): Tree = {
    import Definitions._
    import TreeDSL._

    typeRef match {
      case ClassRef(className0) =>
        val className =
          if (className0 == BoxedLongClass) LongImpl.RuntimeLongClass
          else className0

        if (HijackedBoxedClasses.contains(className)) {
          if (test) {
            className match {
              case BoxedUnitClass    => expr === Undefined()
              case BoxedBooleanClass => typeof(expr) === "boolean"
              case BoxedByteClass    => genCallHelper("isByte", expr)
              case BoxedShortClass   => genCallHelper("isShort", expr)
              case BoxedIntegerClass => genCallHelper("isInt", expr)
              case BoxedFloatClass   => genCallHelper("isFloat", expr)
              case BoxedDoubleClass  => typeof(expr) === "number"
            }
          } else {
            className match {
              case BoxedUnitClass    => genCallHelper("asUnit", expr)
              case BoxedBooleanClass => genCallHelper("asBoolean", expr)
              case BoxedByteClass    => genCallHelper("asByte", expr)
              case BoxedShortClass   => genCallHelper("asShort", expr)
              case BoxedIntegerClass => genCallHelper("asInt", expr)
              case BoxedFloatClass   => genCallHelper("asFloat", expr)
              case BoxedDoubleClass  => genCallHelper("asDouble", expr)
            }
          }
        } else {
          Apply(
              envField(if (test) "is" else "as", className),
              List(expr))
        }

      case ArrayTypeRef(base, depth) =>
        Apply(
            envField(if (test) "isArrayOf" else "asArrayOf", base),
            List(expr, IntLiteral(depth)))
    }
  }

  def genCallHelper(helperName: String, args: Tree*)(
      implicit pos: Position): Tree = {
    Apply(envField(helperName), args.toList)
  }

  def encodeClassVar(className: String)(implicit pos: Position): Tree =
    envField("c", className)

  def genLoadModule(moduleClass: String)(implicit pos: Position): Tree = {
    import TreeDSL._
    Apply(envField("m", moduleClass), Nil)
  }

  def genRawJSClassConstructor(className: String,
      keepOnlyDangerousVarNames: Boolean)(
      implicit globalKnowledge: GlobalKnowledge,
      pos: Position): WithGlobals[Tree] = {

    genRawJSClassConstructor(className,
        globalKnowledge.getJSNativeLoadSpec(className),
        keepOnlyDangerousVarNames)
  }

  def genRawJSClassConstructor(className: String,
      spec: Option[irt.JSNativeLoadSpec],
      keepOnlyDangerousVarNames: Boolean)(
      implicit pos: Position): WithGlobals[Tree] = {
    spec match {
      case None =>
        // This is a non-native JS class
        WithGlobals(genNonNativeJSClassConstructor(className))

      case Some(spec) =>
        genLoadJSFromSpec(spec, keepOnlyDangerousVarNames)
    }
  }

  def genNonNativeJSClassConstructor(className: String)(
      implicit pos: Position): Tree = {
    Apply(envField("a", className), Nil)
  }

  def genLoadJSFromSpec(spec: irt.JSNativeLoadSpec,
      keepOnlyDangerousVarNames: Boolean)(
      implicit pos: Position): WithGlobals[Tree] = {

    def pathSelection(from: Tree, path: List[String]): Tree = {
      path.foldLeft(from) {
        (prev, part) => genBracketSelect(prev, StringLiteral(part))
      }
    }

    spec match {
      case irt.JSNativeLoadSpec.Global(globalRef, path) =>
        val globalVarRef = VarRef(Ident(globalRef, Some(globalRef)))
        val globalVarNames = {
          if (keepOnlyDangerousVarNames && !GlobalRefUtils.isDangerousGlobalRef(globalRef))
            Set.empty[String]
          else
            Set(globalRef)
        }
        WithGlobals(pathSelection(globalVarRef, path), globalVarNames)

      case irt.JSNativeLoadSpec.Import(module, path) =>
        val moduleValue = envModuleField(module)
        path match {
          case DefaultExportName :: rest =>
            val defaultField = genCallHelper("moduleDefault", moduleValue)
            WithGlobals(pathSelection(defaultField, rest))
          case _ =>
            WithGlobals(pathSelection(moduleValue, path))
        }

      case irt.JSNativeLoadSpec.ImportWithGlobalFallback(importSpec, globalSpec) =>
        moduleKind match {
          case ModuleKind.NoModule =>
            genLoadJSFromSpec(globalSpec, keepOnlyDangerousVarNames)
          case ModuleKind.CommonJSModule =>
            genLoadJSFromSpec(importSpec, keepOnlyDangerousVarNames)
        }
    }
  }

  def genArrayValue(tpe: ArrayType, elems: List[Tree])(
      implicit pos: Position): Tree = {
    genCallHelper("makeNativeArrayWrapper", genClassDataOf(tpe.arrayTypeRef),
        ArrayConstr(elems))
  }

  def genClassDataOf(typeRef: TypeRef)(implicit pos: Position): Tree = {
    typeRef match {
      case ClassRef(className) =>
        envField("d", className)
      case ArrayTypeRef(base, dims) =>
        (1 to dims).foldLeft[Tree](envField("d", base)) { (prev, _) =>
          Apply(DotSelect(prev, Ident("getArrayOf")), Nil)
        }
    }
  }

  def envModuleField(module: String)(implicit pos: Position): VarRef = {
    /* This is written so that the happy path, when `module` contains only
     * valid characters, is fast.
     */

    def isValidChar(c: Char): Boolean =
      (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')

    def containsOnlyValidChars(): Boolean = {
      // scalastyle:off return
      val len = module.length
      var i = 0
      while (i != len) {
        if (!isValidChar(module.charAt(i)))
          return false
        i += 1
      }
      true
      // scalastyle:on return
    }

    def buildValidName(): String = {
      val result = new java.lang.StringBuilder("$i_")
      val len = module.length
      var i = 0
      while (i != len) {
        val c = module.charAt(i)
        if (isValidChar(c))
          result.append(c)
        else
          result.append("$%04x".format(c.toInt))
        i += 1
      }
      result.toString()
    }

    val varName =
      if (containsOnlyValidChars()) "$i_" + module
      else buildValidName()

    VarRef(Ident(avoidClashWithGlobalRef(varName), Some(module)))
  }

  def envField(field: String, subField: String, origName: Option[String] = None)(
      implicit pos: Position): VarRef = {
    VarRef(Ident(avoidClashWithGlobalRef("$" + field + "_" + subField),
        origName))
  }

  def envField(field: String)(implicit pos: Position): VarRef =
    VarRef(Ident(avoidClashWithGlobalRef("$" + field)))

  def avoidClashWithGlobalRef(envFieldName: String): String = {
    /* This is not cached because it should virtually never happen.
     * slowPath() is only called if we use a dangerous global ref, which should
     * already be very rare. And if do a second iteration in the loop only if
     * we refer to the global variables `$foo` *and* `$$foo`. At this point the
     * likelihood is so close to 0 that caching would be more expensive than
     * not caching.
     */
    @tailrec
    def slowPath(lastNameTried: String): String = {
      val nextNameToTry = "$" + lastNameTried
      if (mentionedDangerousGlobalRefs.contains(nextNameToTry))
        slowPath(nextNameToTry)
      else
        nextNameToTry
    }

    /* Hopefully this is JIT'ed away as `false` because
     * `mentionedDangerousGlobalRefs` is in fact `Set.EmptySet`.
     */
    if (mentionedDangerousGlobalRefs.contains(envFieldName))
      slowPath(envFieldName)
    else
      envFieldName
  }

  def genPropSelect(qual: Tree, item: PropertyName)(
      implicit pos: Position): Tree = {
    item match {
      case item: Ident         => DotSelect(qual, item)
      case item: StringLiteral => genBracketSelect(qual, item)
      case ComputedName(tree)  => genBracketSelect(qual, tree)
    }
  }

  def genBracketSelect(qual: Tree, item: Tree)(implicit pos: Position): Tree = {
    item match {
      case StringLiteral(name) if internalOptions.optimizeBracketSelects &&
          irt.isValidIdentifier(name) && name != "eval" =>
        /* We exclude "eval" because we do not want to rely too much on the
         * strict mode peculiarities of eval(), so that we can keep running
         * on VMs that do not support strict mode.
         */
        DotSelect(qual, Ident(name))
      case _ =>
        BracketSelect(qual, item)
    }
  }

  def genIdentBracketSelect(qual: Tree, item: String)(
      implicit pos: Position): Tree = {
    require(item != "eval")
    if (internalOptions.optimizeBracketSelects)
      DotSelect(qual, Ident(item))
    else
      BracketSelect(qual, StringLiteral(item))
  }
}

private object JSGen {
  private final val ScalaJSEnvironmentName = "ScalaJS"
  private final val DefaultExportName = "default"
}
