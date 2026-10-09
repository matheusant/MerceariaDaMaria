#!/usr/bin/env bash
# Hook GLOBAL (settings.json): protege arquivos sensíveis e gerados contra Write/Edit.
# Atenção: hooks globais NÃO são herdados por sub-agents — só disparam na sessão
# principal. A proteção equivalente para sub-agents está nos validate-scope.sh de cada
# agent (.claude/hooks/<agent>/validate-scope.sh).
#
# Parsing sem jq: extrai SÓ o campo file_path. Inspecionar o JSON inteiro daria
# falso-positivo com o "content" de um Write que mencione ".env" ou "SPEC.md".

INPUT="$(cat)"

FP="$(printf '%s' "$INPUT" \
  | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"([^"\\]|\\.)*"' \
  | head -1 \
  | sed -E 's/^"file_path"[[:space:]]*:[[:space:]]*"//; s/"$//')"

FP="$(printf '%s' "$FP" | sed -e 's|\\\\|/|g' -e 's|\\|/|g')"

[ -z "$FP" ] && exit 0

bloqueia() {
  printf 'BLOQUEADO: %s\n\nArquivo: %s\n' "$1" "$FP" >&2
  exit 2
}

case "$FP" in
  # --- Segredos e credenciais ---
  *.jks|*.keystore|*.p12|*.pem)
    bloqueia "arquivo de assinatura/credencial. Nunca editar nem commitar (CLAUDE.md §8.9)." ;;
  */local.properties)
    bloqueia "local.properties contém o caminho do SDK desta máquina e é gitignored." ;;
  */.env|*/.env.*|*/secrets.properties|*/keystore.properties)
    bloqueia "arquivo de segredo." ;;
  *service-account*.json|*firebase-adminsdk*.json)
    bloqueia "credencial de service account. Este projeto não usa Admin SDK, e passar a usar exige aprovação." ;;

  # --- Configuração gerada pelo Console do Firebase ---
  */google-services.json)
    cat >&2 <<'MSG'
BLOQUEADO: google-services.json é gerado pelo Console do Firebase.

Não crie, não edite e não gere um substituto para "fazer o build passar", e não
remova o plugin google-services para contornar. Se o build falha por causa dele,
falta o passo 2 de CLAUDE.md §3 — uma AÇÃO HUMANA que nenhum agent executa.
MSG
    exit 2 ;;

  # --- Contratos do Harness ---
  */SPEC.md)
    bloqueia "SPEC.md é o contrato e não se altera durante a execução de uma task (CLAUDE.md §8.1). Se a SPEC está errada ou omissa, PARE e sinalize — mudar o contrato para o código passar inverte a relação." ;;
  */CLAUDE.md)
    bloqueia "CLAUDE.md é o onboarding do projeto e não se altera durante a execução de uma task (CLAUDE.md §8.1)." ;;

  # --- Gerados: editar é inútil e engana o diff ---
  */app/build/*|*/build/*|*/.gradle/*|*/.idea/*|*/captures/*|*/.cxx/*)
    bloqueia "diretório gerado pelo build ou pela IDE. É regenerado por ./gradlew — editar não tem efeito." ;;
  */gradle/wrapper/gradle-wrapper.jar)
    bloqueia "binário do wrapper do Gradle. Trocar a versão do Gradle é mudança de stack (CLAUDE.md §8.2)." ;;
  *.apk|*.aab|*.dex|*.class)
    bloqueia "artefato binário de build." ;;
esac

exit 0
