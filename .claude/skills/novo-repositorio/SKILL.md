---
name: novo-repositorio
description: >
  Cria um repositório Firestore neste projeto: interface no domínio, implementação com timeout e
  mapeamento de exceção, mappers que nunca lançam, transação com leituras antes de escritas, fake
  escrito à mão e testes. Use sempre que o pedido envolver acesso a dado, coleção do Firestore,
  mapper, transação ou fake de repositório. Exemplos de trigger: "criar o repositório de
  produtos", "acessar a coleção fiados", "mapper do Firestore", "transação de lançamento",
  "preciso de um fake para testar".
---

# Novo repositório Firestore

Argumento: `$ARGUMENTS` — a entidade (ex.: `Produto`, `Fiado`).

## Quando usar

- Criar acesso a uma das três coleções: `usuarios`, `produtos`, `fiados`.
- Adicionar operação a um repositório existente.

## Quando NÃO usar

- **Coleção nova** que não está no `SPEC.md §5` — pare e sinalize. São exatamente três coleções.
- **Lógica pura** (formatação, validação) — vai em `core/`, com o `core-kotlin`.
- **Tela ou estado de tela** — use `/nova-tela`.
- **Regra de segurança** — é `firestore.rules`; audite com `@firestore-rules-auditor`.

## Passo a passo

### 1. Interface no domínio
Em `domain/repository/`. **Nenhum tipo do Firebase na assinatura** — nem `Timestamp`, nem
`DocumentSnapshot`, nem `FirebaseUser`. Escrita retorna `Result<T>`; leitura contínua retorna
`Flow<List<T>>`.

```kotlin
interface ProdutoRepository {
    fun observarAtivos(): Flow<List<Produto>>
    suspend fun criar(nome: String, precoCentavos: Long, quantidade: Int): Result<String>
    suspend fun definirAtivo(id: String, ativo: Boolean): Result<Unit>
}
```

### 2. Mapper que nunca lança
Em `data/mapper/`. Campo ausente, `null` ou de tipo errado → retorna `null` e emite `Log.w`; o
item é descartado da lista. Um documento malformado não pode derrubar a tela (`SPEC.md B13`).
`Timestamp` vira `Long` em millis. No `toMap()`, `nomeNormalizado` é sempre `normalizar(nome)`.

### 3. Implementação em `data/firebase/`
- **Todo `await()` dentro de `withTimeout(10_000)`.** Offline, o `Task` de uma escrita nunca
  completa e o `await()` pendura para sempre (`SPEC.md §F12`).
- **Mapeie toda exceção** para as mensagens da tabela de `SPEC.md §F1`. Nada de exceção crua nem
  `e.message` atravessando a fronteira.
- Leitura contínua com `addSnapshotListener` dentro de `callbackFlow { ... awaitClose { reg.remove() } }`.
- Exclusão é **soft delete** (`ativo = false`), nunca `delete()` físico.

### 4. Transação — a ordem importa (`SPEC.md §F6`)
```
1. TODAS as leituras primeiro. O Firestore rejeita ler depois de escrever.
2. Validações DENTRO do bloco: runTransaction reexecuta em contenção, e é a revalidação
   que impede estoque negativo com duas vendas simultâneas (B19).
3. add() não existe em transação: pré-gere o id com collection("...").document() e use
   transaction.set(ref, dados).
4. Falha em um item aborta tudo — nada de escrita parcial.
5. Transação NÃO funciona offline: falhe com "Sem conexão. O lançamento precisa de
   internet." Não trate como sucesso-diferido (B9).
```

### 5. Fake escrito à mão
Em `app/src/test/.../fake/Fake<X>Repository.kt`. Sem mockk. O fake **registra as chamadas
recebidas**, para o teste poder afirmar "o repositório não foi chamado" — que é metade dos casos
de erro da SPEC. Exponha gatilhos de falha (`falharProximaEscrita`, `estoqueDisponivel`).

### 6. Teste primeiro (RED → GREEN)
Cubra: caminho feliz, cada caso de erro com a mensagem exata, e — nas transações — a prova de
atomicidade: **nenhum** dos documentos envolvidos muda quando um item falha.

### 7. Verifique
```bash
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```
Depois `/review`.

## Exemplo

**Input:** `/novo-repositorio Fiado`

**Output esperado:**
```
domain/repository/FiadoRepository.kt          (5 métodos, zero tipo do Firebase)
data/firebase/FiadoRepositoryFirebase.kt      (runTransaction: 3 leituras → 6 validações → 3 escritas)
data/mapper/Mappers.kt                        (+toContaFiadoOrNull, +toLancamentoOrNull)
test/.../fake/FakeFiadoRepository.kt          (registra chamadas + gatilhos de falha)
test/.../data/FiadoRepositoryTest.kt          (11 testes: RED 11/11 → GREEN 11/11)

Prova de atomicidade: estoque insuficiente em 1 de 2 itens → 4 documentos verificados,
nenhum alterado.
BUILD SUCCESSFUL
```
