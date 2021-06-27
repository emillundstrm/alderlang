package com.github.emillundstrm.alderlang.ast

data class Assign(val lhs: Id, val rhs: Expression) : Node {
    override fun toString(): String = "Assign($lhs, $rhs)"
}
