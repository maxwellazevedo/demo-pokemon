# Design Técnico: pokemon-not-found-validation

## Visão Geral

Esta feature corrige o comportamento do sistema `demo-pokemon` quando um Pokémon não existe na PokeAPI. Atualmente, uma resposta HTTP 404 da PokeAPI resulta em uma `WebClientResponseException.NotFound` não capturada, que pode vazar como HTTP 500 para o cliente.

O objetivo é introduzir tratamento explícito desse erro na camada de aplicação, comunicando-o por meio de uma exceção de domínio (`PokemonNotFoundException`), e tratar essa exceção no adaptador web para retornar HTTP 404 com mensagem descritiva. Todos os outros erros do WebClient (5xx, timeout, etc.) devem ser propagados sem modificação.

A solução segue os princípios da arquitetura hexagonal já presente no projeto: a lógica de detecção do erro pertence ao serviço de aplicação (`PokeApiService`), e o mapeamento para resposta HTTP pertence ao adaptador de entrada (`PokemonController`).

---

## Arquitetura

O projeto já segue arquitetura hexagonal com as seguintes camadas:

```
┌─────────────────────────────────────────────┐
│               Adaptador de Entrada           │
│           PokemonController (HTTP)           │
└──────────────────┬──────────────────────────┘
                   │ chama
┌──────────────────▼──────────────────────────┐
│           Camada de Aplicação                │
│              PokeApiService                  │
│  (orquestra cache, PokeAPI, Kafka)           │
└──────┬──────────────────────┬───────────────┘
       │ usa porta             │ chama API externa
┌──────▼──────────┐    ┌──────▼──────────────┐
│ PokemonRepository│    │    PokeAPI (HTTP)    │
│      Port        │    │   via WebClient      │
└──────────────────┘    └─────────────────────┘
```

### Fluxo de tratamento de erros (novo comportamento)

```
PokemonController.getPokemon(name)
       │
       ▼
PokeApiService.fetchPokemon(name)
       │
       ├─ cache hit ──────────────────────────► retorna Pokemon
       │
       └─ cache miss ──► fetchFromApi(name)
                               │
                               ├─ 2xx ────────► processa JSON ──► retorna Pokemon
                               │
                               ├─ 404 ────────► lança PokemonNotFoundException(name)
                               │
                               └─ outro erro ──► propaga exceção original
                                                        │
                               ◄────────────────────────┘
                        PokemonController.onErrorMap
                               │
                               ├─ PokemonNotFoundException ──► HTTP 404 + { "error": "Pokemon '{name}' not found" }
                               │
                               └─ qualquer outra exceção ───► HTTP 500 (não mascarado)
```

---

## Componentes e Interfaces

### 1. `PokemonNotFoundException` (novo — domínio)

Exceção de domínio sem dependência de framework. Criada no pacote `domain` para manter a separação de camadas.

```
package: com.example.demopokemon.domain.exception
```

```kotlin
class PokemonNotFoundException(val pokemonName: String) :
    RuntimeException("Pokemon '$pokemonName' not found")
```

**Decisão de design:** Usar `RuntimeException` sem checked exception para compatibilidade com a API reativa (lambdas e `map`/`flatMap` do Reactor não aceitam checked exceptions sem wrapper). Armazenar o `pokemonName` como propriedade para permitir que o controller construa a mensagem com o nome exato.

---

### 2. `PokeApiService` (modificado)

Adicionar tratamento de `WebClientResponseException` no método `fetchFromApi`, utilizando o operador `onErrorMap` do Reactor:

```kotlin
private fun fetchFromApi(pokemonName: String): Mono<Pokemon> =
    webClientBuilder.build()
        .get()
        .uri("$pokeApiBaseUrl$pokemonName")
        .retrieve()
        .bodyToMono(String::class.java)
        .onErrorMap(WebClientResponseException::class.java) { ex ->
            if (ex.statusCode.value() == 404) {
                PokemonNotFoundException(pokemonName)
            } else {
                ex  // propaga original para status != 404
            }
        }
        .map { body ->
            // desserialização existente
        }
        .doOnSuccess { ... }
```

**Decisão de design:** `onErrorMap` é o operador semântico correto do Reactor para transformar exceções em pipeline reativo — preserva a semântica do stream sem quebrar a cadeia. Alternativas como `onErrorResume` forçariam retornar um `Mono` e introduziriam complexidade desnecessária.

---

### 3. `PokemonController` (modificado)

Substituir o tratamento atual baseado em `switchIfEmpty` + `onErrorResume(ResponseStatusException)` por tratamento explícito de `PokemonNotFoundException` via `onErrorMap` e `onErrorResume`:

```kotlin
@GetMapping("/{name}")
fun getPokemon(@PathVariable name: String): Mono<ResponseEntity<Map<String, Any>>> =
    pokeApiService.fetchPokemon(name)
        .map { pokemon -> ResponseEntity.ok(mapOf("data" to pokemon as Any)) }
        .onErrorResume(PokemonNotFoundException::class.java) { ex ->
            val body = mapOf("error" to "Pokemon '${ex.pokemonName}' not found" as Any)
            Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body))
        }
```

**Decisão de design:** Usar `ResponseEntity<Map<String, Any>>` como tipo de retorno ao invés de `Map<String, Any>` para permitir controle explícito do status HTTP sem depender de `ResponseStatusException`. Isso elimina a ambiguidade atual onde `onErrorResume(ResponseStatusException)` retorna um `Map` com o campo `error` mas com status HTTP 200.

**Sobre erros não-`PokemonNotFoundException`:** O Spring WebFlux já converte exceções não tratadas em HTTP 500 automaticamente. Não adicionar `onErrorResume` genérico — deixar o framework propagar.

---

## Modelos de Dados

### `PokemonNotFoundException`

| Campo         | Tipo     | Descrição                              |
|---------------|----------|----------------------------------------|
| `pokemonName` | `String` | Nome do Pokémon não encontrado         |
| `message`     | `String` | `"Pokemon '$pokemonName' not found"`   |

### `ErrorResponse` (resposta HTTP)

Representação JSON retornada ao cliente no caso de erro 404:

```json
{
  "error": "Pokemon 'missingno' not found"
}
```

### `SuccessResponse` (sem alteração)

```json
{
  "data": {
    "id": null,
    "name": "pikachu",
    "abilities": ["static", "lightning-rod"],
    "moves": ["thunder-shock", "growl"]
  }
}
```

---

## Propriedades de Corretude

*Uma propriedade é uma característica ou comportamento que deve se verificar para todas as execuções válidas de um sistema — essencialmente, uma declaração formal sobre o que o sistema deve fazer. Propriedades servem como ponte entre especificações legíveis por humanos e garantias de corretude verificáveis por máquina.*

### Propriedade 1: 404 da PokeAPI lança PokemonNotFoundException com o nome correto

*Para qualquer* nome de Pokémon, quando o WebClient lança `WebClientResponseException` com status 404, o `PokeApiService` deve lançar `PokemonNotFoundException` cuja propriedade `pokemonName` é igual ao nome solicitado.

**Validates: Requirements 1.1, 4.2**

---

### Propriedade 2: Erros não-404 do WebClient são propagados sem alteração

*Para qualquer* status code de erro diferente de 404 (ex: 500, 502, 503), quando o WebClient lança `WebClientResponseException`, o `PokeApiService` deve propagar exatamente a mesma exceção original — sem conversão de tipo e sem perda de informação de status.

**Validates: Requirements 1.2, 3.1, 3.3, 4.3**

---

### Propriedade 3: Resposta 200 da PokeAPI é desserializada preservando todos os campos

*Para qualquer* Pokémon com nome, lista de abilities e lista de moves válidos, quando o WebClient retorna status 200 com o JSON correspondente, o objeto `Pokemon` retornado pelo `PokeApiService` deve ter `name`, `abilities` e `moves` iguais aos valores originais do JSON.

**Validates: Requirements 1.3, 4.1**

---

### Propriedade 4: Controller mapeia PokemonNotFoundException para HTTP 404 com nome correto

*Para qualquer* nome de Pokémon, quando o `PokeApiService` lança `PokemonNotFoundException(pokemonName)`, o `PokemonController` deve retornar HTTP 404 com corpo `{ "error": "Pokemon '{pokemonName}' not found" }` onde `{pokemonName}` é exatamente o nome armazenado na exceção.

**Validates: Requirements 2.1, 4.4**

---

### Propriedade 5: Controller retorna HTTP 200 com Pokemon no campo "data"

*Para qualquer* objeto `Pokemon` válido retornado pelo `PokeApiService`, o `PokemonController` deve retornar HTTP 200 com o corpo `{ "data": <pokemon> }` onde o pokemon serializado contém os mesmos campos do objeto original.

**Validates: Requirements 2.3, 4.5**

---

## Tratamento de Erros

### Matriz de erros

| Cenário                                          | Componente         | Comportamento esperado                                           |
|--------------------------------------------------|--------------------|------------------------------------------------------------------|
| PokeAPI retorna 404                              | `PokeApiService`   | Lança `PokemonNotFoundException(name)`                           |
| PokeAPI retorna 5xx                              | `PokeApiService`   | Propaga `WebClientResponseException` original                    |
| Timeout no WebClient                             | `PokeApiService`   | Propaga exceção de timeout (ex: `WebClientRequestException`)     |
| Serviço lança `PokemonNotFoundException`         | `PokemonController`| HTTP 404 + `{ "error": "Pokemon '{name}' not found" }`           |
| Serviço lança qualquer outra exceção             | `PokemonController`| HTTP 500 (tratamento padrão do Spring WebFlux)                   |
| Serviço resolve com sucesso                      | `PokemonController`| HTTP 200 + `{ "data": <pokemon> }`                               |

### Garantias de não regressão

- O tratamento de 404 é adicionado via `onErrorMap` **apenas** dentro de `fetchFromApi`, que é chamado somente no caso de cache miss. O caminho de cache hit não é afetado.
- A lógica de persistência no repositório e publicação no Kafka não são alteradas.
- O endpoint `/pokemon/all` não é alterado.

---

## Estratégia de Testes

### Abordagem dual

Os testes utilizam duas abordagens complementares:

1. **Testes baseados em exemplos** (`@Test` JUnit 5): cenários específicos e casos de borda
2. **Testes baseados em propriedades** (Kotest Property Testing): propriedades universais com geração aleatória de inputs

### Framework de testes baseados em propriedades

O projeto usa Kotest como biblioteca de property-based testing. Adicionar dependência em `build.gradle.kts`:

```kotlin
testImplementation("io.kotest:kotest-runner-junit5:5.8.1")
testImplementation("io.kotest:kotest-property:5.8.1")
```

Cada property test deve executar no mínimo **100 iterações**. A tag de referência para cada propriedade segue o formato:

```
// Feature: pokemon-not-found-validation, Property {N}: {texto da propriedade}
```

### Testes unitários — `PokeApiServiceTest`

Localização: `src/test/kotlin/com/example/demopokemon/unit/PokeApiServiceTest.kt`

Utiliza `MockWebServer` (já presente no projeto) para simular respostas do WebClient.

| Cenário                                        | Tipo       | Propriedade     |
|------------------------------------------------|------------|-----------------|
| WebClient 200 → Pokemon com campos corretos    | Property   | Propriedade 3   |
| WebClient 404 → PokemonNotFoundException(name) | Property   | Propriedade 1   |
| WebClient 5xx → exceção original propagada     | Property   | Propriedade 2   |
| Timeout → exceção propagada sem alteração      | Example    | Req 3.2         |
| Cache hit → retorna do cache, não chama API    | Example    | (existente)     |

### Testes unitários — `PokemonControllerTest`

Localização: `src/test/kotlin/com/example/demopokemon/unit/PokemonControllerTest.kt` (novo)

Utiliza `WebTestClient` com mock do `PokeApiService` via Mockito.

| Cenário                                              | Tipo       | Propriedade     |
|------------------------------------------------------|------------|-----------------|
| Serviço lança PokemonNotFoundException → HTTP 404    | Property   | Propriedade 4   |
| Serviço resolve com Pokemon → HTTP 200 + "data"      | Property   | Propriedade 5   |
| Serviço lança RuntimeException → HTTP 500            | Example    | Req 2.2         |

### Estratégia de geração de dados (Kotest Arb)

- **Nomes de Pokémon**: `Arb.string(minSize = 1, maxSize = 50)` filtrado para strings não vazias
- **Listas de abilities/moves**: `Arb.list(Arb.string(minSize = 1), range = 0..10)`
- **Status codes de erro (não-404)**: `Arb.int(400..599).filter { it != 404 }`
- **Objetos Pokemon**: `Arb.bind(nameArb, abilitiesArb, movesArb) { n, a, m -> Pokemon(name=n, abilities=a, moves=m) }`

### Testes de integração (existentes, sem alteração)

Os testes de integração em `KafkaIntegrationTest` e `PokeApiIntegrationTest` não precisam ser modificados, pois testam o fluxo de sucesso que permanece inalterado.
