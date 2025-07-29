package com.vudangtinh.handdetection

class MarkovRPS {
    private val moves = listOf("búa", "kéo", "bao")
    private val transitions = mutableMapOf<String, MutableMap<String, Int>>()
    private val history = mutableListOf<String>()

    init {
        // Khởi tạo ma trận chuyển trạng thái
        moves.forEach { move ->
            transitions[move] = mutableMapOf("búa" to 0, "kéo" to 0, "bao" to 0)
        }
    }

    // Gọi để chơi 1 lượt: nhập vào nước người chơi, trả về kết quả
    fun play(playerMove: String): Result {
        val lastMove = history.lastOrNull()
        if (lastMove != null) {
            transitions[lastMove]?.let {
                it[playerMove] = it.getOrDefault(playerMove, 0) + 1
            }
        }

        val predictedPlayerMove = predictNextPlayerMove()
        val aiMove = counter(predictedPlayerMove)

        history.add(playerMove)

        val result = judge(playerMove, aiMove)
        return Result(playerMove, aiMove, result)
    }

    // Dự đoán nước tiếp theo của người chơi (theo lịch sử)
    fun predictNextPlayerMove(): String {
        val last = history.lastOrNull() ?: return moves.random()
        val freqMap = transitions[last] ?: return moves.random()
        return freqMap.maxByOrNull { it.value }?.key ?: moves.random()
    }

    // Dự đoán nước AI nên ra để khắc chế người chơi
    fun predictBestAIMove(): String {
        return counter(predictNextPlayerMove())
    }

    // Nước thắng nước truyền vào
    private fun counter(move: String): String = when (move) {
        "búa" -> "bao"
        "kéo" -> "búa"
        "bao" -> "kéo"
        else -> moves.random()
    }

    // So kết quả
    private fun judge(player: String, ai: String): String = when {
        player == ai -> "hòa"
        player == "búa" && ai == "kéo" -> "thắng"
        player == "kéo" && ai == "bao" -> "thắng"
        player == "bao" && ai == "búa" -> "thắng"
        else -> "thua"
    }

    // Kết quả mỗi lượt chơi
    data class Result(val playerMove: String, val aiMove: String, val result: String)

    // Gọi để in ma trận học được (debug hoặc hiển thị)
    fun getTransitions(): Map<String, Map<String, Int>> = transitions
}
