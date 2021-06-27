package com.github.emillundstrm.alderlang.reader

import java.io.BufferedInputStream
import java.io.InputStream

class Reader {
    fun loadModule(stream: InputStream): String {
        return stream.use { stream -> BufferedInputStream(stream).bufferedReader().readText() }
    }

    fun loadModule(name: String): String {
        val stream = javaClass.classLoader.getResourceAsStream("$name.al") ?: throw IllegalArgumentException("Cannot find module ${name}.al")
        return loadModule(stream)
    }
}