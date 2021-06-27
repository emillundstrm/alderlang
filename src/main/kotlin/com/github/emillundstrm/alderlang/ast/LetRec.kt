package com.github.emillundstrm.alderlang.ast

data class LetRec(val types: List<TypeDef>, val defs: List<Assign>) : Node