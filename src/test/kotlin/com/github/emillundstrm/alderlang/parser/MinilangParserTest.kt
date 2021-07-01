package com.github.emillundstrm.alderlang.parser

import org.junit.jupiter.api.Assertions.assertEquals
import com.github.emillundstrm.alderlang.ast.*
import kotlin.test.Test

internal class MinilangParserTest {

    @Test
    fun testParseApply() {
        val ast = AlderParser.parseExpr("head list")
        assertEquals(Apply(Id.named("head"), Id.named("list")), ast)
    }

    @Test
    fun testParseApplyChain() {
        val ast = AlderParser.parseExpr("get 0 list")
        assertEquals(
            Apply(
                Apply(Id.named("get"), IntegerLiteral(0)),
                Id.named("list")
            ), ast)
    }

    @Test
    fun testParseApplyParens() {
        val ast = AlderParser.parseExpr("head (list 1 2)")
        assertEquals(
            Apply(
                Id.named("head"),
                Apply(
                    Apply(Id.named("list"), IntegerLiteral(1)),
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
                Id.named("a"),
                Function(Id.named("b"), Apply(Id.named("a"), Id.named("b")))
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
            TypeDef(TypeName(Id.named("Bool")),
                listOf(
                    DataConstructor(Id.named("True"), TypedValue("True")),
                    DataConstructor(Id.named("False"), TypedValue("False"))
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
