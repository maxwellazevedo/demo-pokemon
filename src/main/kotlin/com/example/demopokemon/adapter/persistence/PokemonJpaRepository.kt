package com.example.demopokemon.adapter.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PokemonJpaRepository : JpaRepository<PokemonJpaEntity, Long> {
    fun findByName(name: String): PokemonJpaEntity?
}
