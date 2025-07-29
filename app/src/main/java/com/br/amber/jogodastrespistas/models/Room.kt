package com.br.amber.jogodastrespistas.models

data class Room(
    val id: String = "",
    val owner: Player = Player(),
    val guest: Player = Player(id = "guest", online = false),
    var ownerTurn: Boolean = true,
    var status: String = RoomStatusesEnum.WAITING.status,
    var cluesShown: Int = 0, //Precisa começar em 0, pois será usado obter o índice do da lista drawnWords[$index].clues
    var round: Int = 0,
    val drawnWords: List<Word> = emptyList()
) {
    constructor() : this(id = "", owner = Player())

    companion object{
        const val NUMBER_OF_ROUNDS = 2
    }
}
