package com.example.demopokemon.entity
import jakarta.persistence.*

@Entity
@Table(name = "pokemon")
open class PokemonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var name: String = "",

    @Column(columnDefinition = "TEXT")
    var abilities: String = "", // Armazene como JSON ou CSV

    @Column(columnDefinition = "TEXT")
    var moves: String = "" // Armazene como JSON ou CSV
) {
    constructor() : this(null, "", "", "")
}
