package com.github.emillundstrm.alderlang.ast

data class IntegerLiteral(val value: Int) : Expression, Pattern

data class StringLiteral(val value: String) : Expression, Pattern

data class DecimalLiteral(val value: Double) : Expression, Pattern