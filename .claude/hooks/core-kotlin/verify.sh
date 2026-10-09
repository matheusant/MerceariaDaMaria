#!/usr/bin/env bash
# Stop hook do core-kotlin: invariantes estáticas da camada pura.
# No frontmatter de um agent, Stop é convertido em SubagentStop.

INPUT="$(cat)"

# Sem esta guarda, o hook reentra e vira loop infinito.
if printf '%s' "$INPUT" | grep -q '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

CORE="app/src/main/java/com/example/merceariadamaria/core"
[ -d "$CORE" ] || exit 0

FALHAS=""

# 1. core/ é Kotlin puro: nada de Android nem Firebase.
IMPUROS="$(grep -rnE '^[[:space:]]*import[[:space:]]+(android|androidx|com\.google\.firebase)\.' "$CORE" 2>/dev/null)"
if [ -n "$IMPUROS" ]; then
  FALHAS="$FALHAS
[1] core/ importou Android ou Firebase (CLAUDE.md §5: core não depende de ninguém):
$IMPUROS"
fi

# 2. Dinheiro é Long em centavos (SPEC.md §8.7). Em core/ o veto é total:
#    nenhuma função desta camada tem motivo legítimo para ponto flutuante.
FLOATS="$(grep -rnE '\b(Double|Float|BigDecimal)\b' "$CORE" 2>/dev/null)"
if [ -n "$FLOATS" ]; then
  FALHAS="$FALHAS
[2] Ponto flutuante em core/ (SPEC.md §8.7: valor monetário é Long em centavos):
$FLOATS"
fi

# 3. formatarBrl tem de ser determinística — não pode depender do Locale do device.
LOCALE="$(grep -rnE '\bLocale\b|getCurrencyInstance|NumberFormat' "$CORE" 2>/dev/null)"
if [ -n "$LOCALE" ]; then
  FALHAS="$FALHAS
[3] Uso de Locale/NumberFormat em core/ (SPEC.md §F4: formatarBrl é determinística
    e monta a string à mão a partir do Long, para dar o mesmo resultado em qualquer
    aparelho):
$LOCALE"
fi

if [ -n "$FALHAS" ]; then
  printf 'BLOQUEADO: invariantes de core/ violadas.\n%s\n\nCorrija antes de encerrar.\n' "$FALHAS" >&2
  exit 2
fi

exit 0
