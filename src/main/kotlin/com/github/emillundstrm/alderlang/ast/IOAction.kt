package com.github.emillundstrm.alderlang.ast

import com.github.emillundstrm.alderlang.builtins.Eval

fun interface IOAction : Expression {
    fun perform(eval: Eval): Expression
}
