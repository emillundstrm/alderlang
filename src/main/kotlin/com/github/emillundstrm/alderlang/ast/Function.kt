package com.github.emillundstrm.alderlang.ast

data class Function(val argument: Pattern, val body: Expression) : Expression {
    override fun toString(): String = "$argument -> $body"
}

fun interface NativeFunction : Expression {
    fun apply(eval: (Expression) -> Expression, arg: Expression): Expression
}
