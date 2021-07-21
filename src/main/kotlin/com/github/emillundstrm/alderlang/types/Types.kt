package com.github.emillundstrm.alderlang.types


sealed interface Type

data class TypeScheme(val vars: List<String>, val type: Type): Type

data class TypeVariable(val name: String) : Type {
    override fun toString(): String {
        return name
    }
}

data class NamedType(val name: String, val subTypes: List<Type>) : Type {

    constructor(name: String) : this(name, listOf())

    override fun toString(): String {
        if (subTypes.isNotEmpty()) {
            return "($name ${subTypes.joinToString(" ")})"
        }
        return name
    }
}

data class FunctionType(val from: Type, val to: Type) : Type {
    override fun toString(): String {
        if (from is FunctionType) {
            return "($from) -> $to"
        }
        return "$from -> $to"
    }

    companion object {
        fun binary(a: Type, b: Type, result: Type) = FunctionType(a, FunctionType(b, result))
    }
}

fun sub(type: Type, variable: String, typeExpr: Type): Type {
    return when (type) {
        is TypeVariable -> if (type.name == variable) typeExpr else type
        is FunctionType -> FunctionType(sub(type.from, variable, typeExpr), sub(type.to, variable, typeExpr))
        is NamedType -> NamedType(type.name, type.subTypes.map { sub(it, variable, typeExpr) })
        is TypeScheme -> if (type.vars.contains(variable)) type else TypeScheme(type.vars, sub(type.type, variable, typeExpr))
    }
}

fun interface Substitution {
    fun apply(type: Type): Type
}

object IdSub : Substitution {
    override fun apply(type: Type): Type = type
    override fun toString(): String = "Id"
}

data class Subst(val variable: TypeVariable, val typeExpr: Type) : Substitution {
    override fun apply(type: Type): Type = sub(type, variable.name, typeExpr)
    override fun toString(): String = "Subst($variable => $typeExpr)"
}

data class Compose(val a: Substitution, val b: Substitution) : Substitution {
    override fun apply(type: Type): Type = a.apply(b.apply(type))
    override fun toString(): String = "$a * $b"
}
