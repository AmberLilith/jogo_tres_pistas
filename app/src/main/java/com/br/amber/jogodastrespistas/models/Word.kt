package com.br.amber.jogodastrespistas.models

data class Word(
    val name: String = "",          // Adicione 'val' e valor padrão
    val firstClue: String = "",     // Todas propriedades devem ser
    val secondClue: String = "",    // declaradas como val/var
    val thirdClue: String = ""      // com valores padrão
) {
    // Construtor vazio para Firebase
    constructor() : this("", "", "", "")
}