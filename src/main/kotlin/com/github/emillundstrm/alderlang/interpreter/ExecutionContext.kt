package com.github.emillundstrm.alderlang.interpreter

import com.github.emillundstrm.alderlang.ast.Expression
import com.github.emillundstrm.alderlang.ast.Id
import kotlin.collections.HashMap

class ExecutionContext {

    private val parent: ExecutionContext?
    private val values: MutableMap<String, Expression> = HashMap()

    constructor(p: ExecutionContext) {
        this.parent = p
    }

    constructor() {
        this.parent = null
    }

    fun readValue(name: String): Expression? {
        return values[name] ?: parent?.readValue(name)
    }

    fun readValue(name: Id): Expression? = readValue(name.name)

    fun setValue(name: String, value: Expression) {
        values[name] = value
    }
}
