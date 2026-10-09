---
paths:
  - "firestore.rules"
  - "firestore.indexes.json"
---
# Regras de segurança do Firestore

Este arquivo é a **única** autorização real do sistema. A UI não é segurança: se a única coisa
que impede uma ação é a ausência de um botão, a ação é possível.

- **Nunca relaxar uma regra para fazer teste ou tela passar.** Se dá `PERMISSION_DENIED` em uso
  legítimo, o bug está na UI ou no repositório.
- **`perfil` vem do banco, não do request.** `ehAdmin()` lê
  `usuarios/$(request.auth.uid).data.perfil`. Decidir autorização com campo que o cliente envia
  é escalada de privilégio garantida.
- **Os quatro ataques que precisam continuar fechados:**
  1. `create`/`update` em `usuarios/{uid}` com `perfil: "ADMIN"` — autopromoção.
  2. `create` em `fiados/{seuUid}/lancamentos` ou `update` em `fiados/{seuUid}.saldoCentavos` —
     o cliente zerando a própria dívida.
  3. `write` em `produtos/*` pelo cliente — mudar preço ou estoque.
  4. `get`/`list` de `usuarios/{outroUid}` ou `fiados/{outroUid}` — vazamento entre clientes.
- **`allow write` cobre create, update E delete de uma vez.** Se a intenção é só um deles,
  escreva só ele.
- **`list` em `usuarios` é restrito a ADMIN** — senão um cliente enumera todos os telefones.
- **`update` em `usuarios` exige `request.resource.data.perfil == resource.data.perfil`** —
  imutabilidade do perfil.
- **O catch-all final é obrigatório:** `match /{document=**} { allow read, write: if false; }`.
- **Só existem três coleções:** `usuarios`, `produtos`, `fiados` (com a subcoleção
  `lancamentos`). Não crie regra para coleção que não está na SPEC.
- Alterou este arquivo? Rode o `@firestore-rules-auditor` antes de publicar no Console, e registre
  o resultado das 18 asserções de `SPEC.md §F11`.
