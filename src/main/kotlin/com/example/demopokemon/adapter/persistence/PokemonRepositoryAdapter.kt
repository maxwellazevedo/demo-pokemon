package com.example.demopokemon.adapter.persistence

import com.example.demopokemon.domain.model.Pokemon
import com.example.demopokemon.domain.port.PokemonRepositoryPort
import org.springframework.stereotype.Component

/**
 * Adaptador de persistência — implementa a porta de saída usando JPA.
 */
@Component
class PokemonRepositoryAdapter(
    private val jpaRepository: PokemonJpaRepository
) : PokemonRepositoryPort {

    override fun findByName(name: String): Pokemon? =
        jpaRepository.findByName(name)?.toDomain()

    override fun findAll(): List<Pokemon> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun save(pokemon: Pokemon): Pokemon =
        jpaRepository.save(PokemonJpaEntity.fromDomain(pokemon)).toDomain()
}
