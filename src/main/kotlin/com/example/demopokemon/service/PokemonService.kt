package com.example.demopokemon.service

import com.example.demopokemon.repository.PokemonRepository
import org.springframework.stereotype.Service

@Service
class PokemonService(private val pokemonRepository: PokemonRepository) {

//    fun savePokemon(pokemon: Pokemon): Pokemon {
//        return pokemonRepository.save(pokemon)
//    }
//
//    fun getAllPokemon(): List<Pokemon> {
//        return pokemonRepository.findAll()
//    }
}