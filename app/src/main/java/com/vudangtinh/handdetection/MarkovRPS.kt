package com.vudangtinh.handdetection

class MarkovRPS {
    private val transitions = mutableMapOf<String, MutableMap<String, Int>>()
    private val history = mutableListOf<String>()
    private val moves = listOf("búa", "kéo", "bao")

    init {
        for (move in moves) {
            transitions[move] = mutableMapOf()
            for (next in moves) {
                transitions[move]!![next] = 0
            }
        }
    }

    fun play(playerMove: String): Result {
        val aiMove = predictNextMove()
        val lastMove = history.lastOrNull()
        if (lastMove != null) {
            updateTransition(lastMove, playerMove)
        }
        history.add(playerMove)
        val result = judge(playerMove, aiMove)
        return Result(playerMove, aiMove, result)
    }

    private fun predictNextMove(): String {
        val last = history.lastOrNull()
        if (last == null || transitions[last].isNullOrEmpty()) {
            return randomMove()
        }

        val options = transitions[last]!!
        val predicted = options.maxByOrNull { it.value }?.key ?: randomMove()
        return counter(predicted)
    }

    private fun updateTransition(prev: String, current: String) {
        transitions[prev]?.let {
            it[current] = it.getOrDefault(current, 0) + 1
        }
    }

    private fun randomMove(): String = moves.random()

    private fun counter(move: String): String = when (move) {
        "búa" -> "bao"
        "kéo" -> "búa"
        "bao" -> "kéo"
        else -> randomMove()
    }

    private fun judge(player: String, ai: String): String = when {
        player == ai -> "hòa"
        player == "búa" && ai == "kéo" -> "thắng"
        player == "kéo" && ai == "bao" -> "thắng"
        player == "bao" && ai == "búa" -> "thắng"
        else -> "thua"
    }

    data class Result(val playerMove: String, val aiMove: String, val result: String)
}
