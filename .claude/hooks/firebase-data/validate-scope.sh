#!/usr/bin/env bash
# Escopo do agent firebase-data: domínio, dados, DI, Application, Manifest e Firestore.
# Parsing sem jq: extrai SÓ o campo file_path.

INPUT="$(cat)"

FP="$(printf '%s' "$INPUT" \
  | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -1 \
  | sed -E 's/^"file_path"[[:space:]]*:[[:space:]]*"//; s/"$//')"

FP="$(printf '%s' "$FP" | sed -e 's|\\\\|/|g' -e 's|\\|/|g')"

[ -z "$FP" ] && exit 0

# app/google-services.json é gerado por humano no Console do Firebase.
# Um agent que "conserta" esse arquivo quebra o app de um jeito difícil de diagnosticar.
case "$FP" in
  */google-services.json)
    cat >&2 <<'MSG'
BLOQUEADO: app/google-services.json é gerado pelo Console do Firebase.

Não crie, não edite e não gere um substituto para "fazer o build passar".
Se o build falha com erro do plugin google-services, o diagnóstico correto é:
falta o passo 2 de CLAUDE.md §3, que é uma AÇÃO HUMANA.

O que fazer: pare e avise que o arquivo precisa ser baixado do Console e colocado
em app/google-services.json. Nenhum agent consegue executar esse passo.
MSG
    exit 2
    ;;
esac

case "$FP" in
  */java/com/example/merceariadamaria/domain/*)        exit 0 ;;
  */java/com/example/merceariadamaria/data/*)          exit 0 ;;
  */java/com/example/merceariadamaria/di/*)            exit 0 ;;
  */java/com/example/merceariadamaria/MerceariaApplication.kt) exit 0 ;;
  */app/src/main/AndroidManifest.xml)                  exit 0 ;;
  */firestore.rules)                                   exit 0 ;;
  */firestore.indexes.json)                            exit 0 ;;
  */app/src/test/java/com/example/merceariadamaria/fake/*)   exit 0 ;;
  */app/src/test/java/com/example/merceariadamaria/data/*)   exit 0 ;;
  */app/src/test/java/com/example/merceariadamaria/domain/*) exit 0 ;;
  */app/src/test/java/com/example/merceariadamaria/di/*)     exit 0 ;;
esac

cat >&2 <<MSG
BLOQUEADO: fora do escopo do agent firebase-data.

Arquivo recusado: $FP

Este agent só escreve em:
  domain/**  data/**  di/**  MerceariaApplication.kt
  app/src/main/AndroidManifest.xml
  firestore.rules  firestore.indexes.json
  app/src/test/.../{fake,data,domain,di}/**

Em particular ui/ e components/ NÃO são seus: a UI consome interfaces de
repositório e nunca importa com.google.firebase (CLAUDE.md §5). Se a task exige
mexer em tela ou ViewModel, ela é do agent compose-ui. Arquivos de build são do
build-engineer.
MSG
exit 2
