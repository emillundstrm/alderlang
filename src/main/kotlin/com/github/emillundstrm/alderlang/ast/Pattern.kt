package com.github.emillundstrm.alderlang.ast

sealed interface Pattern: Node

data class ConstructorPattern(val constructor: Id, val vars: List<Pattern>): Pattern