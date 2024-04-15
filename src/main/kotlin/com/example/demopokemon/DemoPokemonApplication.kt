package com.example.demopokemon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
class DemoPokemonApplication

fun main(args: Array<String>) {
	runApplication<DemoPokemonApplication>(*args)
}
