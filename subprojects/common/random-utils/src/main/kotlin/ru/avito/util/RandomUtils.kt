@file:JvmName("RandomUtils")

package ru.avito.util

import java.util.ArrayList
import java.util.Date
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

private val random: ThreadLocalRandom
    get() = ThreadLocalRandom.current()

private const val DEFAULT_MAX_INT = 10

public fun randomInt(): Int {
    return random.nextInt(Int.MAX_VALUE)
}

public fun randomIntAsString(): String {
    return randomInt().toString()
}

public fun randomIntAsString(maxValue: Int): String {
    return randomInt(maxValue).toString()
}

public fun randomInt(maxValue: Int): Int {
    return random.nextInt(maxValue)
}

public fun randomInt(min: Int, max: Int): Int {
    require(max >= min) {
        "`min` must be lower or equal then `max`"
    }
    return min + random.nextInt(max - min + 1)
}

public fun randomDouble(): Double {
    return random.nextDouble()
}

public fun randomDouble(min: Double, max: Double): Double {
    return random.nextDouble(min, max)
}

public fun randomFloat(): Float {
    return random.nextFloat()
}

public fun randomLong(): Long {
    return randomLong(min = 0, max = Long.MAX_VALUE)
}

public fun randomLong(min: Long, max: Long): Long {
    require(max >= min) {
        "`min` must be lower or equal then `max`"
    }
    return random.nextLong(min, max)
}

public fun randomBoolean(): Boolean {
    return random.nextBoolean()
}

public fun randomString(): String {
    return "test_" + randomInt()
}

public fun randomUrlString(): String {
    return "https://example.com/" + randomString()
}

public fun randomStringList(capacity: Int = randomInt(DEFAULT_MAX_INT)) = List(capacity) { randomString() }

public fun randomLongArray(): LongArray {
    val array = LongArray(randomInt(DEFAULT_MAX_INT))
    for (i in array.indices) {
        array[i] = randomInt().toLong()
    }
    return array
}

public fun randomNullableString(): String? {
    val random = Random()
    val isNullable = random.nextInt(3) == 0
    return if (isNullable) {
        null
    } else {
        randomString()
    }
}

public fun randomString(length: Int, symbols: Characters = Characters.Alphanumeric): String {
    val random = Random()
    val builder = StringBuilder()
    repeat(length) {
        val index = random.nextInt(symbols.chars.size)
        builder.append(symbols.chars[index])
    }
    return builder.toString()
}

public fun randomIntArray(): IntArray = IntArray(randomInt(DEFAULT_MAX_INT)).map { randomInt() }.toIntArray()

public fun randomByteArray(): ByteArray = ByteArray(randomInt(DEFAULT_MAX_INT)).apply { random.nextBytes(this) }

public inline fun <reified T : Any> List<T>.randomElement(): T = this[randomInt(this.size)]

public inline fun <reified T : Any> Array<T>.randomElement(): T = this[randomInt(this.size)]

public fun randomPassword(charLength: Int): String {
    return randomString(charLength, Characters.Alphabetic) + randomInt(DEFAULT_MAX_INT)
}

/**
 * Generates random e-mail.
 * <p>
 * Note: `@avito-test.ru` is needed to allow deleting phones used in registration in functional tests.
 *
 * @return random e-mail address in @avito-test.ru domain
 */
public fun randomEmail(): String {
    return randomString(6, Characters.AlphabeticLowercase) + "@avito-test.ru"
}

public fun randomPhone(): String {
    return "+7" + randomString(10, Characters.Digits)
}

@Deprecated("start and end could be negative", replaceWith = ReplaceWith("randomDate(start, end)"))
public fun randomDate(start: Long, end: Long): Date {
    return Date(randomLong(start, end))
}

public fun randomDate(start: Date, end: Date): Date {
    return Date(randomLong(start.time, end.time))
}

public fun randomDate(): Date = Date(randomLong(min = 0, max = Long.MAX_VALUE))

public fun randomDateInMillis(): Long = randomDate().time
