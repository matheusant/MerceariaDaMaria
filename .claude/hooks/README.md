# Hooks deste projeto

Enforcement determinístico. Rules e `CLAUDE.md` são guias que o modelo pode ignorar; hook não.

**Contrato:** o script lê o JSON do evento por **stdin**. `exit 2` **bloqueia** e devolve o
stderr ao agent. `exit 0` permite.

**Parsing sem `jq`.** `jq` não está instalado nesta máquina. Os scripts extraem **só o campo
relevante** (`file_path` ou `command`) com `grep -oE` + `sed`. Nunca inspecione o JSON inteiro:
o `content` de um `Write` que mencione `.env` ou `SPEC.md` daria falso-positivo — há um teste
específico para isso.

**Windows.** Invoque sempre como `bash .claude/hooks/...`, nunca o `.sh` direto. Os scripts
normalizam as barras invertidas do JSON (`C:\\Users\\...`) para barras antes de comparar caminho.

## Mapa

| Hook | Agent | Evento | O que faz |
|---|---|---|---|
| `code-reviewer/block-writes.sh` | code-reviewer | PreToolUse `Write\|Edit\|Bash\|NotebookEdit` | Bloqueia tudo: o revisor é somente leitura |
| `firestore-rules-auditor/block-writes.sh` | firestore-rules-auditor | idem | Bloqueia tudo, inclusive o próprio `firestore.rules` — quem audita não edita |
| `core-kotlin/validate-scope.sh` | core-kotlin | PreToolUse `Write\|Edit` | Só `core/` e os testes de `core/` |
| `core-kotlin/verify.sh` | core-kotlin | Stop → SubagentStop | `core/` sem Android/Firebase, sem ponto flutuante, sem `Locale` |
| `firebase-data/validate-scope.sh` | firebase-data | PreToolUse `Write\|Edit` | Só `domain/`, `data/`, `di/`, Application, Manifest, `firestore.*`; bloqueia `google-services.json` |
| `firebase-data/verify.sh` | firebase-data | Stop | `domain/` puro, dinheiro em `Long`, `await()` com `withTimeout`, sem `add()` em transação |
| `compose-ui/validate-scope.sh` | compose-ui | PreToolUse `Write\|Edit` | Só `ui/`, `components/`, `res/`, testes de UI |
| `compose-ui/verify.sh` | compose-ui | Stop | Sem Firebase em `ui/`, dinheiro em `Long`; avisa sobre M2 e rota órfã |
| `build-engineer/validate-scope.sh` | build-engineer | PreToolUse `Write\|Edit` | Só arquivos Gradle; bloqueia `local.properties`, `wrapper/`, `google-services.json` |
| `build-engineer/verify.sh` | build-engineer | Stop | Sem coordenada Maven literal; stack congelada; plugin `google-services` aplicado |
| `test-runner/only-tests.sh` | test-runner | PreToolUse `Bash` | Allowlist de comandos de verificação; bloqueia git, `rm`, `curl`, `clean` |
| `global/protect-files.sh` | — (settings.json) | PreToolUse `Write\|Edit\|NotebookEdit` | Segredos, gerados, `SPEC.md`, `CLAUDE.md`, `google-services.json` |
| `global/no-secret-commit.sh` | — (settings.json) | PreToolUse `Bash` | Segredo no índice do git; `push --force`; `reset --hard` |

## Herança: a pegadinha

**Hooks globais (`settings.json`) NÃO são herdados por sub-agents** — só disparam na sessão
principal. Por isso a proteção de escopo está duplicada: o global cobre a sessão, e cada
`<agent>/validate-scope.sh` cobre o mesmo agent quando ele roda como sub-agent (Task tool ou
`@mention`).

**`Stop` no frontmatter de um agent é convertido em `SubagentStop`.** Todo `verify.sh` checa
`stop_hook_active` antes de qualquer coisa — sem essa guarda o hook reentra e vira loop infinito.

## Como testar um hook

```bash
printf '%s' '{"tool_input":{"file_path":"/x/SPEC.md"}}' | bash .claude/hooks/global/protect-files.sh; echo $?
# esperado: mensagem no stderr e exit 2
```

Os dois blocos de teste que validaram estes 13 scripts (34 casos, incluindo a armadilha de
falso-positivo pelo `content` e as guardas de loop) estão descritos na seção de verificação do
`PLAN.md`. Reproduza-os depois de qualquer alteração.

**Armadilha já encontrada e corrigida:** `N=$(grep -c padrao arquivo || echo 0)` produz **duas**
linhas quando não há match — `grep -c` já imprime `0` e ainda sai com código 1, então o `||`
acrescenta um segundo `0`. A comparação `[ "$N" -lt 1 ]` quebra em silêncio e o hook passa a
**aprovar tudo**. Use `N="$(grep -c padrao arquivo)"` e `${N:-0}`.
