#!/usr/bin/env bash
# firestore-rules-auditor é SOMENTE LEITURA — inclusive para o firestore.rules.
# Contrato de hook: exit 2 bloqueia e devolve o stderr ao agent; exit 0 permite.
cat >/dev/null 2>&1

cat >&2 <<'MSG'
BLOQUEADO: o agent firestore-rules-auditor é somente leitura.

Isso inclui o próprio firestore.rules: quem audita não edita. Um auditor que
"conserta" a regra que acabou de reprovar perde a independência da auditoria.

O que fazer: reporte cada achado como BLOQUEANTE (escalada de privilégio ou
vazamento entre clientes), IMPORTANTE (regra mais larga que o necessário) ou
SUGESTÃO, apontando a linha do firestore.rules e a asserção de SPEC.md §F11 que
falha. A correção é do agent firebase-data.
MSG
exit 2
