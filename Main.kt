import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    checkArgs(args)
    val listWords = File(args[0]).readLines()
    val listCandidateWords = File(args[1]).readLines()
    val secretWord = listCandidateWords.random().lowercase(Locale.getDefault())
    var tries = 0
    val startTime = System.currentTimeMillis()
    val clueList = mutableListOf<String>()
    val wrongCharactersList = mutableListOf<Char>()
    while (true) {
        val guessWord = checkInput(listWords)
        tries += 1
        if (guessWord.length != 5) { println(guessWord); continue }
        val clueString = clueString(
            guessWord.lowercase(Locale.getDefault()),
            secretWord,
            tries,
            startTime,
            clueList,
            wrongCharactersList
        )
        val clueStringList = clueString.first
        val  wrongCharacters = clueString.second
        clueStringList.map { println(it) }
        println()
        println("\u001B[48:5:14m${wrongCharacters.joinToString("")}\u001B[0m")

    }
}

fun checkArgs(args: Array<String>) {
    when {
        args.size != 2 -> { println("Error: Wrong number of arguments.") ; exitProcess(0) }
        !File(args[0]).exists() -> { println("Error: The words file ${args[0]} doesn't exist.") ; exitProcess(0) }
        !File(args[1]).exists() -> { println("Error: The candidate words file ${args[1]} doesn't exist.") ; exitProcess(0) }
    }
    val checkListWords = File(args[0]).readLines().count { it.length != 5 || it.contains("[^a-zA-Z]".toRegex()) || it.toSet().size != 5 }
    val checkCandidateWords = File(args[1]).readLines().count { it.length != 5 || it.contains("[^a-zA-Z]".toRegex()) || it.toSet().size != 5 }
    val checkIncludeWords = File(args[1]).readLines().count { i -> i.lowercase(Locale.getDefault()) !in File(args[0]).readLines().map { it.lowercase(Locale.getDefault()) } }
    when {
        checkListWords == 0 && checkCandidateWords == 0 && checkIncludeWords == 0 -> println("Words Virtuoso")
        checkListWords != 0 -> { println("Error: $checkListWords invalid words were found in the ${args[0]} file.") ; exitProcess(0) }
        checkCandidateWords != 0 -> { println("Error: $checkCandidateWords invalid words were found in the ${args[1]} file.") ; exitProcess(0) }
        checkIncludeWords > 0 -> { println("Error: $checkIncludeWords candidate words are not included in the ${args[0]} file.") ; exitProcess(0) }
    }
}

fun checkInput(listWords: List<String>): String {
    println("\nInput a 5-letter word:")
    val word = readln()
    if (word == "exit") { println("\nThe game is over."); exitProcess(0) }
    return when {
        word.length != 5 -> "The input isn't a 5-letter word."
        word.contains("[^a-zA-Z]".toRegex()) -> "One or more letters of the input aren't valid."
        word.toSet().size != 5 -> "The input has duplicate letters."
        word !in listWords -> "The input word isn't included in my words list."
        else -> word
    }
}

fun clueString(guessWord: String, secretWord: String, tries: Int, startTime: Long, clueStringList: MutableList<String>, wrongCharacters: MutableList<Char>): Pair<MutableList<String>, List<Char>> {
    var clueString = ""
    when {
        guessWord == secretWord && tries == 1 -> {
            guessWord.map { print("\u001B[48:5:10m${it.uppercase(Locale.getDefault())}\u001B[0m") }
            println()
            println("Correct!\nAmazing luck! The solution was found at once.")
            exitProcess(0)
        }

        guessWord == secretWord && tries != 1 -> {
            clueStringList.map { println(it) }
            guessWord.forEach { print("\u001B[48:5:10m${it.uppercaseChar()}\u001B[0m") }
            println()
            println("Correct!\nThe solution was found after $tries tries in ${System.currentTimeMillis() - startTime} seconds.")
            exitProcess(0)
        }
    }
    for (i in guessWord.indices) {
        when {
            guessWord[i] == secretWord[i] -> clueString += "\u001B[48:5:10m${guessWord[i].uppercaseChar()}\u001B[0m"
            guessWord[i] in secretWord && guessWord[i] != secretWord[i] -> clueString += "\u001B[48:5:11m${guessWord[i].uppercaseChar()}\u001B[0m"
            guessWord[i] !in secretWord -> {
                clueString += "\u001B[48:5:7m${guessWord[i].uppercaseChar()}\u001B[0m"; wrongCharacters.add(guessWord[i])
            }
        }
    }
    val characters = wrongCharacters.toSortedSet().map { it.uppercaseChar() }
    clueStringList.add(clueString)
    return Pair(clueStringList, characters)
}