#!/usr/bin/env bash
# O agent test-runner só pode executar verificação. Qualquer outro comando é bloqueado.
# Parsing sem jq: extrai SÓ o campo command — inspecionar o JSON inteiro daria
# falso-positivo com qualquer texto que contenha "rm" ou "git".

INPUT="$(cat)"

CMD="$(printf '%s' "$INPUT" \
  | grep -oE '"command"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -1 \
  | sed -E 's/^"command"[[:space:]]*:[[:space:]]*"//; s/"$//')"

# Sem comando identificável: nada a autorizar. Bloqueia por segurança — este agent
# não tem caso de uso legítimo com comando ilegível.
if [ -z "$CMD" ]; then
  echo "BLOQUEADO: não foi possível identificar o comando. O agent test-runner só executa a lista de comandos de verificação de .claude/agents/test-runner.md." >&2
  exit 2
fi

# Allowlist: gradlew com tarefas de verificação, e leitura de relatório de teste.
case "$CMD" in
  *"rm "*|*"rm -"*|*"git commit"*|*"git push"*|*"git reset"*|*"git checkout"*|*">"*|*"curl"*|*"wget"*|*"npm "*|*"pip "*)
    echo "BLOQUEADO: comando destrutivo, de rede ou de escrita. O agent test-runner é somente verificação: sem git, sem remoção, sem redirecionamento, sem instalação de pacote." >&2
    exit 2
    ;;
esac

case "$CMD" in
  *"gradlew"*"clean"*)
    echo "BLOQUEADO: 'clean' apaga o cache de build e transforma um gate de 20s em vários minutos. Se precisar de build limpo, peça a um humano." >&2
    exit 2
    ;;
  *"gradlew"*"compileDebugKotlin"*) exit 0 ;;
  *"gradlew"*"testDebugUnitTest"*)  exit 0 ;;
  *"gradlew"*"assembleDebug"*)      exit 0 ;;
  *"gradlew"*"lintDebug"*)          exit 0 ;;
  *"gradlew"*"connectedDebugAndroidTest"*) exit 0 ;;
  *"gradlew"*"dependencies"*)       exit 0 ;;
  *"gradlew"*"tasks"*)              exit 0 ;;
esac

cat >&2 <<MSG
BLOQUEADO: comando fora do escopo do agent test-runner.

Comando recusado: $CMD

Permitidos (ver .claude/agents/test-runner.md):
  ./gradlew :app:compileDebugKotlin
  ./gradlew :app:testDebugUnitTest            (com ou sem --tests "<padrão>")
  ./gradlew :app:assembleDebug
  ./gradlew :app:lintDebug
  ./gradlew :app:connectedDebugAndroidTest
  ./gradlew :app:dependencies --configuration debugRuntimeClasspath
  ./gradlew tasks

Este agent verifica e relata; não implementa, não conserta e não commita.
MSG
exit 2
