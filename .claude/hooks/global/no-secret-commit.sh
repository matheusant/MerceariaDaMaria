#!/usr/bin/env bash
# Hook GLOBAL (settings.json): impede que um segredo entre no histórico do git e
# bloqueia reescrita de histórico. Um segredo commitado não sai com um `git rm`.
#
# Parsing sem jq: extrai SÓ o campo command.

INPUT="$(cat)"

CMD="$(printf '%s' "$INPUT" \
  | grep -oE '"command"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -1 \
  | sed -E 's/^"command"[[:space:]]*:[[:space:]]*"//; s/"$//')"

[ -z "$CMD" ] && exit 0

# Só nos interessam comandos git.
case "$CMD" in
  *git*) ;;
  *) exit 0 ;;
esac

# 1. Reescrita de histórico e push forçado — destrutivo e difícil de desfazer.
case "$CMD" in
  *"push"*"--force"*|*"push"*"-f "*|*"filter-branch"*|*"reset --hard"*)
    cat >&2 <<'MSG'
BLOQUEADO: operação destrutiva de git.

push --force, filter-branch e reset --hard descartam trabalho de forma difícil de
reverter. Se for realmente necessário, peça a um humano para executar.
MSG
    exit 2 ;;
esac

# 2. Adicionar segredo explicitamente pelo caminho.
case "$CMD" in
  *"git add"*)
    case "$CMD" in
      *".jks"*|*".keystore"*|*"local.properties"*|*".env"*|*"service-account"*|*"firebase-adminsdk"*)
        cat >&2 <<'MSG'
BLOQUEADO: tentativa de adicionar um segredo ao git.

Arquivos de assinatura (.jks/.keystore), local.properties, .env e credenciais de
service account nunca entram no repositório. Um segredo commitado permanece no
histórico mesmo depois de removido do working tree.
MSG
        exit 2 ;;
    esac
    ;;
esac

# 3. Commit com segredo já staged. Verifica o índice de verdade em vez de confiar no
#    texto do comando — `git commit -a` não menciona arquivo nenhum.
case "$CMD" in
  *"git commit"*)
    if command -v git >/dev/null 2>&1; then
      STAGED="$(git diff --cached --name-only 2>/dev/null | grep -iE '(\.jks|\.keystore|\.p12|\.pem|local\.properties|(^|/)\.env|service-account|firebase-adminsdk)' || true)"
      if [ -n "$STAGED" ]; then
        printf 'BLOQUEADO: há segredo(s) no índice do git:\n%s\n\nRode `git restore --staged <arquivo>` e confirme que o .gitignore cobre o padrão antes de commitar.\n' "$STAGED" >&2
        exit 2
      fi
    fi
    ;;
esac

exit 0
