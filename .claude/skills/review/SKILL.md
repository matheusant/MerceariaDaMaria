---
name: review
description: >
  Invoca o agent code-reviewer contra o SPEC.md e o PLAN.md, classifica os achados em
  BLOQUEANTE, IMPORTANTE e SUGESTÃO, e chama o firestore-rules-auditor quando as regras do
  Firestore foram alteradas. Use sempre que houver código novo a validar antes de marcar uma
  task ou avançar de fase. Exemplos de trigger: "revise", "code review", "está pronto para
  avançar", "verifique a aderência à SPEC", "terminei a task".
---

# Review

Argumento: `$ARGUMENTS` — o que revisar (ex.: `Task 4.3`, `SignUpViewModel`, `o diff atual`).
Sem argumento, revise o diff de trabalho atual (`git diff` + arquivos não rastreados).

## Quando usar

- Ao terminar uma task, **antes** de marcar checkbox no `PLAN.md`.
- Antes de fechar o gate de uma fase.
- Quando algo passa nos testes mas parece errado.

## Quando NÃO usar

- **Antes de existir código** — não há o que revisar.
- **Para consertar** o que a revisão achou: o `code-reviewer` é somente leitura (um hook bloqueia
  Write/Edit/Bash nele). Quem corrige é o agent de implementação.
- **Para revisar o `firestore.rules` isoladamente** — chame `@firestore-rules-auditor` direto.

## Passo a passo

### 1. Delimite o escopo
Rode `git diff` e `git status --porcelain` e liste os arquivos mudados. Identifique a task do
`PLAN.md` correspondente e as features da SPEC que ela cobre.

### 2. Dispare o revisor em contexto isolado
Chame `@code-reviewer` passando: os arquivos mudados, o texto integral da task e as seções da
SPEC. Contexto isolado é o ponto — um revisor que participou da implementação tende a aprovar as
próprias decisões.

### 3. Dispare o auditor de regras, se aplicável
Se `firestore.rules` ou `firestore.indexes.json` estão no diff, chame também
`@firestore-rules-auditor`. Ele analisa escalada de privilégio, que o revisor de código não olha.

### 4. Verifique o que é mecânico
Confirme, item por item:
- Todo `Teste crítico` da task tem teste real e verde.
- `Output` da task entregue com a assinatura exata.
- Nenhum arquivo de outra task no diff (tabela anti-conflito no fim do `PLAN.md`).
- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` → `BUILD SUCCESSFUL`.

### 5. Consolide
Junte os achados dos dois agents, sem duplicar. Ordene por severidade.

## Classificação

- **BLOQUEANTE** — impede o funcionamento, viola regra de `CLAUDE.md §6`/`§8`, ou deixa um
  `Teste crítico` sem cobertura. Corrigir antes de marcar a task.
- **IMPORTANTE** — corrigir antes da entrega; não impede avançar a task.
- **SUGESTÃO** — próximo ciclo.

Se não houver nada, **diga explicitamente que não há BLOQUEANTE** — silêncio não é aprovação.

## Exemplo

**Input:** `/review Task 4.3`

**Output esperado:**
```
Task 4.3 — FiadoRepository (transações)
Arquivos: data/firebase/FiadoRepositoryFirebase.kt, test/fake/FakeFiadoRepository.kt,
          test/data/FiadoRepositoryTest.kt

BLOQUEANTE
  FiadoRepositoryFirebase.kt:88 — leitura de produtos/{id} DEPOIS de transaction.update()
    do saldo. O Firestore rejeita leitura após escrita numa transação (SPEC.md §F6, passo 1-3).
  FiadoRepositoryTest.kt — falta o teste "51 itens distintos → Máximo de 50 produtos por
    lançamento" (Teste crítico 5 da task, SPEC.md B18).

IMPORTANTE
  FiadoRepositoryFirebase.kt:142 — lancarCredito sem withTimeout no await() (CLAUDE.md §6).

SUGESTÃO
  Extrair a mensagem "Estoque insuficiente para %s (disponível: %d)" para uma constante.

Gate: BUILD SUCCESSFUL (mas com 1 Teste crítico sem cobertura → não marque a task ainda)
```
