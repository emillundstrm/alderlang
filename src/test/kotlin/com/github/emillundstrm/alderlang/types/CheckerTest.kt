package com.github.emillundstrm.alderlang.types

import com.github.emillundstrm.alderlang.ast.Id
import com.github.emillundstrm.alderlang.parser.AlderParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class CheckerTest {

    val intType = NamedType("Int")
    val stringType = NamedType("String")
    val listType = NamedType("List")

    val baseTypes: TypeEnvironment = mapOf(
        Pair(Id.named("plus"), TypeScheme(listOf(), FunctionType.binary(intType, intType, intType))),
        Pair(Id.named("parseInt"), TypeScheme(listOf(), FunctionType(stringType, intType))),
        Pair(Id.named("Pair"), TypeScheme(listOf("a", "b"), FunctionType.binary(TypeVariable("a"), TypeVariable("b"), NamedType("Pair"))))
    )

    @Test
    fun testChecker() {
        val expr = AlderParser.parseExpr("x -> x")
        println(WChecker().check(mapOf(), expr))
    }

    @Test
    fun testExampleFromWand() {
        val expr = AlderParser.parseExpr("x -> y -> z -> x z (y z)")
        println(WChecker().check(mapOf(), expr))
    }

    @Test
    fun testCheckerPlus() {
        val expr = AlderParser.parseExpr("x -> y -> x + (parseInt y)")
        println(WChecker().check(baseTypes, expr))
    }

    @Test
    fun testCheckerPattern() {
        val expr = AlderParser.parseExpr("(Pair x y) -> x + y")
        println(WChecker().check(baseTypes, expr))
    }

    @Test
    fun testCheckFailed() {
        // Trying to use x as both string and function
        val expr = AlderParser.parseExpr("x -> (parseInt x) + (x 1)")
        assertThrows<IllegalArgumentException> { WChecker().check(baseTypes, expr) }
    }

    @Test
    fun checkLiteral() {
        val expr = AlderParser.parseExpr("x -> 1")
        println(WChecker().check(baseTypes, expr))
    }

    @Test
    fun checkLet() {
        val expr = AlderParser.parseExpr("let id = x -> x in id (parseInt (\"1\"))")
        val (_, type) = WChecker().check(baseTypes, expr)
        assertEquals(NamedType("Int"), type)
    }

    @Test
    fun checkListType() {
        val expr = AlderParser.parseExpr("[1,2,3,4]")
        val listArchetype = NamedType("List", listOf(TypeVariable("a")))
        val consType = TypeScheme(listOf("a"), FunctionType.binary(TypeVariable("a"), listArchetype, listArchetype))
        val nilType = TypeScheme(listOf("a"), listArchetype)
        val (_, type) = WChecker().check(mapOf(
            Pair(Id.named("Cons"), consType),
            Pair(Id.named("Nil"), nilType)), expr)
        assertEquals(NamedType("List", listOf(NamedType("Int"))), type)
    }
}