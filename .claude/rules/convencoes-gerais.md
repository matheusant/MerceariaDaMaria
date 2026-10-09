# Convenções gerais — vale em toda sessão

- **Dinheiro é `Long` em centavos**, com sufixo `Centavos` no nome (`precoCentavos`,
  `saldoCentavos`, `totalCentavos`). `Double`, `Float` e `BigDecimal` para valor monetário são
  proibidos — `0.1 + 0.2 != 0.3`. Formatação só na borda da UI, via `formatarBrl`.
- **Direção de dependência:** `ui → domain ← data`; `core` não depende de ninguém.
  `import com.google.firebase.*` só existe em `data/` e `di/`.
- **Domínio e código em português** (`Produto`, `lancarDebito`, `saldoCentavos`). Toda string
  visível ao usuário vai para `res/values/strings.xml` — nada de texto hardcoded em composable.
- **Toda chamada de rede dentro de `withTimeout(10_000)`.** Offline, o `Task` de uma escrita do
  Firestore nunca completa e o `await()` pendura para sempre (`SPEC.md §F12`).
- **Nenhuma exceção crua na UI.** Erro do Firebase é mapeado para uma das strings definidas na
  SPEC. `e.message` nunca é exibido ao usuário.
- **Toda dependência entra primeiro em `gradle/libs.versions.toml`** e é referenciada por alias.
  Nenhuma dependência fora de `SPEC.md §7` sem aprovação humana.
- **TDD:** teste primeiro, rodar e ver falhar (RED), depois implementar o mínimo (GREEN).
- **Não editar** `SPEC.md` nem `CLAUDE.md` durante a execução de uma task. No `PLAN.md`, só
  marcar checkbox.
- **Não implementar nada fora do `PLAN.md`.** Bug achado fora do escopo da task: registre e siga.
- **Se travar por 2 tentativas, pare e sinalize.** Não invente solução alternativa nem mude o
  escopo por conta própria.

Gate mínimo: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` → `BUILD SUCCESSFUL`.
