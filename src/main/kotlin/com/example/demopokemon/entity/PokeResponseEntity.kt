package com.example.demopokemon.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class PokeResponseEntity(
    @JsonProperty("name") val name: String,
    @JsonProperty("abilities") val abilities: List<String>,
    @JsonProperty("moves") val moves: List<String>
)