package com.github.emillundstrm.alderlang.interpreter

import com.github.emillundstrm.alderlang.ast.Function
import com.github.emillundstrm.alderlang.ast.*
import java.lang.RuntimeException
import kotlin.NoSuchElementException

data class LazyEvaluator(val context: ExecutionContext) {

    var applies = 0
    var matches = 0
    var matchFails = 0

    fun eval(e: Expression): Expression {
        return when (e) {
            is Apply -> e.cachedValue ?: apply(e)
            is Id -> eval(context.readValue(e) ?: throw NoSuchElementException("Missing value for ${e.name}"))
            is Case -> applyCase(e)
            else -> e
        }
    }

    private fun applyCase(e: Case): Expression {
        for (case in e.cases) {
            val result = matchAndReplace(case, e.test)
            if (result !is PatternMatchFailed) {
                matches++
                return eval(result)
            } else {
                matchFails++
            }
        }
        // TODO: This used to return PatternMatchFailed, but this is incorrect since it could cause another branch to be executed in an unrelated case expr
        throw PatternMatchFailedException("Pattern match failed (non-exhaustive case clause $e for value ${eval(e.test)})")
    }

    private fun apply(e: Apply): Expression {
        //println("Apply ${e.arg} to ${e.func}")
        applies++
        val result = when (val f = eval(e.func)) {
            is Function -> eval(matchAndReplace(f, e.arg))
            is NativeFunction -> f.apply(this::eval, e.arg)
            else -> throw TypeCastException("f must be a function but was $f")
        }
        e.cachedValue = result
        return result
    }

    private fun matchAndReplace(f: Function, expr: Expression): Expression {
        return when (val pattern = f.argument) {
            is Id -> f.body.replace(pattern, if (pattern.strict) eval(expr) else expr)
            is ConstructorPattern -> {
                // TODO: Implement lazy matching for product types. The constructor check is unnecessary if there is only one constructor.
                // TODO: How to lift out arguments though?
                val value = eval(expr)
                if (value is TypedValue && pattern.constructor == value.constructor) {
                    // Convert
                    // ((S p1 p2 p3) -> E) (S x y z)
                    // (x -> y -> z -> E) x y z
                    pattern.vars
                        .zip(value.args)
                        .fold(f.body) { acc, (p, v) -> matchAndReplace(Function(p, acc), v) }
                } else {
                    PatternMatchFailed
                }
            }
            else -> if (pattern == eval(expr)) f.body else PatternMatchFailed
        }
    }
}

private fun Expression.replace(name: Id, value: Expression): Expression {
    return when (this) {
        is Id -> if (this == name) value else this
        is TypedValue -> TypedValue(constructor, args.map { it.replace(name, value) })
        is Apply -> Apply(func.replace(name, value), arg.replace(name, value))
        is Function -> this.replaceWithAlphaConversion(name, value)
        is Case -> Case(
            test.replace(name, value),
            cases.map { it.replaceWithAlphaConversion(name, value) })
        else -> this
    }
}

private fun Function.replaceWithAlphaConversion(name: Id, value: Expression): Function {
    return when {
        name == argument -> this
        argument !is UniqueIdentifier && freeVars(value).contains(argument) -> {
            // Change (x -> y) to (z -> y.replace(x, y)), so variable names no longer clash.
            val newArg = UniqueIdentifier(false)
            Function(newArg, body.replace(argument as Id, newArg).replace(name, value))
        }
        else -> Function(argument, body.replace(name, value))
    }
}

private fun freeVars(e: Node): Set<Id> {
    return when (e) {
        is Id -> setOf(e)
        is Function -> freeVars(e.body) - freeVars(e.argument)
        is Apply -> freeVars(e.arg) + freeVars(e.func)
        else -> setOf()
    }
}

class PatternMatchFailedException(msg: String) : RuntimeException(msg)

