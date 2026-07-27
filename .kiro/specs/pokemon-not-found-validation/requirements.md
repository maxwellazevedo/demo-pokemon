# Documento de Requisitos

## Introdução

Esta feature trata do comportamento do sistema `demo-pokemon` quando um Pokémon solicitado não existe na PokeAPI. Atualmente, uma resposta HTTP 404 da PokeAPI gera uma `WebClientResponseException.NotFound` que não é capturada corretamente, podendo vazar como HTTP 500 para o cliente.

O objetivo é garantir que:
- Erros 404 da PokeAPI sejam convertidos em uma exceção de domínio (`PokemonNotFoundException`) e retornem HTTP 404 com mensagem clara ao cliente.
- Outros erros do WebClient (5xx, timeout, etc.) propaguem como HTTP 500 sem ser engolidos.
- O comportamento seja coberto por testes unitários.

## Glossário

- **PokeApiService**: Serviço de aplicação responsável por orquestrar a busca de Pokémon no cache e na PokeAPI externa.
- **PokemonController**: Adaptador de entrada HTTP que expõe os casos de uso via REST.
- **PokemonNotFoundException**: Exceção de domínio lançada quando um Pokémon não é encontrado na PokeAPI (resposta 404).
- **WebClient**: Cliente HTTP reativo do Spring usado para chamar a PokeAPI.
- **PokeAPI**: API externa que fornece dados de Pokémon.
- **ErrorResponse**: Objeto de resposta JSON com campo `error` retornado ao cliente em caso de falha.

---

## Requisitos

### Requisito 1: Captura de Pokémon não encontrado na PokeAPI

**User Story:** Como desenvolvedor do sistema, quero que erros 404 da PokeAPI sejam convertidos em uma exceção de domínio específica, para que a camada de aplicação comunique claramente ao adaptador web que o Pokémon não existe.

#### Critérios de Aceitação

1. WHEN a PokeAPI retorna status HTTP 404 para um nome de Pokémon, THE PokeApiService SHALL lançar uma `PokemonNotFoundException` contendo o nome do Pokémon solicitado.
2. WHEN a PokeAPI retorna status HTTP diferente de 404 e diferente de 2xx, THE PokeApiService SHALL propagar a exceção original sem convertê-la.
3. WHEN a PokeAPI retorna status 2xx, THE PokeApiService SHALL processar o corpo da resposta normalmente e retornar o objeto `Pokemon`.

---

### Requisito 2: Tratamento de `PokemonNotFoundException` no Controller

**User Story:** Como consumidor da API REST, quero receber HTTP 404 com uma mensagem descritiva quando solicitar um Pokémon inexistente, para que eu possa distinguir claramente esse caso de um erro interno do servidor.

#### Critérios de Aceitação

1. WHEN o `PokeApiService` lança `PokemonNotFoundException`, THE PokemonController SHALL retornar HTTP 404 com corpo `{ "error": "Pokemon '{name}' not found" }`, onde `{name}` é o nome exato solicitado.
2. WHEN o `PokeApiService` lança qualquer exceção que não seja `PokemonNotFoundException`, THE PokemonController SHALL retornar HTTP 500 sem tratar ou mascarar a exceção.
3. THE PokemonController SHALL retornar o objeto `Pokemon` com HTTP 200 quando o serviço resolver com sucesso.

---

### Requisito 3: Não engolimento de outros erros do WebClient

**User Story:** Como operador do sistema, quero que erros 5xx e timeouts da PokeAPI não sejam silenciados, para que possam ser monitorados e alertados corretamente como falhas de infraestrutura.

#### Critérios de Aceitação

1. WHEN a PokeAPI retorna status HTTP 5xx, THE PokeApiService SHALL propagar a `WebClientResponseException` original sem alteração.
2. WHEN ocorre timeout na chamada ao WebClient, THE PokeApiService SHALL propagar a exceção de timeout sem alteração.
3. IF a `WebClientResponseException` possui status diferente de 404, THEN THE PokeApiService SHALL deixar a exceção propagar para a camada de transporte.

---

### Requisito 4: Cobertura de testes unitários

**User Story:** Como desenvolvedor do sistema, quero testes unitários cobrindo os três cenários principais de resposta da PokeAPI, para garantir a corretude dos fluxos de sucesso, Pokémon não encontrado e erro de servidor.

#### Critérios de Aceitação

1. THE PokeApiServiceTest SHALL verificar que, quando o WebClient retorna status 200, o `PokeApiService` retorna um objeto `Pokemon` com os campos `name`, `abilities` e `moves` corretamente preenchidos.
2. THE PokeApiServiceTest SHALL verificar que, quando o WebClient lança `WebClientResponseException` com status 404, o `PokeApiService` lança `PokemonNotFoundException` com o nome do Pokémon.
3. THE PokeApiServiceTest SHALL verificar que, quando o WebClient lança `WebClientResponseException` com status 500, o `PokeApiService` propaga a exceção original sem alteração.
4. THE PokemonControllerTest SHALL verificar que, quando o serviço lança `PokemonNotFoundException`, o endpoint retorna HTTP 404 com corpo `{ "error": "Pokemon '{name}' not found" }`.
5. THE PokemonControllerTest SHALL verificar que, quando o serviço resolve com sucesso, o endpoint retorna HTTP 200 com o objeto `Pokemon` no campo `data`.
