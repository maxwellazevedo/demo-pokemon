package com.example.demopokemon.domain.exception

class PokemonNotFoundException(val pokemonName: String) :
    RuntimeException("Pokemon '$pokemonName' not found")
