package com.br.amber.jogodastrespistas.models

data class Room(
    val id: String = "",
    val owner: Player = Player(),
    val guest: Player = Player(id = "guest", online = false),
    var ownerTurn: Boolean = true,
    var status: String = RoomStatusesEnum.WAITING.status,
    var cluesShown: Int = 1,
    var round: Int = 0,
    val drawnWords: List<Word> = emptyList()
) {
    constructor() : this(id = "", owner = Player())
}
