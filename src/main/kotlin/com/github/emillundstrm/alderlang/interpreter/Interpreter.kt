package com.github.emillundstrm.alderlang.interpreter

import com.github.emillundstrm.alderlang.ast.Expression
import com.github.emillundstrm.alderlang.ast.IOAction
import com.github.emillundstrm.alderlang.builtins.BuiltIns
import com.github.emillundstrm.alderlang.parser.AlderParser
import com.github.emillundstrm.alderlang.reader.Reader
import com.github.emillundstrm.alderlang.ast.Module
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object Interpreter {

    fun eval(expr: String): Any {
        return eval(ExecutionContext(), AlderParser.parseExpr(expr))
    }

    fun run(file: File): Any {
        val reader = Reader()
        return run(reader.loadModule(FileInputStream(file)))
    }

    fun run(stream: InputStream): Any {
        val reader = Reader()
        return run(reader.loadModule(stream))
    }

    fun run(program: String): Any {
        val ast = AlderParser.parse(program)
        return run(ast)
    }

    fun run(program: Module): Any {
        val context = ExecutionContext()
        val reader = Reader()

        BuiltIns.defs.forEach { (name, nativeFunction) -> context.setValue(name, nativeFunction) }
        import(reader, context, program)

        return runIO(context, context.readValue("main") ?: throw IllegalArgumentException("Program is missing main function"))
    }

    private fun import(reader: Reader, context: ExecutionContext, moduleName: String) {
        val moduleSource = reader.loadModule(moduleName)
        val module = AlderParser.parse(moduleSource)
        import(reader, context, module)
    }

    private fun import(reader: Reader, context: ExecutionContext, module: Module) {
        module.imports.forEach { import(reader, context, it.moduleName) }
        module.letRec.types.flatMap { it.constructors }.forEach { context.setValue(it.name.name, it.value) }
        module.letRec.defs.forEach { context.setValue(it.lhs.name, it.rhs) }
    }

    private fun eval(context: ExecutionContext, expression: Expression): Any {
        val evaluator = LazyEvaluator(context)
        val value = evaluator.eval(expression)

        //println("Applies ${evaluator.applies}")
        //println("Matches ${evaluator.matches}")
        //println("Matches failed ${evaluator.matchFails}")
        return value
    }

    private fun runIO(context: ExecutionContext, expression: Expression): Any {
        val evaluator = LazyEvaluator(context)
        val value = evaluator.eval(expression)

        if (value is IOAction) {
            value.perform(evaluator::eval)
        }
        return value
        //throw TypeCastException("Value should be IOAction")
    }
}