package com.example.demopokemon.adapter.messaging

import com.example.demopokemon.domain.model.Pokemon
import com.example.demopokemon.domain.port.PokemonRepositoryPort
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Adaptador de entrada — consome mensagens do tópico Kafka e persiste via porta de domínio.
 */
@Component
class PokeConsumer(
    private val pokemonRepository: PokemonRepositoryPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // Usado apenas em testes para verificar a última mensagem recebida
    @Volatile
    private var lastReceived: PokemonResponseDTO? = null

    @KafkaListener(topics = ["\${topic}"], groupId = "my_group_id")
    fun getMessage(consumerRecord: ConsumerRecord<String, Pokemon>) {
        val pokemon = consumerRecord.value()
        log.info("m=getMessage, pokemon='{}'", pokemon.name)
        pokemonRepository.save(pokemon)
        lastReceived = PokemonResponseDTO.fromDomain(pokemon)
    }

    fun getLastReceived(): PokemonResponseDTO? = lastReceived

    fun clearLastReceived() {
        lastReceived = null
    }
}
