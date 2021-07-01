package com.github.emillundstrm.alderlang.builtins

import com.github.emillundstrm.alderlang.ast.*

val readLn = IOAction { StringLiteral(readLine()!!) }

val print = NativeFunction { _, arg ->
    IOAction { eval ->
        val str = eval(arg) as StringLiteral
        print(str.value)
        TypedValue("Unit")
    }
}

//:: IO a -> (a -> IO b) -> IO b
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