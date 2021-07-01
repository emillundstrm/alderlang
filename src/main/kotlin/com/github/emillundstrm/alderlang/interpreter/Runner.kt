package com.github.emillundstrm.alderlang.interpreter

import java.io.File

fun main(arg: Array<String>) {
    Interpreter.run(File(arg[0]))
}