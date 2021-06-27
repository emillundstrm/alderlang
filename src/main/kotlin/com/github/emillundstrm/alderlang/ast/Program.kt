package com.github.emillundstrm.alderlang.ast

data class Module(val imports: List<Import>, val letRec: LetRec) : Node

