package com.example.demopokemon.dataprovider.kafka.consumer

import com.example.demopokemon.entity.PokeResponseEntity
import lombok.extern.slf4j.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import kotlin.math.log

@Slf4j
@Component
class PokeConsumer {

    private val log = LoggerFactory.getLogger(javaClass)

    private var payload: PokeResponseEntity? = null

    @KafkaListener(topics = ["\${topic}"], groupId = "my_group_id")
    fun getMessage(consumerRecord: ConsumerRecord<String, PokeResponseEntity>) {
        log.info("m=getMessage, i=consumer_pokemon='{}'", consumerRecord.value())
        setPayload(consumerRecord.value())
    }

    private fun setPayload(payload: PokeResponseEntity?) {
        this.payload = payload
    }

    fun getPayload(): PokeResponseEntity? {
        log.info("m=getPayload, i=getting_payload='{}'", payload)
        return payload
    }

    fun clearPayload() {
        // Zera o payload para evitar dados antigos
        payload = null
    }

}