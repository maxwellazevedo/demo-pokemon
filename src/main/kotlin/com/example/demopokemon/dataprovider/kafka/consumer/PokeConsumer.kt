package com.example.demopokemon.dataprovider.kafka.consumer

import com.example.demopokemon.entity.PokeResponseEntity
import lombok.extern.slf4j.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Slf4j
@Component
class PokeConsumer {

    private val LOGGER = LoggerFactory.getLogger(PokeConsumer::class.java)

    private var payload: PokeResponseEntity? = null

    @KafkaListener(topics = ["\${topic}"], groupId = "my_group_id")
    fun getMessage(consumerRecord: ConsumerRecord<String, PokeResponseEntity>) {
        LOGGER.info("m=getMessage, i=consumer_pokemon='{}'", consumerRecord.value())
        setPayload(consumerRecord.value())
    }

    private fun setPayload(payload: PokeResponseEntity?) {
        this.payload = payload
    }

    fun getPayload(): PokeResponseEntity? {
        return payload
    }

}