import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * 通道：
 *      延期的值提供一个一种便捷的方法使单个值在多个协程之间相互传输。
 *      通道提供了一种在流中传输值得方法。
 *
 *      Channel : BlockingQueue
 *      put     : send // 把 anObject 加到 BlockingQueue 里,如果 BlockQueue 没有空间,则调用此方法的线程被阻断直到 BlockingQueue 里面有空间再继续.
 *      take    : receive // 取走 BlockingQueue 里排在首位的对象,若 BlockingQueue 为空,阻断进入等待状态直到 BlockingQueue 有新的对象被加入为止
 */
fun test20() = runBlocking {
    val channel = Channel<Int>()
    launch {
        // 这里可能是消耗大量 CPU 运算的异步逻辑，我们将仅仅做 5 次整数的平方并发送
        for (x in 1..5) channel.send(x * x)
    }
    // 这里我们打印了 5 次接收的整数
    repeat(5) {
        println(channel.receive())
    }
    println("Done!")
}

/***
 * 关闭与迭代通道
 */
fun test21() = runBlocking {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x * x)
        channel.close() // 这里保证所有先前发送出去的元素都在通道关闭前被接收到。
//        channel.send(6 * 6) ClosedSendChannelException
    }
    // 这里我们使用 'for' 循环来打印所有收到的元素（直到通道被关闭）
    for (y in channel) println(y)
    println("Done!")
}


fun tes22() = runBlocking {
    val squares = produceSquares()
    squares.consumeEach { println(it) } // 使用扩展函数 consumeEach 在消费者端替代 for 循环
    println("Done!")
}

/**
 * 将生产者抽象成一个函数，并且使用通道作为它的参数
 **/
fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) send(x * x)
}

fun test23() = runBlocking {
    val numbers = produceNumbers() // 从 1 开始生成整数
    val squares = square(numbers) // 对整数做平方
    val squares2 = square(numbers) // 对整数做平方
    for (i in 1..5) println(squares.receive()) // 打印前 5 个数字
    println()
    for (i in 1..5) println(squares2.receive()) // 打印前 5 个数字
    println("Done!")
    coroutineContext.cancelChildren() // 取消子协程
}

/***
 * 管道是一种一个协程在流中开始产生无穷多个元素的模式
 */
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) send(x++) // 从 1 开始的无限的整数流
}

/**
 * 并且另一个或多个协程开始消费这些流，做一些操作，产生一些额外的结果
 */
fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (x in numbers) send(x * x)
}

/**
 * 使用管道的素数
 */
fun test24() = runBlocking {
    var cur = numbersFrom(2)
    for (i in 1..10) {
        var prime = cur.receive()
        println(prime)
        cur = filter(cur, prime)
    }
    coroutineContext.cancelChildren() // 取消所有的子协程让主协程结束
}

fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) // 开启了一个无限的整数流
}

fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) if (x % prime != 0) send(x)
}

/**
 * 扇出
 */
fun test25() = runBlocking<Unit> {
    val producer = produceNumbers2()
    repeat(5) { launchProcessor(it, producer) }
    delay(950)
    producer.cancel() // 取消协程生产者从而将它们全部杀死
}


fun CoroutineScope.produceNumbers2() = produce<Int> {
    var x = 1 // 从 1 开始
    while (true) {
        send(x++) // 产生下一个数字
        kotlinx.coroutines.delay(100) //等待 0.1 s
    }
}

fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    //    channel.consumeEach { msg-> //  consumeEach 编写的生产者始终在正常或非正常完成时消耗（取消）底层通道
//        println("Processor #$id received $msg")
//    }
    for (msg in channel) {
        println("Processor #$id received $msg")
    }
}

/***
 * 多个协程可以发送到同一个通道
 */
fun test26() = runBlocking {
    val channel = Channel<String>()
    launch { sendString(channel, "foo", 200L) }
    launch { sendString(channel, "BAR!", 500L) }
    repeat(6) {
        // 接收前 6 个
        println(channel.receive())
    }
    coroutineContext.cancelChildren()
}

suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}


/***
 * 带缓冲的通道
 */
fun test27() = runBlocking {
    val channel = Channel<Int>(4) // 启动缓冲的通道
    val sender = launch {
        // 启动发送者协程
        repeat(10) {
            println("Sending $it") // 在每个元素发送前打印它们
            channel.send(it) // 将在缓冲区被占满时挂起
        }
    }
    // 没有接收到东西... 只是等待 ...
    kotlinx.coroutines.delay(1000)
    sender.cancel()
}

/**
 * 通道是公平的，遵循先进先出原则
 *      第一个协程调用 receive 取得元素后，下次就是就是第二个协程取数据
 */
fun test28() = runBlocking {
    val table = Channel<Ball>() // 一个共享的 table (桌子）
    launch { player("ping", table) }
    launch { player("pong", table) }
    table.send(Ball(0)) // 乒乓球
    kotlinx.coroutines.delay(1000L)
    coroutineContext.cancelChildren()
}

suspend fun player(name: String, table: Channel<Ball>) {
//    while (true) {
//        var ball =  table.receive()
//        ball.hits++
//        println("$name $ball")
//        delay(300) // 等待一段时间
//        table.send(ball) // 将球发送回去
//    }
    for (ball in table) { // 在循环中接收球
        ball.hits++
        println("$name $ball")
        delay(300) // 等待一段时间
        table.send(ball) // 将球发送回去
    }
}

data class Ball(var hits: Int)

fun test29() = runBlocking {
    val tickerChannel = ticker(delayMillis = 100,initialDelayMillis = 0) // 创建计时器通道
    var nextElement = withTimeoutOrNull(1){ tickerChannel.receive()}
    println("Initial element is available immediately: $nextElement") // 初始化未经处理的延迟

    nextElement = withTimeoutOrNull(50) { tickerChannel.receive()} // 所有随后到来的元素都经过了 100 毫秒的延迟
    println("Next element is not ready in 50 ms: $nextElement")

    nextElement = withTimeoutOrNull(60) { tickerChannel.receive()}
    println("Next element is ready in 100 ms: $nextElement")

    //模拟大量消费延迟
    println("Consumer pauses for 150ms")
    kotlinx.coroutines.delay(150)
    // 下一个元素立即可用
    nextElement = withTimeoutOrNull(1){ tickerChannel.receive()}
    println("Next element is available immediately after large consumer delay: $nextElement")
    // 请注意，`receive` 调用之间的暂停被考虑在内，下一个元素的到达速度更快
    nextElement = withTimeoutOrNull(60) { tickerChannel.receive()}
    println("Next element is ready is 50ms after consumer pause in 150ms: $nextElement")

    tickerChannel.cancel() // 表明不再需要更多的元素
}


fun main() {
    test29()
}