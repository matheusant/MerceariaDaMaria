---
name: implementar
description: >
  Executa uma única task do PLAN.md em TDD estrito — escreve os testes críticos primeiro,
  confirma que falham, implementa o mínimo para passar, roda o gate e dispara o code-reviewer.
  Use sempre que o pedido for para implementar, fazer ou codar uma task específica do plano.
  Exemplos de trigger: "implemente a Task 4.3", "faça a 1.1", "codifique o SignUpViewModel",
  "comece a task do catálogo".
---

# Implementar uma task do PLAN.md

Argumento: `$ARGUMENTS` — o identificador da task (ex.: `4.3`, `Task 6.1`) ou o nome do
entregável (ex.: `SignUpViewModel`).

## Quando usar

- Executar **uma** task do `PLAN.md`, de ponta a ponta, com teste primeiro.

## Quando NÃO usar

- **Uma fase inteira** com várias tasks — use `/orquestrar`.
- **Só revisar** — use `/review`.
- **Fechar a sessão** — use `/entrega`.
- Pedido que **não corresponde a nenhuma task** do `PLAN.md`: pare e diga. Implementar fora do
  plano é proibido (`CLAUDE.md §8.7`). Se o trabalho é legítimo mas não está no plano, o caminho
  é atualizar o plano com aprovação humana — não codar por fora.

## Passo a passo

### 1. Localize a task e leia tudo
No `PLAN.md`, encontre a task e leia `Agent`, `Input`, `Output` e `Testes críticos`. Abra as
seções do `SPEC.md` citadas no `Input` — é de lá que vêm as strings exatas, as assinaturas e os
valores esperados. Leia `CLAUDE.md`.

Confirme que o `Input` está pronto. Se depende de task de fase anterior não concluída, pare e
diga qual falta.

### 2. Confirme o baseline
```bash
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```
Tem de dar `BUILD SUCCESSFUL` **antes** de você mexer em nada. Começar com o build vermelho
esconde qual falha é sua.

### 3. RED — escreva os testes primeiro
Traduza **cada** item de `Testes críticos` em um teste de verdade. Use os valores literais da
SPEC: `assertEquals("R$ 5,49", formatarBrl(549))`, não um valor recalculado.

Rode e **confirme que falham**:
```bash
./gradlew :app:testDebugUnitTest --tests "*<SeuTeste>*"
```
Se um teste passa antes da implementação, ele está errado ou não testa nada. Conserte antes de
seguir.

### 4. GREEN — implemente o mínimo
Escreva só o necessário para os testes passarem e entregar exatamente o `Output` da task —
mesma assinatura, mesmo nome de arquivo. Nada além disso: melhoria não pedida é escopo novo.

Respeite as regras que valem para a sua camada (`.claude/rules/`): dinheiro em `Long` centavos,
`withTimeout` em toda chamada de rede, estado no ViewModel, sem Firebase em `ui/`.

### 5. Verifique
```bash
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```
Todos os `Testes críticos` cobertos e verdes. Depois `git diff` e confira que você não tocou em
arquivo de outra task (ver a tabela anti-conflito no fim do `PLAN.md`).

### 6. Revise e marque
Dispare `@code-reviewer`. Corrija todo **BLOQUEANTE**. Se a task mexeu em `firestore.rules`,
dispare também `@firestore-rules-auditor`.

Só então marque os checkboxes de `Testes críticos` no `PLAN.md` — é a única edição permitida
nesse arquivo.

## Restrições

- Não edite `SPEC.md` nem `CLAUDE.md`. Se a SPEC está errada ou omissa, **pare e sinalize**:
  mudar o contrato para o código passar inverte a relação entre os dois.
- Não adicione dependência fora de `SPEC.md §7`.
- Não conserte bug fora do escopo da task — registre e siga.
- **Travou por 2 tentativas? Pare e sinalize.** Não invente solução alternativa.

## Exemplo

**Input:** `/implementar 1.1`

**Output esperado:**
```
Task 1.1 — Formatadores  (agent: core-kotlin)
Input verificado: SPEC.md §F4 lido. Baseline: BUILD SUCCESSFUL.

RED  FormatadoresTest: 9 testes escritos, 9 falhando (Unresolved reference: formatarBrl) ✓
GREEN core/Formatadores.kt: formatarBrl(Long) e normalizar(String)
      → ./gradlew :app:testDebugUnitTest --tests "*FormatadoresTest*" → 9/9 passando

Gate: ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest → BUILD SUCCESSFUL
git diff: apenas core/Formatadores.kt e core/FormatadoresTest.kt
Review: sem BLOQUEANTE.
Checkboxes 1.1 marcados no PLAN.md (3 de 3).
```
