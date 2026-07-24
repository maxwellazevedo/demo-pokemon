package com.example.demopokemon.adapter.persistence

import com.example.demopokemon.domain.model.Pokemon
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * Entidade JPA — adaptador de persistência.
 * abilities e moves são mapeados com @JdbcTypeCode(SqlTypes.JSON):
 * - PostgreSQL: coluna jsonb (nativo)
 * - H2 (testes): coluna json/varchar compatível, inferida pelo dialeto
 */
@Entity
@Table(name = "pokemon")
open class PokemonJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(unique = true, nullable = false)
    open var name: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    open var abilities: List<String> = emptyList(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    open var moves: List<String> = emptyList()
) {
    constructor() : this(null, "", emptyList(), emptyList())

    fun toDomain(): Pokemon = Pokemon(
        id = id,
        name = name,
        abilities = abilities,
        moves = moves
    )

    companion object {
        fun fromDomain(pokemon: Pokemon): PokemonJpaEntity = PokemonJpaEntity(
            id = pokemon.id,
            name = pokemon.name,
            abilities = pokemon.abilities,
            moves = pokemon.moves
        )
    }
}
