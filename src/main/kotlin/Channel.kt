import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
    for (i in 1..5) println(squares.receive() ) // 打印前 5 个数字
    println()
    for (i in 1..5) println(squares2.receive() ) // 打印前 5 个数字
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

fun main() {
    test23()
}