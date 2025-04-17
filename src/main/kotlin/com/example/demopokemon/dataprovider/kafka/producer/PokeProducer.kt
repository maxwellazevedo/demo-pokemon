package com.example.demopokemon.dataprovider.kafka.producer

import lombok.extern.slf4j.Slf4j
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Slf4j
@Component
class PokeProducer(private val kafkaTemplate: KafkaTemplate<String, Any>) {
    private val LOGGER: Logger = LoggerFactory.getLogger(KafkaProducer::class.java)


    @Value("\${topic}")
    private lateinit var pokemonTopic: String

    fun sendPokemon(pokemon: Any) {
        LOGGER.info("m=sendPokemon, i=sendPokemon='{}' to topic='{}'", pokemon, pokemonTopic)
        kafkaTemplate.send(pokemonTopic, pokemon)
    }

}
