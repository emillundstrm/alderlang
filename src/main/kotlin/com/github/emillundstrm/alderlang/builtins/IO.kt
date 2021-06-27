package com.github.emillundstrm.alderlang.builtins

import com.github.emillundstrm.alderlang.ast.*

val printLine = NativeFunction { _, arg ->
    IOAction { eval ->
        val str = eval(arg) as StringLiteral
        println(str.value)
        TypedValue("Unit")
    }
}

//:: IO a -> (f a -> IO b) -> IO b
val chainIO = NativeFunction { _, first ->
    NativeFunction { _, next ->
        IOAction { eval ->
            val a = (eval(first) as IOAction).perform(eval)
            val b = (eval(Apply(next, a)) as IOAction).perform(eval)
            b
        }
    }
}

val noop = IOAction { TypedValue("Unit") }