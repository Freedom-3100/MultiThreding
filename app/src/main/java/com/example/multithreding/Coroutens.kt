import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

fun main() = runBlocking {
    println("=== 3 Producer - 2 Consumer (Простая версия) ===")

    val channel = Channel<Int>(capacity = 5)

    // 3 Producer корутины
    val producer1 = launch { produceNumbers(channel, 1, 10) }
    val producer2 = launch { produceNumbers(channel, 2, 10) }
    val producer3 = launch { produceNumbers(channel, 3, 10) }

    // 2 Consumer корутины
    val consumer1 = launch { consumeNumbers(channel, 1) }
    val consumer2 = launch { consumeNumbers(channel, 2) }

    // Ждем завершения всех Producer
    joinAll(producer1, producer2, producer3)
    channel.close() // Закрываем канал когда все Producer завершились

    // Ждем завершения всех Consumer
    joinAll(consumer1, consumer2)

    println("Все корутины завершены успешно!")
}

suspend fun produceNumbers(channel: Channel<Int>, producerId: Int, count: Int) {
    for (i in 1..count) {
        val number = producerId * 100 + i
        channel.send(number) // Автоматически ждет если очередь полная
        println("Producer$producerId → $number")
        delay((100..300).random().toLong())
    }
    println("Producer$producerId завершил работу")
}

suspend fun consumeNumbers(channel: Channel<Int>, consumerId: Int) {
    var processed = 0
    for (item in channel) { // Автоматически завершится при закрытии канала
        println("Consumer$consumerId ← $item")
        processed++
        delay((200..500).random().toLong())
    }
    println("Consumer$consumerId завершил работу (обработал $processed чисел)")
}