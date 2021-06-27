package com.github.emillundstrm.alderlang.parser

import org.junit.jupiter.api.Assertions.assertEquals
import com.github.emillundstrm.alderlang.ast.*
import kotlin.test.Test

internal class MinilangParserTest {

    @Test
    fun testParseApply() {
        val ast = AlderParser.parseExpr("head list")
        assertEquals(Apply(Identifier("head"), Identifier("list")), ast)
    }

    @Test
    fun testParseApplyChain() {
        val ast = AlderParser.parseExpr("get 0 list")
        assertEquals(
            Apply(
                Apply(Identifier("get"), IntegerLiteral(0)),
                Identifier("list")
            ), ast)
    }

    @Test
    fun testParseApplyParens() {
        val ast = AlderParser.parseExpr("head (list 1 2)")
        assertEquals(
            Apply(
                Identifier("head"),
                Apply(
                    Apply(Identifier("list"), IntegerLiteral(1)),
                    IntegerLiteral(2)
                )
            ), ast)
    }

    @Test
    fun testParseStringLiteral() {
        assertEquals(StringLiteral(""), AlderParser.parseExpr("\"\""))
        assertEquals(StringLiteral("foo"), AlderParser.parseExpr("\"foo\""))
        assertEquals(StringLiteral("\""), AlderParser.parseExpr("\"\\\"\""))
        assertEquals(StringLiteral("\\"), AlderParser.parseExpr("\"\\\\\""))
    }

    @Test
    fun testParseLambdaFunction() {
        assertEquals(
            Function(
                Identifier("a"),
                Function(Identifier("b"), Apply(Identifier("a"), Identifier("b")))
            ),
            AlderParser.parseExpr("a -> b -> a b")
        )
    }

    @Test
    fun testParseNamedFunction() {
        AlderParser.parseExpr("""
            let f a b = plus a b in
            f 1 2""".trimIndent()
        )
    }

    @Test
    fun testParseBoolType() {
        val program = AlderParser.parse(
            """
            type Bool = True | False
            """.trimIndent()
        )

        assertEquals(program.letRec.types[0],
            TypeDef(TypeName(Identifier("Bool")),
                listOf(
                    DataConstructor(Identifier("True"), TypedValue("True")),
                    DataConstructor(Identifier("False"), TypedValue("False"))
                )))
    }


    @Test
    fun testParseListType() {
        AlderParser.parse(
            """
            type List a = Cons a (List a) | Empty
            """.trimIndent()
        )
    }

    @Test
    fun testParseCase() {
        AlderParser.parseExpr(
            """
            case x of 
                | 1 -> foo 
                | 2 -> bar 
                | 3 -> (case y of 1 -> baz | x -> x)""".trimIndent()
        )
    }

    @Test
    fun testParseLetCase() {
        AlderParser.parseExpr(
            """
            let z = (case y of 1 -> baz | x -> x) in
            z
            """.trimIndent()
        )
    }
}
