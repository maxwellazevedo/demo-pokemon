package com.example.demopokemon.repository

import com.example.demopokemon.entity.PokemonEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PokemonRepository : JpaRepository<PokemonEntity, Long> {
    fun findByName(name: String): PokemonEntity?
}