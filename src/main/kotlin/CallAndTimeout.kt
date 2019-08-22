import kotlinx.coroutines.*

/**
 * 取消协程的运行
 */
fun test10() = runBlocking {
    val job = launch {
        repeat(1000) { i ->
            println("job: I'm sleeping $i ...")
            kotlinx.coroutines.delay(500L)
        }
    }
    kotlinx.coroutines.delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancel() // 取消该作业
    job.join()  // 等待作业执行结束
    job.cancelAndJoin()
    println("main: Now I can quit.")
}

/***
 * 一段代码必须协作才能被取消
 */
fun test11() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // 一个执行计算的循环，只是为了占用
            // 每次打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    kotlinx.coroutines.delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消一个作业并且等待它结束
    println("main: Now I can quit.")
}

/***
 * 使计算代码可取消
 */
fun test12() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (isActive) { // 可以被取消的计算循环
            // 每次打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    kotlinx.coroutines.delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并等待它结束
    println("main: Now I can quit.")
}

/**
 * 在 finally 中释放资源
 */
fun test13() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                kotlinx.coroutines.delay(500L)
            }
        } finally {
            println("job: I'm running finally")
        }
    }
    kotlinx.coroutines.delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并且等待它结束
    println("main: Now I can quit.")
}

/**
 * 运行不能取消的代码块
 */
fun test14() = runBlocking {
    val job = launch {


        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                kotlinx.coroutines.delay(500L)
            }
        } finally {
            withContext(NonCancellable) {
                println("job: I'm running finally")
                kotlinx.coroutines.delay(1000L)
                println("job: And I've just delayed for 1 sec because I'm non-cancellable")
            }
        }
    }
    kotlinx.coroutines.delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消作业并等待它结束
    println("main: Now I can quit.")
}

fun test15() = runBlocking {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            kotlinx.coroutines.delay(500L)
        }
    }
}

fun test16() =  runBlocking {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Done" // 在它运行得到结果之前取消它
    }
    println("Result is $result")
}

fun main() {
    test16()
}