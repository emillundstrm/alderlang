package com.github.emillundstrm.alderlang.ast

data class Case(val test: Expression, val cases: List<Function>) : Expression
