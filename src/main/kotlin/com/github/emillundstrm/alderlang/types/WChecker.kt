package com.github.emillundstrm.alderlang.types

import com.github.emillundstrm.alderlang.ast.*
import com.github.emillundstrm.alderlang.ast.Function
import java.lang.IllegalArgumentException

typealias TypeEnvironment = Map<Id, TypeScheme>

class WChecker {

    private var binder = 0

    private fun createVar(): TypeVariable {
        return TypeVariable("t${++binder}")
    }

    fun check(env: TypeEnvironment, expr: Expression): Pair<Substitution, Type> {
        return when (expr) {
            is Id -> Pair(IdSub, instantiate(env, expr))
            is Function -> {
                val argTypeVars = mutableMapOf<Id, TypeScheme>()
                val argType = typeForPattern(expr.argument, argTypeVars)
                val (sub, bodyType) = check(env + argTypeVars, expr.body)
                Pair(sub, FunctionType(sub.apply(argType), bodyType))
            }
            is Apply -> {
                val typeVar = createVar()
                val (sub1, funcType) = check(env, expr.func)
                val (sub2, argType) = check(env.mapValues { sub1.apply(it.value) as TypeScheme }, expr.arg)
                val sub3 = unify(sub2.apply(funcType), FunctionType(argType, typeVar))
                Pair(Compose(sub3, Compose(sub2, sub1)), sub3.apply(typeVar))
            }
            is DecimalLiteral -> Pair(IdSub, NamedType("Decimal"))
            is IntegerLiteral -> Pair(IdSub, NamedType("Int"))
            is StringLiteral -> Pair(IdSub, NamedType("String"))
            else -> {
                throw IllegalArgumentException("Not yet implemented $expr")
            }
        }
    }

    private fun instantiate(env: TypeEnvironment, expr: Id): Type {
        val schema = env[expr] ?: throw IllegalArgumentException("No type known for $expr")
        return schema.vars.fold(schema.type) { acc, oldVar -> sub(acc, oldVar, createVar()) }
    }

    private fun unify(left: Type, right: Type): Substitution {
        return if (left == right) {
            IdSub
        } else if (left is TypeVariable) {
            unifyVar(left, right)
        } else if (right is TypeVariable) {
            unifyVar(right, left)
        } else if (left is FunctionType && right is FunctionType) {
            // TODO: FunctionType can probably be NamedType("Function", listOf(from, to))
            val s1 = unify(left.from, right.from)
            val s2 = unify(s1.apply(left.to), s1.apply(right.to))
            Compose(s1, s2)
        } else if (left is NamedType && right is NamedType && left.name == right.name) {
            // Unify all "subtypes" to build a complete substitution
            left.subTypes.zip(right.subTypes).fold(IdSub as Substitution) { acc, (a, b) ->
                Compose(acc, unify(acc.apply(a), acc.apply(b)))
            }
        } else {
            throw IllegalArgumentException("Cannot unify $left and $right")
        }
    }

    private fun unifyVar(typeVar: TypeVariable, term: Type): Substitution {
        if (occursIn(typeVar, term)) {
            throw IllegalArgumentException("$typeVar occurs in $term")
        }
        return Subst(typeVar, term)
    }

    private fun occursIn(left: Type, right: Type): Boolean {
        if (left == right) {
            return true;
        }
        return when (right) {
            is FunctionType -> occursIn(left, right.from) || occursIn(left, right.to)
            is NamedType -> false
            is TypeVariable -> false
            is NamedType -> right.subTypes.any { occursIn(left, it) }
            else -> false
        }
    }

    private fun typeForPattern(pattern: Pattern, newVariables: MutableMap<Id, TypeScheme>): Type {
        return when (pattern) {
            is Id -> {
                val variable = createVar()
                newVariables[pattern] = TypeScheme(listOf(), variable)
                return variable
            }
            // FIXME: constructor.name !== type.name... also constructor vars are not necessarily type vars right?
            is ConstructorPattern -> NamedType(pattern.constructor.name, pattern.vars.map { typeForPattern(it, newVariables) })
            is DecimalLiteral -> NamedType("Decimal")
            is IntegerLiteral -> NamedType("Int")
            is StringLiteral -> NamedType("String")
        }
    }

    private fun generalize(env: TypeEnvironment, type: Type): TypeScheme {
        val vars = (freeVars(type) - freeVarsEnv(env)).toList()
        return TypeScheme(vars, type)
    }

    private fun freeVarsEnv(env: TypeEnvironment): List<String> {
        return env.values.flatMap { freeVars(it) }
    }

    private fun freeVars(type: Type): Set<String> {
        return when (type) {
            is FunctionType -> freeVars(type.from) + freeVars(type.to)
            is NamedType -> type.subTypes.flatMap { freeVars(it) }.toSet()
            is TypeVariable -> setOf(type.name)
            is TypeScheme -> freeVars(type.type) - type.vars
        }
    }
}