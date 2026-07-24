# Demo Pokemon Application

Este projeto é uma aplicação Spring Boot desenvolvida em Kotlin que consome a PokeAPI para buscar informações sobre Pokémon e publica os dados em um tópico Kafka. Ele segue uma arquitetura baseada em boas práticas de desenvolvimento, como a Arquitetura Hexagonal (Ports and Adapters), para garantir modularidade, testabilidade e facilidade de manutenção.

## Tecnologias Utilizadas
- **Kotlin**
- **Spring Boot**
- **Spring WebFlux**
- **Spring Kafka**
- **Gradle**
- **PokeAPI**
- **Docker e Docker Compose** (para orquestração de serviços)

## Arquitetura do Projeto

O projeto adota a **Arquitetura Hexagonal (Ports and Adapters)**, que promove o desacoplamento entre o núcleo da aplicação e as dependências externas. A estrutura é organizada da seguinte forma:

- **Core (Núcleo)**: Contém a lógica de negócios e as regras da aplicação.
  - **Casos de Uso**: Implementam as regras de negócio, como buscar informações de Pokémon e publicá-las no Kafka.
  - **Entidades**: Modelos do domínio, como Pokémon.
- **Ports (Portas)**: Interfaces que definem como o núcleo interage com o mundo externo.
  - **Driving Ports**: Interfaces para entrada de dados (ex.: controladores REST).
  - **Driven Ports**: Interfaces para saída de dados (ex.: repositórios, clientes HTTP).
- **Adapters (Adaptadores)**: Implementações concretas das portas.
  - **Driving Adapters**: Controladores REST para expor endpoints.
  - **Driven Adapters**: Repositórios para persistência e clientes HTTP para consumir a PokeAPI.

## Funcionalidades
- Buscar informações de Pokémon (nome, habilidades, movimentos) através da PokeAPI.
- Publicar os dados obtidos em um tópico Kafka.

## Boas Práticas Adotadas
- **Desacoplamento**: Uso da Arquitetura Hexagonal para isolar o núcleo da aplicação.
- **Testes Automatizados**: Testes unitários e de integração com relatórios gerados automaticamente.
- **Configuração Centralizada**: Arquivos `application.yml` e `application.properties` para gerenciar configurações de ambiente.
- **Orquestração com Docker**: Uso de `docker-compose.yml` para facilitar a execução de serviços como Kafka.

## Configuração

### Pré-requisitos
- Java 17+
- Gradle
- Docker e Docker Compose (para executar o Kafka localmente)

### Configuração do Kafka
No arquivo `src/main/resources/application.yml`, configure o servidor Kafka:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### Executando o Projeto
1. Certifique-se de que o Kafka está em execução. Você pode usar o `docker-compose.yml` para subir o serviço:
   ```bash
   docker-compose up -d
   ```
2. Compile e execute o projeto com o Gradle:
   ```bash
   ./gradlew bootRun
   ```

## Testes

O projeto inclui testes automatizados para garantir a qualidade do código:
- **Testes Unitários**: Localizados em `src/test/kotlin`, testam componentes isolados.
- **Testes de Integração**: Validam a interação entre componentes e serviços externos.
- **Relatórios de Teste**: Gerados automaticamente em `build/reports/tests/test/index.html`.

Para executar os testes, use o comando:
```bash
./gradlew test
```

## Estrutura do Projeto
- **src/main/kotlin**: Código-fonte principal.
- **src/main/resources**: Arquivos de configuração e recursos estáticos.
- **src/test/kotlin**: Testes automatizados.
- **build/**: Diretório gerado pelo Gradle contendo artefatos de build e relatórios.

## Contribuição
Contribuições são bem-vindas! Siga as etapas abaixo para contribuir:
1. Faça um fork do repositório.
2. Crie uma branch para sua feature ou correção de bug.
3. Envie um pull request com uma descrição detalhada das alterações.

---

Este README fornece uma visão geral do projeto, suas funcionalidades, arquitetura e práticas adotadas. Para mais informações, consulte os arquivos de configuração e documentação no repositório.
