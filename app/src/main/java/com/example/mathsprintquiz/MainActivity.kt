package com.example.mathsprintquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class PrimeBand(val label: String, val start: Int, val end: Int, val primes: List<Int>)

val PRIME_BANDS = listOf(
    PrimeBand("1-10", 1, 10, listOf(2, 3, 5, 7)),
    PrimeBand("10-20", 10, 20, listOf(11, 13, 17, 19)),
    PrimeBand("20-30", 20, 30, listOf(23, 29)),
    PrimeBand("30-40", 30, 40, listOf(31, 37)),
    PrimeBand("40-50", 40, 50, listOf(41, 43, 47)),
    PrimeBand("50-60", 50, 60, listOf(53, 59)),
    PrimeBand("60-70", 60, 70, listOf(61, 67)),
    PrimeBand("70-80", 70, 80, listOf(71, 73, 79)),
    PrimeBand("80-90", 80, 90, listOf(83, 89)),
    PrimeBand("90-100", 90, 100, listOf(97)),
    PrimeBand("100-110", 100, 110, listOf(101, 103, 107, 109)),
    PrimeBand("110-125", 110, 125, listOf(113))
)

data class QuizQuestion(
    val loopNumber: Int,
    val stepInLoop: Int,
    val title: String,
    val expression: String,
    val correctAnswer: String,
    val timeLimitSeconds: Int,
    val isPrimeStage: Boolean = false
)

data class QuizAttempt(
    val question: QuizQuestion,
    val userAnswer: String,
    val isCorrect: Boolean,
    val timedOut: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0F172A)) {
                    ArithmeticQuizApp()
                }
            }
        }
    }
}

fun gcd(a: Long, b: Long): Long = if (b == 0L) kotlin.math.abs(a) else gcd(b, a % b)
fun lcm(a: Long, b: Long): Long = if (a == 0L || b == 0L) 0L else kotlin.math.abs(a / gcd(a, b) * b)

fun generateAllTwelveLoops(): List<QuizQuestion> {
    val allStages = mutableListOf<QuizQuestion>()
    PRIME_BANDS.forEachIndexed { index, band ->
        val loopNum = index + 1
        val addA = Random.nextLong(2, 10000)
        val addB = Random.nextLong(2, 10000)

        val subRawA = Random.nextLong(2, 10000)
        val subRawB = Random.nextLong(2, 10000)
        val subA = maxOf(subRawA, subRawB)
        val subB = minOf(subRawA, subRawB)

        val mulA = Random.nextLong(2, 10000)
        val mulB = Random.nextLong(2, 26)

        val divB = Random.nextLong(2, 26)
        val maxQuotient = 9999L / divB
        val quotient = Random.nextLong(1, maxQuotient + 1)
        val divA = quotient * divB

        val lcmA = Random.nextLong(2, 250)
        val lcmB = Random.nextLong(2, 250)
        val lcmC = Random.nextLong(2, 250)
        val lcmAns = lcm(lcm(lcmA, lcmB), lcmC)

        val hcfA = Random.nextLong(2, 250)
        val hcfB = Random.nextLong(2, 250)
        val hcfC = Random.nextLong(2, 250)
        val hcfAns = gcd(gcd(hcfA, hcfB), hcfC)

        val primeCsv = band.primes.joinToString(", ")

        allStages.add(QuizQuestion(loopNum, 1, "Addition (2..9999)", "$addA + $addB", (addA + addB).toString(), 5))
        allStages.add(QuizQuestion(loopNum, 2, "Subtraction (2..9999)", "$subA - $subB", (subA - subB).toString(), 5))
        allStages.add(QuizQuestion(loopNum, 3, "Multiplication (2..9999 × 2..25)", "$mulA × $mulB", (mulA * mulB).toString(), 15))
        allStages.add(QuizQuestion(loopNum, 4, "Division (2..9999 ÷ 2..25)", "$divA ÷ $divB", (divA / divB).toString(), 15))
        allStages.add(QuizQuestion(loopNum, 5, "LCM of 3 Numbers (< 250)", "LCM($lcmA, $lcmB, $lcmC)", lcmAns.toString(), 25))
        allStages.add(QuizQuestion(loopNum, 6, "HCF of 3 Numbers (< 250)", "HCF($hcfA, $hcfB, $hcfC)", hcfAns.toString(), 25))
        allStages.add(
            QuizQuestion(
                loopNumber = loopNum,
                stepInLoop = 7,
                title = "Prime Numbers (${band.label})",
                expression = "Primes from ${band.start} to ${band.end}",
                correctAnswer = primeCsv,
                timeLimitSeconds = 30,
                isPrimeStage = true
            )
        )
    }
    return allStages
}

@Composable
fun ArithmeticQuizApp() {
    var questions by remember { mutableStateOf(generateAllTwelveLoops()) }
    var currentIndex by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(listOf<QuizAttempt>()) }
    var quizState by remember { mutableStateOf("HOME") }
    var remainingMs by remember { mutableStateOf(5000L) }

    val currentQuestion = questions.getOrNull(currentIndex)

    fun submitCurrent(timedOut: Boolean) {
        val q = currentQuestion ?: return
        val isCorrect = if (q.isPrimeStage) {
            true
        } else {
            !timedOut && userInput.trim() == q.correctAnswer
        }
        attempts = attempts + QuizAttempt(
            question = q,
            userAnswer = if (q.isPrimeStage) q.correctAnswer else if (timedOut) "Time's Up" else userInput,
            isCorrect = isCorrect,
            timedOut = timedOut
        )
        userInput = ""
        if (currentIndex + 1 < questions.size) {
            currentIndex += 1
            remainingMs = questions[currentIndex].timeLimitSeconds * 1000L
        } else {
            quizState = "RESULT"
        }
    }

    LaunchedEffect(quizState, currentIndex) {
        if (quizState == "PLAY" && currentQuestion != null) {
            remainingMs = currentQuestion.timeLimitSeconds * 1000L
            while (remainingMs > 0 && quizState == "PLAY") {
                delay(100L)
                remainingMs -= 100L
            }
            if (remainingMs <= 0 && quizState == "PLAY") {
                submitCurrent(timedOut = true)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (quizState) {
            "HOME" -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("12-Loop Arithmetic & Prime Quiz", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Loops 6 Arithmetic Questions + 30s Prime Band across all 12 bands (1–10 to 110–125)", color = Color(0xFF94A3B8))
                }
                Button(
                    onClick = {
                        questions = generateAllTwelveLoops()
                        attempts = emptyList()
                        currentIndex = 0
                        userInput = ""
                        quizState = "PLAY"
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Start 12-Loop Quiz", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            "PLAY" -> if (currentQuestion != null) {
                val progress = remainingMs.toFloat() / (currentQuestion.timeLimitSeconds * 1000f)
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Loop ${currentQuestion.loopNumber}/12 • Stage ${currentQuestion.stepInLoop}/7",
                            color = Color(0xFF818CF8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            String.format("%.1fs", remainingMs / 1000f),
                            color = Color(0xFF34D399),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(currentQuestion.title, color = Color(0xFF94A3B8), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(currentQuestion.expression, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold)
                            if (currentQuestion.isPrimeStage) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = currentQuestion.correctAnswer,
                                    color = Color(0xFFF0ABFC),
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (!currentQuestion.isPrimeStage) {
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            label = { Text("Your Answer") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(
                        onClick = { submitCurrent(timedOut = false) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (currentQuestion.isPrimeStage) "Continue to Next Loop →" else "Submit Answer",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            "RESULT" -> {
                val score = attempts.count { it.isCorrect }
                Text("Score: $score / ${questions.size}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(attempts) { _, item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Loop ${item.question.loopNumber} • ${item.question.expression}", fontWeight = FontWeight.Bold)
                                Text("Your Answer: ${item.userAnswer} | Correct: ${item.question.correctAnswer}")
                            }
                        }
                    }
                }
                Button(
                    onClick = { quizState = "HOME" },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Play Again")
                }
            }
        }
    }
}
