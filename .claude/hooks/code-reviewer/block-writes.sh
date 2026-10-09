#!/usr/bin/env bash
# code-reviewer é SOMENTE LEITURA. Write/Edit/Bash bloqueados por design.
# Contrato de hook: exit 2 bloqueia e devolve o stderr ao agent; exit 0 permite.
cat >/dev/null 2>&1   # drena o stdin para o Claude Code não ver pipe quebrado

cat >&2 <<'MSG'
BLOQUEADO: o agent code-reviewer é somente leitura.

Write, Edit, Bash e NotebookEdit estão indisponíveis para este agent por design
(.claude/agents/code-reviewer.md). Sua função é ler, analisar e reportar.

O que fazer: reporte o problema classificado como BLOQUEANTE / IMPORTANTE / SUGESTÃO,
com arquivo:linha e a regra da SPEC.md ou do CLAUDE.md que foi violada. Quem corrige
é o agent de implementação da task.
MSG
exit 2
