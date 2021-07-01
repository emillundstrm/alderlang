package com.github.emillundstrm.alderlang.interpreter

import com.github.emillundstrm.alderlang.ast.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.FileNotFoundException
import kotlin.test.Test

internal class InterpreterTest {

    @Test
    fun testInterpreter() {
        assertEquals(IntegerLiteral(1), Interpreter.eval("(a -> 1) 2"))
        assertEquals(IntegerLiteral(1), Interpreter.eval("(a -> a) 1"))
        assertEquals(IntegerLiteral(1), Interpreter.eval("(a -> b -> a) 1 (foo bar baz)"))
    }

    @Test
    fun testGlobalFuncs() {
        assertEquals(IntegerLiteral(3), Interpreter.run("""
            let f a b = a + b
            let main = f 1 2""".trimIndent()))
    }

    private fun runFile(filename: String): Any {
        return Interpreter.run(this.javaClass.classLoader.getResourceAsStream(filename) ?: throw FileNotFoundException(filename))
    }

    @Test
    fun testChainLet() {
        assertEquals(
            StringLiteral("test"),
            Interpreter.eval("""
            let a = "test" in
            let b = a in
            let c = b in
            c""".trimIndent()))
    }

    @Test
    fun testFuncWithInternalLet() {
        assertEquals(
            IntegerLiteral(1),
            Interpreter.eval("""
            let f a = 
                let x = a in x
                in f 1
            """.trimIndent()))
    }


    @Test
    fun testFlip() {
        assertEquals(
            IntegerLiteral(1),
            Interpreter.run("""
            let flip f x y = f y x
            let f a b = b
            let main = flip f 1 2""".trimIndent()))
    }

    @Test
    fun testTwoArgs() {
        assertEquals(
            IntegerLiteral(1),
            Interpreter.eval("let f x y = x in f 1 2"))
    }

    @Test
    fun testTwoArgs2() {
        assertEquals(
            IntegerLiteral(1),
            Interpreter.eval("(x -> y -> x) 1 2"))
    }

    @Test
    fun testInvalidFunction() {
        assertThrows(NoSuchElementException::class.java) {
            val value = Interpreter.eval("""
            let f a = x in
            let x = 1 in
            f 1""".trimIndent())
            println(value)
        }
    }

    @Test
    fun testYComb() {
        val value = Interpreter.eval("let fix = f -> (x -> f (x x)) (x -> f (x x)) in fix (x -> 1)")
        assertEquals(IntegerLiteral(1), value)
    }

    @Test
    fun testLexicalClosure() {
        assertEquals(
            IntegerLiteral(2),
            Interpreter.run("""
            let f x = 2
            let g x = f x
            let main = let f x = 1 in
                g x""".trimIndent()))
    }

    @Test
    fun testType() {
        assertEquals(
            TypedValue("True"),
            Interpreter.run("""
            type Bool = True | False
            let main = True
            """.trimIndent()))
    }

    @Test
    fun testCons() {
        assertEquals(
            TypedValue("Cons", IntegerLiteral(1), Id.named("Nil")), // Note that Nil is not evaluated to TypedValue("Nil") yet
            Interpreter.run("""
            type List a = Cons a (List a) | Nil
            let main = Cons 1 Nil
            """.trimIndent()))
    }

    @Test
    fun testConsLiteral() {
        assertEquals(
            TypedValue("Cons", IntegerLiteral(1), Id.named("Nil")),
            Interpreter.run("""
            type List a = Cons a (List a) | Nil
            let main = [1]
            """.trimIndent()))
    }

    @Test
    fun testPatternMatch() {
        assertEquals(
            IntegerLiteral(3),
            Interpreter.run("""
            type List a = Cons a (List a) | Nil
            let fst (Cons a b) = a
            let main = fst [3,2,1]
            """.trimIndent()))
    }

    @Test
    fun testComplexPatternMatch() {
        assertEquals(
            IntegerLiteral(3),
            Interpreter.run("""
            type List a = Cons a (List a) | Nil

            let last list = case list of
                | (Cons x Nil) -> x
                | (Cons x y) -> last y

            let main = last [1,2,3]
            """.trimIndent()))
    }

    @Test
    fun testStdLib() {
        assertEquals(
            IntegerLiteral(1),
            Interpreter.run("""
            import stdlib
            let main = head (map head [[1],[2],[3]])
            """.trimIndent()))
    }

    @Test
    fun testStdLibFold() {
        assertEquals(
            IntegerLiteral(6),
            Interpreter.run("""
            import stdlib
            let main = fold plus 0 [1,2,3]
            """.trimIndent()))
    }

    @Test
    fun testLazyList() {
        assertEquals(
            IntegerLiteral(1),
            Interpreter.run("""
            import stdlib
            let main = head [1, bottom]
            """.trimIndent()))
    }

    @Test
    fun testRange() {
        assertEquals(
            TypedValue(Id.named("True")),
            Interpreter.run("""
            import stdlib
            let main = eq [5,6,7,8,9] (range 5 10)
            """.trimIndent()))
    }

    @Test
    fun testReverse() {
        assertEquals(
            TypedValue(Id.named("True")),
            Interpreter.run("""
            import stdlib
            let main = eq [1,2,3,4,5] (reverse [5,4,3,2,1])
            """.trimIndent()))
    }

    @Test
    fun testConcat() {
        assertEquals(
            TypedValue(Id.named("True")),
            Interpreter.run("""
            import stdlib
            let main = eq [1,2,3,4] (concat [1,2] [3,4])
            """.trimIndent()))
    }

    @Test
    fun testPartition() {
        assertEquals(
            TypedValue(Id.named("True")),
            Interpreter.run("""
            import stdlib
            let isEven x = eq 0 (x % 2)
            let main = eq (partition isEven [1,2,3,4]) (Pair [2,4] [1,3])
            """.trimIndent()))
    }

    @Test
    fun testFoldr() {
        assertEquals(
            IntegerLiteral(10),
            Interpreter.run("""
            import stdlib
            let main = foldStrict plus 0 [1,2,3,4]
            """.trimIndent()))
    }

    @Test
    fun testFizzBuzz() {
        runFile("fizzbuzz.al")
    }
}