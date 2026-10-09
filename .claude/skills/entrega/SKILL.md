---
name: entrega
description: >
  Roda os gates de encerramento de sessão: build e testes verdes, checkboxes do PLAN.md
  coerentes com o código, nenhum review pendente, nenhum segredo staged, e preenchimento da
  seção "Próxima sessão" do PLAN.md. Use sempre ao finalizar o trabalho ou antes de commitar.
  Exemplos de trigger: "vamos fechar", "encerrar a sessão", "pronto para commitar", "entrega",
  "o que ficou pendente".
---

# Entrega — gates de encerramento

## Quando usar

- Ao encerrar a sessão de trabalho.
- Antes de commitar ou abrir PR.

## Quando NÃO usar

- **No meio de uma task** — termine ou registre o ponto de parada primeiro.
- Para revisar código: use `/review`. Este comando **verifica** e **registra**; não conserta.

## Passo a passo

### 1. Gate de build
```bash
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```
Tem de dar `BUILD SUCCESSFUL`. Se está vermelho, **não feche a sessão como entregue**: registre
o que quebrou na seção `## Próxima sessão` e diga com clareza que a entrega está incompleta.

Se houver device conectado, rode também:
```bash
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```
Sem device, registre que os testes de UI não foram executados — não dê como aprovados.

### 2. Coerência dos checkboxes
Para cada checkbox marcado no `PLAN.md` nesta sessão, confirme que o teste correspondente existe
e passa. Checkbox marcado sem teste verde é pior que checkbox desmarcado: mente para a próxima
sessão. Desmarque o que não se sustenta.

### 3. Review pendente
Confirme que toda task concluída passou por `/review` e que nenhum **BLOQUEANTE** ficou aberto.
Se `firestore.rules` mudou, confirme que o `@firestore-rules-auditor` rodou e que
`docs/verificacao-regras.md` está atualizado.

### 4. Higiene do repositório
```bash
git status --porcelain
git diff --cached --name-only
```
Verifique: nenhum segredo staged (`*.jks`, `local.properties`, `.env`, service account); nenhum
arquivo gerado (`app/build/`, `.gradle/`); `SPEC.md` e `CLAUDE.md` **não** modificados; nenhum
arquivo temporário sobrando. Todo o `.claude/` deve estar versionado — é arquitetura.

### 5. Setup ainda reproduzível
Confirme que os 5 pré-requisitos de `CLAUDE.md §3` continuam documentados e que nada no código
passou a depender de um passo manual não escrito. Alguém que clona o repo hoje conseguiria subir
o projeto seguindo só o `CLAUDE.md`?

### 6. Preencha `## Próxima sessão` no `PLAN.md`
Substitua a seção por, em 5 linhas:
- **Onde paramos:** última task concluída e a próxima da fila.
- **Estado do build:** comando rodado e resultado literal.
- **Bloqueio ativo:** o que impede a próxima task (ou "nenhum").
- **Próxima ação:** a primeira coisa concreta a fazer.
- **Decisões pendentes:** o que precisa de resposta humana (ou "nenhuma").

Essa é a **segunda** edição permitida no `PLAN.md`, além dos checkboxes.

## Exemplo

**Input:** `/entrega`

**Output esperado:**
```
GATES DE ENCERRAMENTO

[✓] Build       ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest → BUILD SUCCESSFUL (17 testes)
[✓] Lint        ./gradlew :app:lintDebug → BUILD SUCCESSFUL, 0 error
[–] UI tests    não executados: nenhum device conectado (NÃO contar como aprovados)
[✓] Checkboxes  1.1, 1.2, 1.3, 1.4 marcados — 17 testes correspondentes verdes
[✓] Review      4 tasks revisadas, 0 BLOQUEANTE aberto
[✓] Git         nada staged; SPEC.md e CLAUDE.md intactos; sem segredo; sem gerado
[✓] Setup       CLAUDE.md §3 íntegro; nenhum passo manual novo
[✓] PLAN.md     "Próxima sessão" atualizada

ENTREGA COMPLETA — Fase 1 fechada. Próxima: Fase 2, Task 2.1 (@firebase-data).
```
