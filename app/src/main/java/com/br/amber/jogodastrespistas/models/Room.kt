package com.br.amber.jogodastrespistas.models

class Room(
    var ownerId: String,
    var owner: Player = Player(id = ownerId, isOnline = true),
    var guest: Player = Player(id = "guest", isOnline = false),
    var ownerTurn: Boolean = true,
    var status: RoomStatusesEnum = RoomStatusesEnum.WAITING,
    var cluesShown: Int = 0,
    var round: Int = 0,
    var drawnWords: Set<Word>
)