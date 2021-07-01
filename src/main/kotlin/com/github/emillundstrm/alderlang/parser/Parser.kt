package com.github.emillundstrm.alderlang.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.emillundstrm.alderlang.ast.*

val alderLangGrammar = object : Grammar<Node>() {

    val LPAREN by literalToken("(")
    val RPAREN by literalToken(")")
    val EQUALS by literalToken("=")
    val ARROW by literalToken("->")
    val TIMES by literalToken("*")
    val DIVIDE by literalToken("/")
    val MOD by literalToken("%")
    val PLUS by literalToken("+")
    val MINUS by literalToken("-")
    val APP_RIGHT by literalToken("|>")
    val APP_LEFT by literalToken("<|")
    val SEQ by literalToken("!!")
    val STRICT by literalToken("!")
    val PIPE by literalToken("|")
    val LSBRACK by literalToken("[")
    val RSBRACK by literalToken("]")
    val COMMA by literalToken(",")

    val K_IMPORT by regexToken("import\\b")
    val K_TYPE by regexToken("type\\b")
    val K_LET by regexToken("let\\b")
    val K_CASE by regexToken("case\\b")
    val K_OF by regexToken("of\\b")
    val K_IN by regexToken("in\\b")

    val DECIMAL by regexToken("-?\\d+[.]\\d+")
    val INTEGER by regexToken("-?\\d+")

    val COMMENT by regexToken("[#].*\\v", ignore = true)
    val WHITESPACE by regexToken("\\s+", ignore = true)

    val TYPE by regexToken("[A-Z][A-Za-z0-9_]*")
    val VARIABLE by regexToken("[a-z][A-Za-z0-9_]*")
    val STRING_LITERAL by regexToken("\"(\\\\.|[^\"\\\\])*?\"")

    val import by -K_IMPORT * VARIABLE use { Import(text) }

    val typeId by TYPE use { Id.named(text) }
    val identifier by optional(STRICT) * VARIABLE use { Id.named(t2.text, t1 != null) }
    val decimalLiteral by DECIMAL use { DecimalLiteral(text.toDouble()) }
    val integerLiteral by INTEGER use { IntegerLiteral(text.toInt()) }
    val stringLiteral by STRING_LITERAL use {
        StringLiteral(text.removeSurrounding("\"", "\"")
            .replace(Regex("\\\\n")) { "\n"}
            .replace(Regex("\\\\(.)")) { it.groupValues[1] })
    }

    val arrayShortHandExpr by -LSBRACK * separatedTerms(parser(this::expression), COMMA, acceptZero = true) * -RSBRACK use {
        val emptyList: Expression = Id.named("Nil")
        foldRight(emptyList) { item, acc ->
            Apply(
                Apply(Id.named("Cons"), item),
                acc
            )
        }
    }

    val noArgConsPattern by typeId use { ConstructorPattern(this, listOf()) }
    val consPattern: Parser<Pattern> by typeId * zeroOrMore(parser(this::pattern)) use { ConstructorPattern(t1, t2) }
    val pattern: Parser<Pattern> by decimalLiteral or integerLiteral or stringLiteral or identifier or noArgConsPattern or (-LPAREN * consPattern * -RPAREN)

    val simpleExpr by decimalLiteral or integerLiteral or stringLiteral or
            identifier or
            typeId or
            arrayShortHandExpr or
            -LPAREN * parser(this::expression) * -RPAREN

    val applyChain by 2.timesOrMore(simpleExpr) map { it.reduce { a, b -> Apply(a, b) } }
    val divMultChain by leftAssociative(applyChain or simpleExpr, TIMES or DIVIDE or MOD, ::binaryOp)
    val plusMinusChain by leftAssociative(divMultChain, PLUS or MINUS, ::binaryOp)
    val seqChain by leftAssociative(plusMinusChain, SEQ, ::binaryOp)
    val appChain by leftAssociative(seqChain, APP_LEFT or APP_RIGHT) { a, op, b ->
        if (op.text == "<|") Apply(a, b) else Apply(b, a)
    }

    val assign by -K_LET * identifier * zeroOrMore(pattern) * -EQUALS * parser(this::expression) use {
        Assign(t1, createFunction(t2, t3))
    }

    val assignExpr by -K_LET * pattern * zeroOrMore(pattern) * -EQUALS * parser(this::expression) * -K_IN * parser(this::expression) use {
        Apply(Function(t1, t4), createFunction(t2, t3))
    }

    val function by pattern * -ARROW * parser(this::expression) use { Function(t1, t2) }
    val caseExpression by -K_CASE * parser(this::expression) * -K_OF * -optional(PIPE) * separatedTerms(function, PIPE) use {
        Case(t1, t2)
    }
    val expression: Parser<Expression> = function or appChain or simpleExpr or assignExpr or caseExpression

    val typeName by typeId * zeroOrMore(identifier) use { TypeName(t1, t2) }
    val typeArg: Parser<TypePattern> by identifier or -LPAREN * typeName * -RPAREN
    val constructor by typeId * zeroOrMore(typeArg) use {
        val args = t2.map { Id.unique(false) }
        val instance: Expression = TypedValue(t1, args)
        DataConstructor(t1, args.foldRight(instance) { arg, acc -> Function(arg, acc) })
    }
    val typeDef by -K_TYPE * typeName * -EQUALS * separatedTerms(constructor, PIPE) use { TypeDef(t1, t2) }

    val letRec by oneOrMore(typeDef or assign) map { list ->
        list.partition { it is Assign }.let { (assigns, types) -> LetRec(types as List<TypeDef>, assigns as List<Assign>) }
    }

    val module by zeroOrMore(import) * letRec use { Module(t1, t2) }

    override val rootParser by expression or module
}

private fun createFunction(args: List<Pattern>, body: Expression) = args.foldRight(body) { pattern, acc ->
    Function(pattern, acc)
}

private fun binaryOp(a: Expression, op: TokenMatch, b: Expression): Expression =
    Apply(Apply(translateOperator(op), a), b)

private fun translateOperator(op: TokenMatch): Expression {
    return when (op.text) {
        "+" -> Id.named("plus")
        "-" -> Id.named("minus")
        "*" -> Id.named("times")
        "/" -> Id.named("divide")
        "%" -> Id.named("mod")
        "!!" -> Id.named("seq")
        else -> throw IllegalArgumentException("Unknown operator " + op.text)
    }
}

object AlderParser {
    fun parse(program: String): Module = alderLangGrammar.parseToEnd(program) as Module
    fun parseExpr(expr: String): Expression = alderLangGrammar.parseToEnd(expr) as Expression
}
