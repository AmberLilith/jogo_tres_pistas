package com.br.amber.jogodastrespistas.models

import com.br.amber.jogodastrespistas.enums.RoomStatusesEnum

data class Room(
    val id: String = "",
    val owner: Player = Player(),
    val guest: Player = Player(id = "", online = false),
    var ownerTurn: Boolean = false,
    var status: String = RoomStatusesEnum.WAITING_GUEST.name,
    var chosenWordIndex: Int = -1,
    var cluesShown: Int = -1,
    var round: Int = 0,
    val drawnWords: List<Word> = emptyList(),
    val usedWords: MutableList<String> = mutableListOf()
) {
    constructor() : this(id = "", owner = Player())

    companion object{
        const val NUMBER_OF_ROUNDS = 2


        const val DIALOGS_MILLISECONDS_DELAY = 3000L

        const val ANSWER_TIMEOUT_SECONDS = 30
    }
}
