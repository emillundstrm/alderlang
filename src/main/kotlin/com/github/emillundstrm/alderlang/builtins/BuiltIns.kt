package com.github.emillundstrm.alderlang.builtins

import com.github.emillundstrm.alderlang.ast.Expression
import com.github.emillundstrm.alderlang.ast.IntegerLiteral
import com.github.emillundstrm.alderlang.ast.NativeFunction
import com.github.emillundstrm.alderlang.ast.StringLiteral
import com.github.emillundstrm.alderlang.ast.TypedValue

typealias Eval = (Expression) -> Expression

val equals = NativeFunction { _, a ->
    NativeFunction { eval, b ->
        fromBoolean(checkEquals(eval, a, b)
        )
    }
}

fun checkEquals(eval: (Expression) -> Expression, e1: Expression, e2: Expression): Boolean {
    val v1 = eval(e1)
    val v2 = eval(e2)
    if (v1 == v2) {
        return true
    }
    if (v1 is TypedValue && v2 is TypedValue) {
        // Do we need to check types here?
        // if equals is forAll a => a -> a -> Bool then no
        // if equals is forAll a, b => a -> b - Bool then yes
        return v1.constructor == v2.constructor && v1.args.zip(v2.args).all { (a, b) -> checkEquals(eval, a, b) }
    }
    return false
}

fun evalStrictHelper(eval: (Expression) -> Expression, expr: Expression): Expression {
    val v = eval(expr)
    return if (v is TypedValue) {
        TypedValue(v.constructor, v.args.map { evalStrictHelper(eval, it) })
    } else {
        v
    }
}

val evalStrict = NativeFunction { eval, expr -> evalStrictHelper(eval, expr) }

val seq = NativeFunction { eval, a ->
    NativeFunction { _, b ->
        eval(a)
        eval(b)
    }
}

val makeString = NativeFunction { eval, expr ->
    val sb = StringBuilder()
    var list = eval(expr)
    while (list is TypedValue && list.constructor.name == "Cons") {
        val char = eval(list.args[0]) as IntegerLiteral
        sb.appendCodePoint(char.value)
        list = eval(list.args[1])
    }
    StringLiteral(sb.toString())
}

// Not lazy :(
val chars = NativeFunction { eval, expr ->
    val string = eval(expr)
    if (string is StringLiteral) {
        var list = TypedValue("Nil")
        string.value.reversed().codePoints().forEachOrdered { i -> list = TypedValue("Cons", IntegerLiteral(i), list) }
        list
    } else {
        throw IllegalArgumentException("not a string")
    }
}

object Plus : BinaryMathFunction() {
    override fun apply(a: Int, b: Int): Int = a + b
}

object Minus : BinaryMathFunction() {
    override fun apply(a: Int, b: Int): Int = a - b
}

object Times : BinaryMathFunction() {
    override fun apply(a: Int, b: Int): Int = a * b
}

object Divide : BinaryMathFunction() {
    override fun apply(a: Int, b: Int): Int = a / b
}

object Modulo : BinaryMathFunction() {
    override fun apply(a: Int, b: Int): Int = a % b
}

abstract class BinaryMathFunction : NativeFunction {
    override fun apply(eval: Eval, arg: Expression): Expression {
        return NativeFunction { _, arg2 -> IntegerLiteral(apply(toInt(eval(arg)), toInt(eval(arg2)))) }
    }

    abstract fun apply(a: Int, b: Int): Int
}

fun toInt(e: Expression): Int {
    return (e as IntegerLiteral).value
}

fun fromBoolean(b: Boolean): Expression {
    return if (b) TypedValue("True") else TypedValue("False")
}

object BuiltIns {
    val defs: List<Pair<String, Expression>> = listOf(
        Pair("plus", Plus),
        Pair("minus", Minus),
        Pair("times", Times),
        Pair("divide", Divide),
        Pair("mod", Modulo),
        Pair("chars", chars),
        Pair("string", makeString),
        Pair("strict", evalStrict),
        Pair("seq", seq),
        Pair("eq", equals),
        Pair("println", printLine),
        Pair("chainIO", chainIO),
        Pair("noop", noop)
    )
}