# Segurança — vale em toda sessão

- **Nunca** commitar segredo: `*.jks`, `*.keystore`, `local.properties`, `.env`, credencial de
  service account. Um segredo commitado fica no histórico mesmo depois de removido do working tree.
- **Nunca** criar, editar ou gerar um substituto de `app/google-services.json`. É gerado por
  humano no Console do Firebase (`CLAUDE.md §3`, passo 2). Se o build falhar por causa dele,
  pare e avise — não remova o plugin `google-services` para contornar.
- **Nunca** relaxar uma regra do `firestore.rules` para fazer teste ou tela passar. Se a regra
  nega, o bug está na UI ou no repositório. A regra é a única autorização real; a ausência de um
  botão não é segurança.
- **Nunca** gravar senha em texto plano em lugar nenhum. Autenticação é do Firebase Auth; o app
  não guarda nem loga senha.
- **Nunca** logar dado pessoal: telefone, e-mail (nem o sintético), UID junto de nome, saldo de
  fiado. Log de erro leva a categoria da exceção, não o conteúdo do documento.
- **Nunca** confiar em campo enviado pelo cliente para decidir autorização. `perfil` vem de
  `usuarios/{uid}` lido pela regra, nunca do `request`.
- Cadastro grava **sempre** `perfil: "CLIENTE"`. A conta ADMIN é criada à mão no Console
  (`SPEC.md §2.3`). Nenhum caminho de código promove usuário a ADMIN.
- `git push --force`, `filter-branch` e `reset --hard` são proibidos para agents. Se for
  necessário, peça a um humano.
