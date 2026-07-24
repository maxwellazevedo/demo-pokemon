package com.example.demopokemon.adapter.messaging

import com.example.demopokemon.domain.model.Pokemon
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

/**
 * Adaptador de saída — publica Pokémon no tópico Kafka configurado.
 */
@Component
class PokeProducer(private val kafkaTemplate: KafkaTemplate<String, Any>) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${topic}")
    private lateinit var pokemonTopic: String

    fun sendPokemon(pokemon: Pokemon) {
        log.info("m=sendPokemon, pokemon='{}', topic='{}'", pokemon.name, pokemonTopic)
        kafkaTemplate.send(pokemonTopic, pokemon)
    }
}
