import kotlinx.coroutines.*
import kotlin.concurrent.thread


fun test01() {
    GlobalScope.launch { // 在后台启动一个新的协程并继续
        delay(1000L) // 非阻塞的等待 1 s
        println("World!")
    }
    print("Hello,")
    Thread.sleep(2000L)
}

fun test02() {
    thread {
        Thread.sleep(1000L) // 阻塞式
        println("World!")
    }
    print("Hello,")
    Thread.sleep(2000L)
}

/**
 * runBlocking 表示阻塞， 惯用方式见 test04
 */

fun test03() {
    GlobalScope.launch { // 后台启动一个新的协程并继续
        delay(1000L)
        print("World!")
    }
    print("Hello,") // 主线程代码会立即执行
    runBlocking { // 但是这个表达式阻塞了主线程
        delay(2000L) // ...我们延迟 2 秒来保证 jvm 的存活
    }
}


fun test04() = runBlocking<Unit> {
    // 开始执行主协程
    GlobalScope.launch { // 在后台启动一个新的协程并继续
        delay(1000L)
        print("World")
    }
    print("Hello,") // 主协程在这里立即执行
    delay(2000L) // 延迟 2 秒保证 jvm 存活
}

/***
 * 等待一个作业
 */
fun test05()= runBlocking {
    val job = GlobalScope.launch {  // 启动一个新协程并保持对这个作业的引用
        kotlinx.coroutines.delay(1000L)
        print("World!")
    }
    print("Hello,")
    job.join() // 等待直到子协程执行结束
}

/***
 * 结构化的并发
 * GlobalScope.launch 是顶层的协程
 * launch 是在作用域内启动的协程
 */
fun test06() = runBlocking {
    // launch 的 CoroutineScope 会添加到所在作用域中
    launch {
        kotlinx.coroutines.delay(1000L)
        println("World!")
    }
    print("Hello,")
    // 无需 join , 因为外部协程（示例中的 runBlocking）直到在其作用域中启动的所有协程都执行完毕后才会结束。
}

fun test07() = runBlocking {
    launch {
        kotlinx.coroutines.delay(200L)
        println("Task from runBlocking")
    }

    coroutineScope { // 创建一个协程作用域 , 只有该协程作用域结束后才会执行后面的代码
        launch {
            kotlinx.coroutines.delay(500L)
            println("Task from nested launch")
        }

        kotlinx.coroutines.delay(100L)
        println("Task from coroutine scope") // 这一行会在内嵌 launch 之前输出
    }

    println("Coroutine scope is over") // 这一行会在内嵌 launch 之前输出
}


fun test08() = runBlocking {
    repeat(100_000) {
        launch {
            kotlinx.coroutines.delay(1000L)
            print(".")
        }
    }
}

/***
 * 全局协程像守护线程
 */
fun test09() = runBlocking {
    GlobalScope.launch { // GlobalScope 中启动的活动协程并不会使进程保活。它们就像守护线程
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L) // 在延迟后退出
}

fun main(){
    test09()
}


