package ru.avito.util

public sealed class Characters(internal val chars: List<Char>) {

    private companion object {
        private val alphabeticLowercase = ('a'..'z').toList()
        private val alphabeticUppercase = ('A'..'Z').toList()
        private val alphabetic = alphabeticLowercase + alphabeticUppercase
        private val digits = ('0'..'9').toList()
        private val punctuations = listOf('.', ',', '!', '?', ';', ':', '-', '(', ')', '"')
        private val specials = listOf('@', '#', '$', '%', '^', '&', '*', '{', '}', '[', ']', '/', '\\', '|')
    }

    public object AlphabeticLowercase : Characters(
        chars = alphabeticLowercase
    )

    public object AlphabeticUppercase : Characters(
        chars = alphabeticUppercase
    )

    public object Alphabetic : Characters(
        chars = alphabetic
    )

    public object Digits : Characters(
        chars = digits
    )

    public object Punctuation : Characters(
        chars = punctuations
    )

    public object Special : Characters(
        chars = specials
    )

    public object Alphanumeric : Characters(
        chars = alphabetic + digits
    )

    public object Text : Characters(
        chars = alphabetic + digits + punctuations
    )

    public class SingleChar(char: Char) : Characters(listOf(char))

    public object All : Characters(
        chars = alphabetic + digits + punctuations + specials
    )
}
