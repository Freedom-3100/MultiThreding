import java.util.*
import kotlin.concurrent.thread

class ProducerConsumer(private val maxSize: Int = 5) {

    private val queue = LinkedList<Int>()
    private val lock = Object()
    private var producerFinished = false

    fun start() {
        val producer = createProducer()
        val consumer = createConsumer()

        producer.join() // Ждем пока Producer создаст все числа
        producerFinished = true // Сообщаем, что Producer завершил

        synchronized(lock) {
            lock.notifyAll() // Будим Consumer, если он ждет
        }

        consumer.join() // Ждем пока Consumer обработает все числа

        println("Программа завершена. Все числа созданы и обработаны.")
    }

    private fun createProducer(): Thread {
        return thread {
            for (i in 1..20) {
                try {
                    produce(i)
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    break
                }
            }
            println("Producer завершил работу (создал все 20 чисел)")
        }
    }

    private fun createConsumer(): Thread {
        return thread {
            while (!producerFinished || queue.isNotEmpty()) {
                try {
                    consume()
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    break
                }
            }
            println("Consumer завершил работу (обработал все числа)")
        }
    }

    private fun produce(item: Int) {
        synchronized(lock) {
            while (queue.size >= maxSize) {
                println("Producer: Очередь полная, жду...")
                lock.wait()
            }

            queue.add(item)
            println("Producer → $item [очередь: ${queue.size}]")
            lock.notifyAll()
        }
    }

    private fun consume() {
        synchronized(lock) {
            while (queue.isEmpty()) {
                if (producerFinished) return // Выходим если Producer завершил и очередь пустая
                println("Consumer: Очередь пустая, жду...")
                lock.wait()
            }

            val item = queue.removeFirst()
            println("Consumer ← $item [очередь: ${queue.size}]")
            lock.notifyAll()
        }
    }
}

fun main() {
    println("=== Producer-Consumer ===")
    ProducerConsumer(5).start()
}