---
name: orquestrar
description: >
  Executa uma fase inteira do PLAN.md como orquestrador: identifica tasks paralelas e
  sequenciais, delega cada uma ao agent certo, dispara o code-reviewer ao fim de cada task e
  valida o gate da fase antes de avançar. Use sempre que o pedido for para rodar, executar ou
  orquestrar uma fase, uma wave ou um sprint do plano. Exemplos de trigger: "execute a Fase 3",
  "rode a Wave 1", "orquestre o Sprint 1", "toque o plano até a Fase 6", "quais tasks posso
  rodar em paralelo agora".
---

# Orquestrar uma fase do PLAN.md

Argumento: `$ARGUMENTS` — a fase ou wave a executar (ex.: `Fase 3`, `Wave 1`, `Sprint 1`).
Sem argumento, execute a próxima fase com gate ainda não atingido, segundo a seção
`## Próxima sessão` do `PLAN.md`.

## Quando usar

- Rodar uma fase completa do `PLAN.md`, com várias tasks.
- Descobrir o que pode rodar em paralelo agora e quantos agents são necessários.

## Quando NÃO usar

- **Uma task só** — use `/implementar <task>`, que é mais direto.
- **Só revisar** código já escrito — use `/review`.
- **Fechar a sessão** — use `/entrega`.
- Trabalho que **não está no `PLAN.md`**. Se o pedido não corresponde a nenhuma task, pare e
  diga isso: implementar fora do plano é proibido (`CLAUDE.md §8.7`).

## Passo a passo

### 1. Carregue o contexto
Leia `SPEC.md`, `CLAUDE.md` e `PLAN.md`. Confirme o baseline: rode
`./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` e verifique `BUILD SUCCESSFUL`.
Se o build já está vermelho antes de começar, pare e conserte isso primeiro.

### 2. Verifique as dependências da fase
Leia a linha `> Dependências:` da fase. Confirme que o gate das fases anteriores passou.
**Gate de fase é inegociável** — não comece uma fase com a anterior vermelha.

Se a fase incluir a Task 0.1, confirme antes que `app/google-services.json` existe. Sem ele o
build falha e é **ação humana** (`CLAUDE.md §3`): pare e avise, não contorne.

### 3. Monte o plano de execução
Da linha `> Paralelismo:` da fase, liste as tasks paralelas. Confira contra a tabela
anti-conflito no fim do `PLAN.md`: **duas tasks paralelas nunca tocam o mesmo arquivo.** Se
tocarem, rode em sequência e diga por quê.

Mapeie cada task ao agent do campo `- Agent:`: `@build-engineer`, `@core-kotlin`,
`@firebase-data`, `@compose-ui`, `@firestore-rules-auditor`.

### 4. Execute cada task com TDD
Para cada task, passe ao agent: o texto integral da task (`Input`, `Output`, `Testes críticos`) e
as seções da SPEC citadas. O agent tem **zero contexto** desta conversa.

Exija a ordem: escrever os testes → **rodar e confirmar que falham (RED)** → implementar o mínimo
(GREEN) → marcar o checkbox no `PLAN.md`.

### 5. Revise antes de avançar (builder-validator)
Ao concluir cada task, dispare `@code-reviewer` em contexto isolado. Se houver **BLOQUEANTE**,
corrija antes de seguir. IMPORTANTE pode ir para a próxima task; SUGESTÃO fica registrada.
Se a task mexeu em `firestore.rules`, dispare também `@firestore-rules-auditor`.

### 6. Feche o gate da fase
Rode o comando da linha `> Gate:` da fase. Só avance se passar. Relate: comando, resultado
literal, tasks concluídas e checkboxes marcados.

## Restrições

- Não edite `SPEC.md` nem `CLAUDE.md`. No `PLAN.md`, só marque checkbox.
- Não implemente nada fora do `PLAN.md`.
- Uma task, um agent. Nunca dois agents editando o mesmo arquivo ao mesmo tempo.
- **Se uma task travar por 2 tentativas, pare e sinalize** — não invente solução alternativa.

## Exemplo

**Input:** `/orquestrar Fase 1`

**Output esperado:**
```
Fase 1 — Núcleo puro (core/)
Dependências: Fase 0 — gate verificado: BUILD SUCCESSFUL
Paralelismo: 4 tasks, arquivos disjuntos → 4 agents @core-kotlin em paralelo

  1.1 Formatadores.kt      → RED (3 testes falhando) → GREEN → review: sem BLOQUEANTE ✓
  1.2 AuthConstants.kt     → RED (3) → GREEN → review: sem BLOQUEANTE ✓
  1.3 Validadores.kt       → RED (8) → GREEN → review: 1 IMPORTANTE registrado ✓
  1.4 Descricoes.kt        → RED (3) → GREEN → review: sem BLOQUEANTE ✓

Gate: ./gradlew :app:testDebugUnitTest --tests "com.example.merceariadamaria.core.*"
      → BUILD SUCCESSFUL, 17 testes
Checkboxes marcados no PLAN.md: 1.1, 1.2, 1.3, 1.4
Próxima: Fase 2 — Task 2.1 (@firebase-data, task única)
```
