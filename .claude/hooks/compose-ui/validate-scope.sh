#!/usr/bin/env bash
# Escopo do agent compose-ui: apresentação (ui/, components/, res/) e testes de UI.
# Parsing sem jq: extrai SÓ o campo file_path.

INPUT="$(cat)"

FP="$(printf '%s' "$INPUT" \
  | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -1 \
  | sed -E 's/^"file_path"[[:space:]]*:[[:space:]]*"//; s/"$//')"

FP="$(printf '%s' "$FP" | sed -e 's|\\\\|/|g' -e 's|\\|/|g')"

[ -z "$FP" ] && exit 0

case "$FP" in
  */java/com/example/merceariadamaria/ui/*)          exit 0 ;;
  */java/com/example/merceariadamaria/components/*)  exit 0 ;;
  */java/com/example/merceariadamaria/MainActivity.kt) exit 0 ;;
  */app/src/main/res/*)                              exit 0 ;;
  */app/src/androidTest/*)                           exit 0 ;;
  */app/src/test/java/com/example/merceariadamaria/ui/*) exit 0 ;;
esac

cat >&2 <<MSG
BLOQUEADO: fora do escopo do agent compose-ui.

Arquivo recusado: $FP

Este agent só escreve em:
  ui/**  components/**  MainActivity.kt
  app/src/main/res/**
  app/src/test/.../ui/**   app/src/androidTest/**

Domínio, mappers, repositórios, AppContainer e firestore.rules são do agent
firebase-data. Funções puras (formatarBrl, normalizar, validadores) são do
core-kotlin. Arquivos de build são do build-engineer.

Se você precisa de um dado do Firebase, ele chega por uma interface de repositório
injetada pelo AppContainer — a UI nunca importa com.google.firebase (CLAUDE.md §5).
MSG
exit 2
