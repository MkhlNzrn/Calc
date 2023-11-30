package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.ln
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {

    private lateinit var mathOperationTextView: TextView
    private lateinit var resultTextView: TextView
    private lateinit var xTextView: TextView
    private lateinit var yTextView: TextView
    private lateinit var zTextView: TextView
    private var inputExpression: String = ""
    private var x: Float = 0.0f
    private var y: Float = 0.0f
    private var z: Float = 0.0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mathOperationTextView = findViewById(R.id.math_operation)
        resultTextView = findViewById(R.id.result_text)

        xTextView = findViewById(R.id.x)
        xTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                x = charSequence.toString().toFloat()
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        yTextView = findViewById(R.id.y)
        yTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                y = charSequence.toString().toFloat()
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        zTextView = findViewById(R.id.z)
        zTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                z = charSequence.toString().toFloat()
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        val numberButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
            R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
            R.id.btn_8, R.id.btn_9
        )

        for (buttonId in numberButtons) {
            findViewById<TextView>(buttonId).setOnClickListener {
                onNumberButtonClick(it)
            }
        }

        val actionButtons = listOf(
            R.id.btn_plus, R.id.btn_minus, R.id.btn_multiply, R.id.btn_slash,
            R.id.btn_start_bracket, R.id.btn_end_bracket, R.id.btn_dot,
            R.id.btn_back, R.id.btn_equals, R.id.btn_clear,
            R.id.btn_x, R.id.btn_y, R.id.btn_z, R.id.btn_ln, R.id.btn_sqrt
        )

        for (buttonId in actionButtons) {
            findViewById<TextView>(buttonId).setOnClickListener {
                onActionButtonClick(it)
            }
        }
    }

    private fun onNumberButtonClick(view: View) {
        val number = (view as TextView).text
        inputExpression += number
        updateMathOperationText()
    }

    private fun onActionButtonClick(view: View) {
        val action = (view as TextView).text.toString()

        when (action) {
            "Back" -> {
                if (inputExpression.isNotEmpty()) {
                    inputExpression = inputExpression.substring(0, inputExpression.length - 1)
                    updateMathOperationText()
                }
            }
            "AC" -> {
                inputExpression = ""
                updateMathOperationText()
                updateResultText()
            }
            "=" -> {
                try {
                    val variables = mapOf("x" to x, "y" to y, "z" to z)
                    val result = evaluateExpression(inputExpression, variables)
                    updateResultText(result.toString())
                } catch (e: Exception) {
                    updateResultText("Error")
                }
            }
            else -> {
                inputExpression += action
                updateMathOperationText()
            }
        }
    }

    private fun evaluateExpression(expression: String, variables: Map<String, Float>): Float {
        val tokens = tokenizeExpression(expression)
        val postfixTokens = infixToPostfix(tokens)
        val stack = mutableListOf<Float>()

        for (token in postfixTokens) {
            when {
                token.isNumber() -> stack.push(token.toFloat())
                token.isOperator() -> {
                    val operand2 = stack.pop()
                    val operand1 = stack.pop()
                    stack.push(evaluateMathOperation(token[0], operand1, operand2))
                }
                token.isFunction() -> {
                    if (stack.isNotEmpty()) {
                        val operand = stack.pop()
                        stack.push(evaluateFunction(token, operand))
                    }
                }
                token.isVariable() -> stack.push(variables[token] ?: 0.0f)
            }
        }

        return stack.pop()
    }

    private fun evaluateMathOperation(operator: Char, operand1: Float, operand2: Float): Float {
        return when (operator) {
            '+' -> operand1 + operand2
            '-' -> operand1 - operand2
            '*' -> operand1 * operand2
            '/' -> {
                if (operand2 != 0.0f) {
                    operand1 / operand2
                } else {
                    throw ArithmeticException("Division by zero")
                }
            }
            else -> throw IllegalArgumentException("Invalid operator")
        }
    }


    private fun evaluateFunction(function: String, operand: Float): Float {
        return when (function) {
            "sqrt" -> {
                if (operand >= 0) {
                    sqrt(operand.toDouble()).toFloat()
                } else {
                    throw ArithmeticException("Cannot take the square root of a negative number")
                }
            }
            "ln" -> {
                if (operand > 0) {
                    ln(operand.toDouble()).toFloat()
                } else {
                    throw ArithmeticException("Invalid input for natural logarithm")
                }
            }
            else -> throw IllegalArgumentException("Invalid function")
        }
    }

    private fun tokenizeExpression(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentToken = ""

        for (char in expression) {
            when {
                char.isDigit() || char == '.' -> currentToken += char
                char.isLetter() -> currentToken += char
                char.toString().isOperator() || char.isParenthesis() -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken)
                        currentToken = ""
                    }
                    tokens.add(char.toString())
                }
                char.isWhitespace() -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken)
                        currentToken = ""
                    }
                }
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken)
        }

        return tokens
    }

    private fun infixToPostfix(infixTokens: List<String>): List<String> {
        val outputQueue = mutableListOf<String>()
        val operatorStack = mutableListOf<String>()

        for (token in infixTokens) {
            when {
                token.isNumber() || token.isVariable() -> outputQueue.add(token)
                token.isFunction() -> operatorStack.push(token)
                token.isOperator() -> {
                    while (operatorStack.isNotEmpty() && operatorStack.peek().isOperator() &&
                        operatorStack.peek().getPrecedence() >= token.getPrecedence()
                    ) {
                        outputQueue.add(operatorStack.pop())
                    }
                    operatorStack.push(token)
                }
                token == "(" -> operatorStack.push(token)
                token == ")" -> {
                    while (operatorStack.isNotEmpty() && operatorStack.peek() != "(") {
                        outputQueue.add(operatorStack.pop())
                    }
                    operatorStack.pop()
                }
            }
        }

        while (operatorStack.isNotEmpty()) {
            outputQueue.add(operatorStack.pop())
        }

        return outputQueue
    }

    private fun String.getPrecedence(): Int {
        return when (this) {
            "+", "-" -> 1
            "*", "/" -> 2
            else -> 0
        }
    }

    private fun Char.isParenthesis(): Boolean {
        return this == '(' || this == ')'
    }

    private fun <T> MutableList<T>.push(item: T) = add(item)
    private fun <T> MutableList<T>.pop(): T = removeAt(size - 1)
    private fun <T> MutableList<T>.peek(): T = this[size - 1]


    private fun String.isNumber(): Boolean {
        return this.toFloatOrNull() != null
    }

    private fun String.isVariable(): Boolean {
        return this.matches(Regex("[a-zA-Z]+"))
    }

    private fun String.isOperator(): Boolean {
        return this.length == 1 && "+-*/".contains(this[0])
    }

    private fun String.isFunction(): Boolean {
        return this.matches(Regex("(sqrt|ln)"))
    }


    private fun updateMathOperationText() {
        mathOperationTextView.text = inputExpression
    }

    private fun updateResultText(result: String = "") {
        resultTextView.text = result
    }
}
