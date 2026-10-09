#!/usr/bin/env bash
# Stop hook do compose-ui: invariantes da camada de apresentação.

INPUT="$(cat)"

if printf '%s' "$INPUT" | grep -q '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

BASE="app/src/main/java/com/example/merceariadamaria"
UI="$BASE/ui"
COMP="$BASE/components"

FALHAS=""

# 1. A UI não conhece Firebase. A fronteira é a interface de repositório (CLAUDE.md §5).
for DIR in "$UI" "$COMP"; do
  [ -d "$DIR" ] || continue
  VAZOU="$(grep -rnE '^[[:space:]]*import[[:space:]]+com\.google\.firebase\.' "$DIR" 2>/dev/null)"
  if [ -n "$VAZOU" ]; then
    FALHAS="$FALHAS
[1] Import de Firebase em $DIR. O dado tem de chegar por uma interface de repositório
    injetada pelo AppContainer:
$VAZOU"
  fi
done

# 2. Dinheiro é Long em centavos (SPEC.md §8.7). A regex mira identificadores monetários
#    de propósito: Float é legítimo em Compose (weight(1f), alpha, fillMaxWidth(0.5f)).
for DIR in "$UI" "$COMP"; do
  [ -d "$DIR" ] || continue
  MONEY="$(grep -rniE '(preco|valor|saldo|total|subtotal)[A-Za-z]*[[:space:]]*:[[:space:]]*(Double|Float|BigDecimal)' "$DIR" 2>/dev/null)"
  CONV="$(grep -rniE '(centavos|preco|saldo|subtotal)[A-Za-z]*\.to(Double|Float)\(' "$DIR" 2>/dev/null)"
  if [ -n "$MONEY$CONV" ]; then
    FALHAS="$FALHAS
[2] Valor monetário em ponto flutuante em $DIR (SPEC.md §8.7: Long em centavos;
    preço digitado em reais vira centavos sem passar por ponto flutuante):
$MONEY$CONV"
  fi
done

# 3. Material 3 é o padrão (SPEC.md §8.11). AVISO, não bloqueio: os dois usos legados
#    de M2 só saem na Task 0.2, e bloquear aqui reprovaria um agent rodando outra task
#    da mesma fase por dívida que não é dele. Quem reprova isso na task certa é o
#    code-reviewer.
for DIR in "$UI" "$COMP"; do
  [ -d "$DIR" ] || continue
  M2="$(grep -rnE '^[[:space:]]*import[[:space:]]+(androidx\.compose\.material\.(Divider|ClickableText)|androidx\.compose\.foundation\.text\.ClickableText)' "$DIR" 2>/dev/null)"
  if [ -n "$M2" ]; then
    printf 'AVISO: API depreciada de Material 2 ainda presente (SPEC.md §8.11 manda usar\nHorizontalDivider do M3 e Text com LinkAnnotation). Se esta é a Task 0.2, corrija agora:\n%s\n\n' "$M2" >&2
  fi
done

# 4. AppNavHost.kt é editado apenas pela Task 5.2. Aviso, não bloqueio: o hook não sabe
#    qual task está rodando, e bloquear travaria a própria Task 5.2.
if [ -f "$UI/navigation/AppNavHost.kt" ]; then
  # Sem `|| echo 0`: grep -c já imprime "0" quando não casa (e sai com código 1); o `||`
  # acrescentaria uma segunda linha e a comparação numérica quebraria em silêncio.
  ROTAS="$(grep -cE 'composable\(' "$UI/navigation/AppNavHost.kt" 2>/dev/null)"
  CONSTS="$(grep -cE 'const val ' "$UI/navigation/AppNavHost.kt" 2>/dev/null)"
  if [ "${CONSTS:-0}" -gt 0 ] && [ "${ROTAS:-0}" -lt "${CONSTS:-0}" ]; then
    printf 'AVISO: AppNavHost.kt tem %s constantes em Routes e só %s composable() registrados.\nToda rota precisa de um destino (SPEC.md §9.3: Routes.LOGIN ficou órfã assim).\n' \
      "$CONSTS" "$ROTAS" >&2
  fi
fi

if [ -n "$FALHAS" ]; then
  printf 'BLOQUEADO: invariantes da camada de UI violadas.\n%s\n\nCorrija antes de encerrar.\n' "$FALHAS" >&2
  exit 2
fi

exit 0
