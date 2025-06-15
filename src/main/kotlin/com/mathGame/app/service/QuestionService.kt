package com.mathGame.app.service

import com.mathGame.app.constants.QuestionConstants
import com.mathGame.app.model.game.Question as GameQuestion
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class QuestionService {
    
    private val operations = listOf(
        QuestionConstants.ADDITION,
        QuestionConstants.SUBTRACTION,
        QuestionConstants.MULTIPLICATION,
        QuestionConstants.DIVISION
    )
    private val random = Random.Default
    
    fun generateQuestions(count: Int): List<GameQuestion> {
        return (1..count).map { generateQuestion() }
    }
    
    private fun generateQuestion(): GameQuestion {
        // Randomly choose question type
        return when(random.nextInt(4)) {
            0 -> generateSimpleAddition()
            1 -> generateSimpleSubtraction()
            2 -> generateSimpleMultiplication()
            else -> generateSimpleDivision()
        }
    }
    
    private fun generateSimpleAddition(): GameQuestion {
        val a = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, QuestionConstants.MAX_ADDITION_NUMBER)
        val b = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, QuestionConstants.MAX_ADDITION_NUMBER)
        val expression = "$a ${QuestionConstants.ADDITION} $b"
        val answer = a + b
        
        return GameQuestion(expression = expression, correctAnswer = answer)
    }
    
    private fun generateSimpleSubtraction(): GameQuestion {
        // Ensure positive result
        val a = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, QuestionConstants.MAX_SUBTRACTION_NUMBER)
        val b = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, a)
        val expression = "$a ${QuestionConstants.SUBTRACTION} $b"
        val answer = a - b
        
        return GameQuestion(expression = expression, correctAnswer = answer)
    }
    
    private fun generateSimpleMultiplication(): GameQuestion {
        val a = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, QuestionConstants.MAX_MULTIPLICATION_NUMBER)
        val b = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, QuestionConstants.MAX_MULTIPLICATION_NUMBER)
        val expression = "$a ${QuestionConstants.MULTIPLICATION} $b"
        val answer = a * b
        
        return GameQuestion(expression = expression, correctAnswer = answer)
    }
    
    private fun generateSimpleDivision(): GameQuestion {
        // Generate division with no remainder
        val b = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, QuestionConstants.MAX_DIVISION_NUMBER)
        val answer = random.nextInt(QuestionConstants.MIN_SIMPLE_NUMBER, QuestionConstants.MAX_DIVISION_NUMBER)
        val a = b * answer
        val expression = "$a ${QuestionConstants.DIVISION} $b"
        
        return GameQuestion(expression = expression, correctAnswer = answer)
    }
    
    fun generateComplexQuestion(): GameQuestion {
        // For more advanced questions with multiple operations
        val numOperations = random.nextInt(1, QuestionConstants.MAX_COMPLEX_OPERATIONS)
        
        var result = random.nextInt(QuestionConstants.MIN_COMPLEX_NUMBER, QuestionConstants.MAX_COMPLEX_NUMBER)
        var expression = result.toString()
        
        repeat(numOperations) {
            val operation = operations[random.nextInt(operations.size)]
            val operand = when (operation) {
                QuestionConstants.ADDITION -> random.nextInt(QuestionConstants.MIN_COMPLEX_NUMBER, QuestionConstants.MAX_COMPLEX_NUMBER)
                QuestionConstants.SUBTRACTION -> random.nextInt(QuestionConstants.MIN_COMPLEX_NUMBER, result)
                QuestionConstants.MULTIPLICATION -> random.nextInt(QuestionConstants.MIN_COMPLEX_NUMBER, 10)
                QuestionConstants.DIVISION -> {
                    // Find a number that divides evenly into result
                    val divisors = (1..result).filter { result % it == 0 }
                    if (divisors.isNotEmpty()) divisors[random.nextInt(divisors.size)] else 1
                }
                else -> 1
            }
            
            result = when (operation) {
                QuestionConstants.ADDITION -> result + operand
                QuestionConstants.SUBTRACTION -> result - operand
                QuestionConstants.MULTIPLICATION -> result * operand
                QuestionConstants.DIVISION -> result / operand
                else -> result
            }
            
            expression = "($expression) $operation $operand"
        }
        
        return GameQuestion(expression = expression, correctAnswer = result)
    }
} 