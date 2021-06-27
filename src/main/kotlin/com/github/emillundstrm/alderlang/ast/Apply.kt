package com.github.emillundstrm.alderlang.ast

data class Apply(val func: Expression, val arg: Expression) : Expression {

    var cachedValue: Expression? = null

    override fun toString(): String = "($func $arg)"
}
