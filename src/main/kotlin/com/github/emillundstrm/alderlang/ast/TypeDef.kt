package com.github.emillundstrm.alderlang.ast

sealed interface TypePattern

data class TypeName(val name: Id, val typeVars: List<Id>) : Node, TypePattern {
    constructor(name: Id): this(name, listOf())
}

data class TypeDef(val name: TypeName, val constructors: List<DataConstructor>) : Node

// do we need this?
// Cons a b === a -> b -> TypedValue(Cons, a, b)
data class DataConstructor(val name: Id, val value: Expression) : Node

data class TypedValue(val constructor: Id, val args: List<Expression>) :
    Expression {
    constructor(name: Id, vararg args: Expression): this(name, args.toList())
    constructor(name: String, vararg args: Expression): this(Id.named(name), args.toList())
}