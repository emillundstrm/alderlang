package com.github.emillundstrm.alderlang.ast

interface Id : Expression, Pattern, TypePattern {
    val name: String

    companion object {
        fun named(name: String): Id = Identifier(name)
        fun unique(): Id = UniqueIdentifier()
    }
}

internal data class Identifier(override val name: String) : Id {
    override fun toString(): String = name
}

internal class UniqueIdentifier : Id {
    override val name get() = "gen_" + System.identityHashCode(this).toString()

    override fun toString(): String = "UniqueIdentifier($name)"
}
