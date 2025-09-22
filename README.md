# Demo Pokemon Application

Este projeto é uma aplicação Spring Boot desenvolvida em Kotlin que consome a PokeAPI para buscar informações sobre Pokémon e publica os dados em um tópico Kafka.

## Tecnologias Utilizadas
- **Kotlin**
- **Spring Boot**
- **Spring WebFlux**
- **Spring Kafka**
- **Gradle**
- **PokeAPI**

## Funcionalidades
- Buscar informações de Pokémon (nome, habilidades, movimentos) através da PokeAPI.
- Publicar os dados obtidos em um tópico Kafka.

## Configuração
### Pré-requisitos
- Java 17+
- Gradle
- Kafka em execução localmente (porta padrão: `9092`)

### Configuração do Kafka
No arquivo `src/main/resources/application.yml`, configure o servidor Kafka:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092