---
paths:
  - "app/src/test/**/*.kt"
  - "app/src/androidTest/**/*.kt"
---
# Regras de teste

- **Teste primeiro.** Escreva, rode e **confirme que falha** (RED) antes de implementar. Um teste
  que nunca falhou não prova nada.
- **Asserção com valor literal, não recalculado.** `assertEquals("R$ 5,49", formatarBrl(549))` —
  nunca `assertEquals(formatar(549), formatarBrl(549))`. O valor esperado vem da SPEC.
- **Fakes escritos à mão** em `app/src/test/.../fake/`. Sem mockk, sem Mockito, sem Firebase real
  em teste unitário. O fake registra as chamadas recebidas para o teste poder afirmar
  "o repositório **não** foi chamado".
- **`runTest` + `TestDispatcher`** para ViewModel e repositório. Nada de `Thread.sleep` nem de
  espera por tempo real.
- **Teste de caminho de erro é obrigatório**, não opcional: cada task do `PLAN.md` exige pelo
  menos um caso de erro tratado. Onde a SPEC define mensagem exata, o teste compara a string.
- **Onde testar o quê:**
  - `core/` → JUnit puro. É rápido e não precisa de device: prefira aqui.
  - ViewModel → `app/src/test/` com fake.
  - Fluxo de tela → `app/src/androidTest/` (Compose). Só para o que **não** cabe num teste de
    ViewModel: navegação, preservação de estado ao voltar, back stack depois do logout.
  - `firestore.rules` → Rules Playground do Console, resultado registrado em
    `docs/verificacao-regras.md`. Não há suíte automatizada nesta entrega.
- **Não escreva teste de UI para lógica que cabe em teste de ViewModel** — é mais lento, exige
  device e falha por motivos que não são o que você quer testar.
- `ExampleUnitTest` e `ExampleInstrumentedTest` são placeholders e devem ser removidos na
  Task 11.3 (`SPEC.md §9.5`). Não escreva teste novo dentro deles.
