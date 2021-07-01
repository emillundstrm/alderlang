package com.github.emillundstrm.alderlang.ast

interface Id : Expression, Pattern, TypePattern {
    val name: String
    val strict: Boolean

    companion object {
        fun named(name: String): Id = Identifier(name, false)
        fun named(name: String, strict: Boolean): Id = Identifier(name, strict)
        fun unique(strict: Boolean): Id = UniqueIdentifier(strict)
    }
}

internal class Identifier(override val name: String, override val strict: Boolean) : Id {
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Identifier && other.name == name)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

internal class UniqueIdentifier(override val strict: Boolean) : Id {
    override val name get() = "gen_" + System.identityHashCode(this).toString()

    override fun toString(): String = "UniqueIdentifier($name)"
}
