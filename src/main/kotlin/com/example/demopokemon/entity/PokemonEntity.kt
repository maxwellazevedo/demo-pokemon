package com.example.demopokemon.entity
import jakarta.persistence.*

@Entity
@Table(name = "pokemon")
open class PokemonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String = "",

    @ElementCollection
    @CollectionTable(name = "pokemon_abilities", joinColumns = [JoinColumn(name = "pokemon_id")])
    @Column(name = "ability")
    var abilities: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "pokemon_moves", joinColumns = [JoinColumn(name = "pokemon_id")])
    @Column(name = "move")
    var moves: MutableList<String> = mutableListOf()
) {
    constructor() : this(null, "", mutableListOf(), mutableListOf())
}
