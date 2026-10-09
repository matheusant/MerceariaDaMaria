#!/usr/bin/env bash
# Stop hook do firebase-data: invariantes da camada de dados.

INPUT="$(cat)"

if printf '%s' "$INPUT" | grep -q '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

BASE="app/src/main/java/com/example/merceariadamaria"
DOMAIN="$BASE/domain"
DATA="$BASE/data"

FALHAS=""

# 1. domain/ não conhece Firebase nem Android (CLAUDE.md §5: ui -> domain <- data).
if [ -d "$DOMAIN" ]; then
  VAZOU="$(grep -rnE '^[[:space:]]*import[[:space:]]+(com\.google\.firebase|android)\.' "$DOMAIN" 2>/dev/null)"
  if [ -n "$VAZOU" ]; then
    FALHAS="$FALHAS
[1] domain/ importou Firebase ou Android. O domínio é puro: converta Timestamp para
    Long em millis no mapper e nunca exponha DocumentSnapshot/FirebaseUser:
$VAZOU"
  fi
fi

# 2. Dinheiro é Long em centavos (SPEC.md §8.7). Regex mira identificadores de valor
#    monetário, não Double/Float em geral.
for DIR in "$DOMAIN" "$DATA"; do
  [ -d "$DIR" ] || continue
  MONEY="$(grep -rniE '(preco|valor|saldo|total|subtotal)[A-Za-z]*[[:space:]]*:[[:space:]]*(Double|Float|BigDecimal)' "$DIR" 2>/dev/null)"
  CONV="$(grep -rniE '(centavos|preco|valor|saldo|total|subtotal)[A-Za-z]*\.to(Double|Float)\(' "$DIR" 2>/dev/null)"
  if [ -n "$MONEY$CONV" ]; then
    FALHAS="$FALHAS
[2] Valor monetário em ponto flutuante em $DIR (SPEC.md §8.7: sempre Long em centavos,
    porque 0.1 + 0.2 != 0.3):
$MONEY$CONV"
  fi
done

# 3. Toda chamada de rede tem timeout (SPEC.md §F12): offline, o Task de uma escrita
#    nunca completa e o await() pendura para sempre.
if [ -d "$DATA" ]; then
  for F in $(find "$DATA" -name '*.kt' 2>/dev/null); do
    # Sem `|| echo 0`: grep -c já imprime "0" quando não casa, e sai com código 1.
    # O `||` acrescentaria um segundo "0", e a comparação numérica quebraria em silêncio
    # — o hook passaria a aprovar tudo.
    N_AWAIT="$(grep -cE '\.await\(\)' "$F" 2>/dev/null)"
    N_TIMEOUT="$(grep -cE 'withTimeout' "$F" 2>/dev/null)"
    if [ "${N_AWAIT:-0}" -gt 0 ] && [ "${N_TIMEOUT:-0}" -lt 1 ]; then
      FALHAS="$FALHAS
[3] $F tem $N_AWAIT chamada(s) await() e nenhum withTimeout (SPEC.md §F12: todo
    await() de Auth ou Firestore vai dentro de withTimeout(10_000))."
    fi
  done
fi

# 4. add() não existe dentro de runTransaction — o id tem de ser pré-gerado com
#    collection.document() (SPEC.md §F6).
if [ -d "$DATA" ]; then
  ADD_TX="$(grep -rnE 'transaction[[:space:]]*\.[[:space:]]*add\(|\.add\([^)]*\)[[:space:]]*//.*transa' "$DATA" 2>/dev/null)"
  if [ -n "$ADD_TX" ]; then
    FALHAS="$FALHAS
[4] Uso de add() em contexto de transação (SPEC.md §F6: pré-gere o id com
    collection(\"lancamentos\").document() e use transaction.set(ref, dados)):
$ADD_TX"
  fi
fi

if [ -n "$FALHAS" ]; then
  printf 'BLOQUEADO: invariantes da camada de dados violadas.\n%s\n\nCorrija antes de encerrar.\n' "$FALHAS" >&2
  exit 2
fi

exit 0
