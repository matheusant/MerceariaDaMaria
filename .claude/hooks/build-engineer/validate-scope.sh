#!/usr/bin/env bash
# Escopo do agent build-engineer: só arquivos de configuração de build.
# Parsing sem jq: extrai SÓ o campo file_path.

INPUT="$(cat)"

FP="$(printf '%s' "$INPUT" \
  | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -1 \
  | sed -E 's/^"file_path"[[:space:]]*:[[:space:]]*"//; s/"$//')"

FP="$(printf '%s' "$FP" | sed -e 's|\\\\|/|g' -e 's|\\|/|g')"

[ -z "$FP" ] && exit 0

# Bloqueios específicos vêm ANTES da allowlist: local.properties e gradle-wrapper
# casariam com o padrão de "arquivo de build", mas são intocáveis.
case "$FP" in
  */google-services.json)
    cat >&2 <<'MSG'
BLOQUEADO: app/google-services.json é gerado pelo Console do Firebase.

Não crie, não edite e não gere um substituto para "fazer o build passar", e não
remova o plugin google-services para contornar. Se o build falha por causa dele,
o diagnóstico correto é: falta o passo 2 de CLAUDE.md §3 — uma AÇÃO HUMANA.

O que fazer: pare e avise que o arquivo precisa ser baixado do Console.
MSG
    exit 2
    ;;
  */local.properties)
    echo "BLOQUEADO: local.properties contém o caminho do SDK desta máquina e é gitignored. Não é configuração compartilhada do projeto (CLAUDE.md §8.9)." >&2
    exit 2
    ;;
  */gradle/wrapper/*)
    echo "BLOQUEADO: gradle/wrapper/ é binário e versão do wrapper. Trocar a versão do Gradle é mudança de stack e exige aprovação humana (CLAUDE.md §8.2)." >&2
    exit 2
    ;;
esac

case "$FP" in
  */gradle/libs.versions.toml) exit 0 ;;
  */app/build.gradle.kts)      exit 0 ;;
  */settings.gradle.kts)       exit 0 ;;
  */gradle.properties)         exit 0 ;;
  */app/proguard-rules.pro)    exit 0 ;;
  */build.gradle.kts)          exit 0 ;;   # raiz — depois do app/, que é mais específico
esac

cat >&2 <<MSG
BLOQUEADO: fora do escopo do agent build-engineer.

Arquivo recusado: $FP

Este agent só escreve em:
  gradle/libs.versions.toml   build.gradle.kts   app/build.gradle.kts
  settings.gradle.kts         gradle.properties  app/proguard-rules.pro

Este agent não escreve código Kotlin. Se a task exige isso, ela é do core-kotlin
(funções puras), do firebase-data (domínio e dados) ou do compose-ui (telas).
MSG
exit 2
