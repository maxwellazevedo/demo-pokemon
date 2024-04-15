package com.example.demopokemon.dataprovider.kafka.consumer

import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
class PokeConsumer {

//    private val LOGGER: Logger = LoggerFactory.getLogger(KafkaConsumer::class.java)
//
//    private var payload: PokeResponse? = null
//
//    @KafkaListener(topics = ["\${topic}"], groupId = "my_group_id")
//    fun getMessage(consumerRecord: ConsumerRecord<*, *>) {
//        LOGGER.info("Consumer message ='{}'", consumerRecord.toString())
//        setPayload(consumerRecord.toString())
//    }
//
//    private fun setPayload(payload: PokeResponse) {
//        this.payload = payload
//    }
//
//    fun getPayload(): String? {
//        return payload
//    }
}