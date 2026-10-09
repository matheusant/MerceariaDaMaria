#!/usr/bin/env bash
# Escopo do agent core-kotlin: só core/ (produção) e os testes de core/.
# Parsing sem jq: extrai SÓ o campo file_path — nunca o JSON inteiro, senão o
# "content" de um Write dá falso-positivo.

INPUT="$(cat)"

FP="$(printf '%s' "$INPUT" \
  | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -1 \
  | sed -E 's/^"file_path"[[:space:]]*:[[:space:]]*"//; s/"$//')"

# Normaliza separadores do Windows (JSON escapa como \\) para comparar com barras.
FP="$(printf '%s' "$FP" | sed -e 's|\\\\|/|g' -e 's|\\|/|g')"

# Sem file_path identificável: não há o que validar.
[ -z "$FP" ] && exit 0

case "$FP" in
  */java/com/example/merceariadamaria/core/*) exit 0 ;;
esac

cat >&2 <<MSG
BLOQUEADO: fora do escopo do agent core-kotlin.

Arquivo recusado: $FP

Este agent só escreve em:
  app/src/main/java/com/example/merceariadamaria/core/**
  app/src/test/java/com/example/merceariadamaria/core/**

core/ é Kotlin puro (sem Android, sem Firebase) — é o que torna esta camada
testável na JVM sem device. Se a task exige mexer em outro lugar, ela é de outro
agent: telas e ViewModels são do compose-ui; domínio, dados e Firebase são do
firebase-data; arquivos de build são do build-engineer.
MSG
exit 2
