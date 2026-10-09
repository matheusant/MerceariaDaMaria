#!/usr/bin/env bash
# Stop hook do build-engineer: invariantes da configuração de build.

INPUT="$(cat)"

if printf '%s' "$INPUT" | grep -q '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

APP_GRADLE="app/build.gradle.kts"
TOML="gradle/libs.versions.toml"

FALHAS=""

# 1. Versões vivem no catálogo, não espalhadas (CLAUDE.md §2).
#    A regex mira coordenada Maven literal (group:artifact:versao) em linha de
#    declaração de dependência — de propósito. Um match amplo por qualquer "N.N"
#    daria falso-positivo em jvmTarget = "1.8" e kotlinCompilerExtensionVersion,
#    que são configuração do compilador e não dependência.
#    desugar_jdk_libs é a exceção aprovada em SPEC.md §7 (nota de build).
if [ -f "$APP_GRADLE" ]; then
  LITERAIS="$(grep -nE '(implementation|api|testImplementation|androidTestImplementation|debugImplementation|classpath)[[:space:]]*\([[:space:]]*"[^"]+:[^"]+:[0-9][^"]*"' "$APP_GRADLE" 2>/dev/null | grep -v 'desugar_jdk_libs')"
  if [ -n "$LITERAIS" ]; then
    FALHAS="$FALHAS
[1] Versão literal em $APP_GRADLE. Toda dependência entra primeiro em
    gradle/libs.versions.toml e é referenciada por alias (CLAUDE.md §2):
$LITERAIS"
  fi
fi

# 2. A stack é fixa (CLAUDE.md §8.2). Mudar qualquer um destes exige aprovação humana.
if [ -f "$APP_GRADLE" ]; then
  if ! grep -qE 'compileSdk[[:space:]]*=[[:space:]]*36' "$APP_GRADLE" 2>/dev/null; then
    FALHAS="$FALHAS
[2a] compileSdk deixou de ser 36 (SPEC.md §7). Mudança de stack precisa de aprovação."
  fi
  if ! grep -qE 'minSdk[[:space:]]*=[[:space:]]*26' "$APP_GRADLE" 2>/dev/null; then
    FALHAS="$FALHAS
[2b] minSdk deixou de ser 26 (SPEC.md §7). Mudança de stack precisa de aprovação."
  fi
  if ! grep -qE 'jvmTarget[[:space:]]*=[[:space:]]*"1\.8"' "$APP_GRADLE" 2>/dev/null; then
    FALHAS="$FALHAS
[2c] jvmTarget deixou de ser \"1.8\" (SPEC.md §7 e a nota de build: se o Firebase exigir
     APIs Java 8+, o caminho aprovado é core library desugaring, não subir o jvmTarget)."
  fi
fi

if [ -f "$TOML" ]; then
  if ! grep -qE '^kotlin[[:space:]]*=[[:space:]]*"1\.9\.0"' "$TOML" 2>/dev/null; then
    FALHAS="$FALHAS
[2d] Kotlin deixou de ser 1.9.0 em $TOML. Está casado com o Compose Compiler
     Extension 1.5.1 — trocar um sem o outro quebra o build (CLAUDE.md §2)."
  fi
fi

# 3. O plugin google-services precisa continuar aplicado: removê-lo é o contorno
#    proibido para a ausência do google-services.json (CLAUDE.md §8.3).
if [ -f "$APP_GRADLE" ] && grep -qE 'firebase' "$APP_GRADLE" 2>/dev/null; then
  if ! grep -qE 'google.?services|com\.google\.gms' "$APP_GRADLE" 2>/dev/null; then
    FALHAS="$FALHAS
[3] Há dependências do Firebase em $APP_GRADLE mas o plugin google-services não está
    aplicado. Sem ele o google-services.json não é processado e o Firebase falha em
    runtime com erro obscuro."
  fi
fi

if [ -n "$FALHAS" ]; then
  printf 'BLOQUEADO: invariantes de build violadas.\n%s\n\nCorrija antes de encerrar.\n' "$FALHAS" >&2
  exit 2
fi

exit 0
