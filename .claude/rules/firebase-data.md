---
paths:
  - "app/src/main/java/com/example/merceariadamaria/data/**/*.kt"
  - "app/src/main/java/com/example/merceariadamaria/domain/**/*.kt"
  - "app/src/main/java/com/example/merceariadamaria/di/**/*.kt"
---
# Regras da camada de dados

- **`domain/` é puro.** Nenhum `Timestamp`, `DocumentSnapshot` ou `FirebaseUser` em assinatura de
  modelo ou de interface de repositório. `Timestamp` vira `Long` em millis no mapper.
- **Mapper de leitura nunca lança.** Campo ausente, `null` ou de tipo errado → retorna `null` e
  emite `Log.w`; o item é descartado da lista. Um documento malformado não derruba a tela
  (`SPEC.md B13`).
- **Todo `await()` dentro de `withTimeout(10_000)`.** Sem exceção (`SPEC.md §F12`).
- **Escrita retorna `Result<T>`; leitura contínua retorna `Flow<List<T>>`** via snapshot listener.
- **Exceção mapeada na fronteira do repositório**, usando a tabela de `SPEC.md §F1`. Nada de
  exceção crua atravessando para o ViewModel.
- **Transação (`runTransaction`) — a ordem importa:**
  1. Todas as leituras **antes** de todas as escritas. O Firestore rejeita o contrário.
  2. Validações **dentro** do bloco: ele reexecuta em contenção, e é a revalidação que impede
     estoque negativo com duas vendas simultâneas (`B19`).
  3. `add()` não existe em transação — pré-gere o id com `collection("...").document()` e use
     `transaction.set(ref, dados)`.
  4. Falha em um item aborta tudo: nenhum estoque baixa, saldo e lançamento intocados.
  5. Transação **não funciona offline**: falhe com `"Sem conexão. O lançamento precisa de
     internet."` — não trate como sucesso-diferido (`B9`).
- **Soft delete sempre.** Produto sai do catálogo com `ativo = false`; nunca `delete()` físico.
- **`saldoCentavos` só muda dentro da mesma transação do lançamento** (`SPEC.md §8.3`).
- **`isPersistenceEnabled` é configurado exatamente uma vez**, antes de qualquer uso do Firestore.
  Duas vezes lança `IllegalStateException`.
- **Todo repositório tem um fake** em `app/src/test/.../fake/`, escrito à mão. Sem mockk.
