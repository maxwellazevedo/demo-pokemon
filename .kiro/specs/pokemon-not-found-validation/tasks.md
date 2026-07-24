# Plano de Implementação: pokemon-not-found-validation

## Visão Geral

Implementar o tratamento correto de Pokémon não encontrado na PokeAPI. A solução introduz a exceção de domínio `PokemonNotFoundException`, modifica o `PokeApiService` para mapear respostas 404 a essa exceção via `onErrorMap`, e atualiza o `PokemonController` para responder com HTTP 404 tipado. O comportamento para respostas 2xx e erros não-404 permanece inalterado.

## Tasks

- [x] 1. Criar a exceção de domínio `PokemonNotFoundException`
  - Criar o arquivo `src/main/kotlin/com/example/demopokemon/domain/exception/PokemonNotFoundException.kt`
  - Definir como `RuntimeException` com propriedade `pokemonName: String` e mensagem `"Pokemon '$pokemonName' not found"`
  - Não adicionar dependências de framework — a exceção deve ser pura de domínio
  - _Requirements: 1.1, 2.1_

- [x] 2. Adicionar tratamento de 404 no `PokeApiService`
  - [x] 2.1 Modificar `fetchFromApi` em `PokeApiService.kt` para usar `onErrorMap`
    - Adicionar import de `WebClientResponseException` e `PokemonNotFoundException`
    - Inserir operador `.onErrorMap(WebClientResponseException::class.java)` na cadeia reativa de `fetchFromApi`, após `.retrieve().bodyToMono(...)`
    - Dentro do lambda: se `ex.statusCode.value() == 404`, lançar `PokemonNotFoundException(pokemonName)`; caso contrário, relançar `ex` sem modificação
    - Garantir que a lógica de `.map { body -> ... }` e `.doOnSuccess { ... }` existentes não seja alterada
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ]* 2.2 Escrever property test — Propriedade 1: 404 lança PokemonNotFoundException com nome correto
    - **Propriedade 1: 404 da PokeAPI lança PokemonNotFoundException com o nome correto**
    - **Validates: Requirements 1.1, 4.2**
    - Adicionar dependências Kotest em `build.gradle.kts`: `io.kotest:kotest-runner-junit5:5.8.1` e `io.kotest:kotest-property:5.8.1`
    - Atualizar `PokeApiServiceTest.kt` com test usando `Arb.string(minSize = 1, maxSize = 50)` para gerar nomes aleatórios
    - Para cada nome gerado: enfileirar resposta 404 no `MockWebServer`, chamar `service.fetchPokemon(name)`, verificar com `StepVerifier` que a exceção é `PokemonNotFoundException` com `pokemonName == name`
    - Mínimo de 100 iterações (`checkAll(iterations = 100, ...)`), tag de referência no comentário: `// Feature: pokemon-not-found-validation, Property 1: 404 da PokeAPI lança PokemonNotFoundException com o nome correto`

  - [ ]* 2.3 Escrever property test — Propriedade 2: erros não-404 são propagados sem alteração
    - **Propriedade 2: Erros não-404 do WebClient são propagados sem alteração**
    - **Validates: Requirements 1.2, 3.1, 3.3, 4.3**
    - Adicionar ao `PokeApiServiceTest.kt` usando `Arb.int(400..599).filter { it != 404 }` para gerar status codes aleatórios
    - Para cada status code gerado: enfileirar resposta no `MockWebServer`, verificar com `StepVerifier` que a exceção é `WebClientResponseException` com o status code original (não convertida para `PokemonNotFoundException`)
    - Mínimo de 100 iterações, tag de referência: `// Feature: pokemon-not-found-validation, Property 2: Erros não-404 do WebClient são propagados sem alteração`

  - [ ]* 2.4 Escrever property test — Propriedade 3: resposta 200 preserva todos os campos
    - **Propriedade 3: Resposta 200 da PokeAPI é desserializada preservando todos os campos**
    - **Validates: Requirements 1.3, 4.1**
    - Adicionar ao `PokeApiServiceTest.kt` usando `Arb.bind` com `Arb.string(minSize = 1)` para nome e `Arb.list(Arb.string(minSize = 1), range = 0..10)` para abilities e moves
    - Para cada combinação gerada: construir JSON correspondente, enfileirar com status 200 no `MockWebServer`, verificar que o `Pokemon` retornado tem `name`, `abilities` e `moves` iguais aos valores gerados
    - Mínimo de 100 iterações, tag de referência: `// Feature: pokemon-not-found-validation, Property 3: Resposta 200 da PokeAPI é desserializada preservando todos os campos`

- [x] 3. Checkpoint — verificar testes do `PokeApiService`
  - Garantir que todos os testes passam, verificar que testes existentes não foram quebrados. Perguntar ao usuário se houver dúvidas.

- [x] 4. Atualizar `PokemonController` para retornar `ResponseEntity` tipado
  - [x] 4.1 Modificar o endpoint `getPokemon` em `PokemonController.kt`
    - Alterar o tipo de retorno de `Mono<Map<String, Any>>` para `Mono<ResponseEntity<Map<String, Any>>>`
    - Substituir o bloco `.switchIfEmpty(...).onErrorResume(ResponseStatusException::class.java)` por:
      - `.map { pokemon -> ResponseEntity.ok(mapOf("data" to pokemon as Any)) }`
      - `.onErrorResume(PokemonNotFoundException::class.java) { ex -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Pokemon '${ex.pokemonName}' not found" as Any))) }`
    - Adicionar import de `PokemonNotFoundException`, `ResponseEntity` e `HttpStatus`
    - Remover import de `ResponseStatusException` se não for mais usado
    - Não modificar o endpoint `/all`
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ]* 4.2 Escrever property test — Propriedade 4: PokemonNotFoundException resulta em HTTP 404 com nome correto
    - **Propriedade 4: Controller mapeia PokemonNotFoundException para HTTP 404 com nome correto**
    - **Validates: Requirements 2.1, 4.4**
    - Criar `src/test/kotlin/com/example/demopokemon/unit/PokemonControllerTest.kt`
    - Usar `WebTestClient` com mock de `PokeApiService` via `@ExtendWith(MockitoExtension::class)` e `WebTestClient.bindToController(...)`
    - Usar `Arb.string(minSize = 1, maxSize = 50)` para gerar nomes aleatórios; para cada nome: configurar mock para lançar `PokemonNotFoundException(name)`, fazer request GET `/pokemon/{name}`, verificar HTTP 404 e que o campo `error` contém exatamente `"Pokemon '$name' not found"`
    - Mínimo de 100 iterações, tag: `// Feature: pokemon-not-found-validation, Property 4: Controller mapeia PokemonNotFoundException para HTTP 404 com nome correto`

  - [ ]* 4.3 Escrever property test — Propriedade 5: sucesso retorna HTTP 200 com Pokemon no campo "data"
    - **Propriedade 5: Controller retorna HTTP 200 com Pokemon no campo "data"**
    - **Validates: Requirements 2.3, 4.5**
    - Adicionar ao `PokemonControllerTest.kt` usando `Arb.bind` com `Arb.string(minSize = 1)` e `Arb.list(Arb.string(minSize = 1), range = 0..10)` para gerar objetos `Pokemon`
    - Para cada `Pokemon` gerado: configurar mock do serviço para retornar `Mono.just(pokemon)`, verificar HTTP 200 e que o campo `data` contém os mesmos `name`, `abilities` e `moves`
    - Mínimo de 100 iterações, tag: `// Feature: pokemon-not-found-validation, Property 5: Controller retorna HTTP 200 com Pokemon no campo "data"`

  - [ ]* 4.4 Escrever teste de exemplo — HTTP 500 para exceções não tratadas
    - Adicionar ao `PokemonControllerTest.kt` um `@Test` convencional (JUnit 5, não property test)
    - Configurar mock do serviço para lançar `RuntimeException("Erro interno")`
    - Verificar com `WebTestClient` que o response é HTTP 500
    - _Requirements: 2.2_

- [x] 5. Checkpoint final — garantir que todos os testes passam
  - Garantir que todos os testes passam, incluindo testes existentes de integração e contrato. Perguntar ao usuário se houver dúvidas.

## Notas

- Tasks marcadas com `*` são opcionais e podem ser puladas para entrega mais rápida de MVP
- As dependências Kotest devem ser adicionadas em `build.gradle.kts` antes de executar os property tests (task 2.2)
- O `MockWebServer` já está disponível nas dependências do projeto — reutilizar o padrão estabelecido em `PokeApiServiceTest.kt`
- Os testes de integração existentes (`KafkaIntegrationTest`, `PokeApiIntegrationTest`) não precisam ser modificados
- O endpoint `/pokemon/all` não é afetado por nenhuma das mudanças
- A ordem das tasks respeita as dependências: exceção de domínio → serviço → controller (as camadas externas dependem das internas)

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["2.2", "2.3", "2.4"] },
    { "id": 3, "tasks": ["4.1"] },
    { "id": 4, "tasks": ["4.2", "4.3", "4.4"] }
  ]
}
```
