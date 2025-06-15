package com.mathGame.app.service

import com.mathGame.app.model.game.Question
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class QuestionService {
    
    private val operations = listOf("+", "-", "*", "/")
    private val random = Random.Default
    
    fun generateQuestions(count: Int): List<Question> {
        return (1..count).map { generateQuestion() }
    }
    
    private fun generateQuestion(): Question {
        // Randomly choose question type
        return when(random.nextInt(4)) {
            0 -> generateSimpleAddition()
            1 -> generateSimpleSubtraction()
            2 -> generateSimpleMultiplication()
            else -> generateSimpleDivision()
        }
    }
    
    private fun generateSimpleAddition(): Question {
        val a = random.nextInt(1, 50)
        val b = random.nextInt(1, 50)
        val expression = "$a + $b"
        val answer = a + b
        
        return Question(expression = expression, correctAnswer = answer)
    }
    
    private fun generateSimpleSubtraction(): Question {
        // Ensure positive result
        val a = random.nextInt(10, 100)
        val b = random.nextInt(1, a)
        val expression = "$a - $b"
        val answer = a - b
        
        return Question(expression = expression, correctAnswer = answer)
    }
    
    private fun generateSimpleMultiplication(): Question {
        val a = random.nextInt(1, 12)
        val b = random.nextInt(1, 12)
        val expression = "$a × $b"
        val answer = a * b
        
        return Question(expression = expression, correctAnswer = answer)
    }
    
    private fun generateSimpleDivision(): Question {
        // Generate division with no remainder
        val b = random.nextInt(1, 12)
        val answer = random.nextInt(1, 12)
        val a = b * answer
        val expression = "$a ÷ $b"
        
        return Question(expression = expression, correctAnswer = answer)
    }
    
    fun generateComplexQuestion(): Question {
        // For more advanced questions with multiple operations
        val numOperations = random.nextInt(1, 3)
        
        var result = random.nextInt(1, 20)
        var expression = result.toString()
        
        repeat(numOperations) {
            val operation = operations[random.nextInt(operations.size)]
            val operand = when (operation) {
                "+" -> random.nextInt(1, 20)
                "-" -> random.nextInt(1, result)
                "*" -> random.nextInt(1, 10)
                "/" -> {
                    // Find a number that divides evenly into result
                    val divisors = (1..result).filter { result % it == 0 }
                    if (divisors.isNotEmpty()) divisors[random.nextInt(divisors.size)] else 1
                }
                else -> 1
            }
            
            result = when (operation) {
                "+" -> result + operand
                "-" -> result - operand
                "*" -> result * operand
                "/" -> result / operand
                else -> result
            }
            
            expression = "($expression) $operation $operand"
        }
        
        return Question(expression = expression, correctAnswer = result)
    }
} 