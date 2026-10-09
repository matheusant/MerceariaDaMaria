# SPEC.md — Mercearia da Maria

> Especificação funcional e técnica. Este documento é o **contrato**: toda ambiguidade aqui
> vira decisão autônoma do agent (e provável efeito colateral). Se algo não está escrito, não
> está especificado — pergunte, não invente.
>
> Regra de ouro: **nenhuma implementação sem seção correspondente nesta SPEC.**

---

## 1. Visão geral

App Android nativo (Kotlin + Jetpack Compose) para uma mercearia de bairro, com **Firebase**
como back-end (Authentication + Cloud Firestore). Um único app, dois perfis de acesso:

- **Do cliente:** ver o catálogo com preço e disponibilidade, e consultar o próprio saldo de
  fiado sem precisar perguntar para a Maria.
- **Da Maria (dona):** cadastrar e manter os produtos com preço e quantidade, e controlar a
  caderneta de fiado — hoje um caderno de papel. A venda a prazo é montada **a partir do
  catálogo**: ela seleciona produto e quantidade, o app calcula o valor e baixa o estoque.

**Três coleções no Firestore, e somente três:** `usuarios`, `produtos`, `fiados`.

**Estado atual do código (baseline):** existe a tela de cadastro renderizada, navegação por
Navigation Compose, 9 componentes Compose reutilizáveis e o tema. Não existe back-end,
persistência, ViewModel, validação, nem nada de domínio de mercearia. Ver §9.

---

## 2. Usuários e uso

### 2.1 Cliente (`Perfil.CLIENTE`)
Morador do bairro, 25–60 anos, Android de entrada (minSdk 26), conexão instável.
Fluxo: abre o app → cadastra/entra → catálogo de produtos → consulta o próprio fiado.

### 2.2 Administradora (`Perfil.ADMIN`) — a Maria
Dona da mercearia, usa o app no balcão com cliente esperando. Prioridade é velocidade.
Fluxo: entra → produtos (cria/edita/ajusta quantidade) → caderneta de fiado (seleciona cliente,
monta a venda a prazo pelo catálogo, recebe pagamento, vê extrato).

### 2.3 Como o perfil é atribuído

- O app **nunca** cria um `ADMIN`. Todo cadastro feito pela tela de cadastro grava
  `perfil: "CLIENTE"`, e as regras de segurança do Firestore (§F11) **rejeitam** qualquer
  tentativa de gravar `"ADMIN"` ou de alterar o próprio `perfil` depois.
- A conta da Maria é criada **manualmente uma única vez no Console do Firebase** (passo de
  setup, documentado no `CLAUDE.md`):
  1. Authentication → Users → Add user → e-mail `11900000000@merceariadamaria.app`, senha à
     escolha da Maria (mín. 8 caracteres com letra e número).
  2. Firestore → coleção `usuarios` → documento com **ID igual ao UID** gerado no passo 1 →
     campos: `nome: "Maria"`, `sobrenome: "Souza"`, `telefone: "11900000000"`,
     `email: "11900000000@merceariadamaria.app"`, `perfil: "ADMIN"`, `criadoEm: <timestamp>`.
- Após o login, o `perfil` lido de `usuarios/{uid}` decide o grafo de navegação exibido (§F3).
  Não existe troca de perfil em runtime sem novo login.

---

## 3. Domínio e glossário

| Termo | Definição precisa |
|---|---|
| **Usuário** | Conta no Firebase Auth + documento em `usuarios/{uid}`. As duas coisas juntas — uma sem a outra é estado inconsistente (§F1, B5). |
| **UID** | Id do Firebase Auth. É o **ID do documento** em `usuarios` e em `fiados`. Nunca gerado pelo app. |
| **Perfil** | `CLIENTE` ou `ADMIN`. Gravado como `String` no Firestore, convertido para `enum` no domínio. Imutável após o cadastro. |
| **E-mail sintético** | E-mail que o app monta a partir do telefone para usar o provider Email/Senha do Firebase Auth: `"11987654321@merceariadamaria.app"`. O usuário nunca vê nem digita. Ver §8.1. |
| **Produto** | Item vendável: nome, preço, quantidade. Mais os campos técnicos `nomeNormalizado` e `ativo`. |
| **Quantidade** | Inteiro ≥ 0 de unidades disponíveis do produto. É o estoque. Só muda por edição na gestão (§F5) ou por venda a prazo (§F6). |
| **Produto inativo** | `ativo = false`. Ausente do catálogo do cliente e do seletor de venda; visível na gestão marcado como inativo. Nunca apagado fisicamente. |
| **Conta de fiado** | Documento `fiados/{clienteUid}` — a "conta" do cliente. Associa o cliente ao seu saldo devedor. |
| **Lançamento** | Documento em `fiados/{clienteUid}/lancamentos`. `DEBITO` = venda a prazo montada pelo catálogo (§F6); `CREDITO` = pagamento recebido (§F7). |
| **Item de lançamento** | Linha congelada dentro de um `DEBITO`: produto, nome, preço unitário e quantidade **no momento da venda**. Mudar o preço do produto depois **não** altera lançamentos antigos. |
| **Saldo** | `soma(DEBITO) − soma(CREDITO)`, em centavos. Positivo = cliente deve. Negativo = crédito a favor do cliente. Armazenado como agregado (§8.3). |
| **Preço / valor / saldo / total** | Sempre `Long` em **centavos**. Nunca `Double`/`Float` em nenhum ponto do código. |

---

## 4. Funcionalidades

Cada funcionalidade tem: comportamento, **exemplo concreto** (é o teste de especificação) e
**critério de aceitação testável**.

---

### F1 — Cadastro de cliente

**Comportamento.** A tela de cadastro existente (`SignUpScreen`) permanece com os mesmos 4
campos e o checkbox — **nenhuma mudança visual**. Passa a ter estado em `SignUpViewModel`, com
validação por campo. O botão "Criar conta" fica desabilitado enquanto o formulário for inválido.

**Sequência exata do submit:**
```
1. Valida as 5 regras localmente. Inválido → para, nada de rede.
2. isSubmitting = true
3. FirebaseAuth.createUserWithEmailAndPassword(emailSinteticoDe(telefone), senha)
4. uid = resultado.user.uid
5. usuarios/{uid}.set(mapa com perfil = "CLIENTE")
6. Se o passo 5 falhar → compensa com resultado.user.delete() e reporta erro
7. isSubmitting = false; efeito NavigateTo(Routes.CATALOGO)
```

**Regras de validação (todas obrigatórias):**

| Campo | Regra | Mensagem de erro exata |
|---|---|---|
| Nome | após `trim()`: 2–40 chars, só letras (com acento), espaço, hífen e apóstrofo | `"Informe um nome válido"` |
| Sobrenome | idem Nome | `"Informe um sobrenome válido"` |
| Telefone | exatamente 11 dígitos após remover não-dígitos; o 3º dígito deve ser `9` | `"Telefone deve ter DDD + 9 dígitos"` |
| Senha | ≥ 8 chars, ao menos 1 letra e ao menos 1 dígito | `"Senha precisa de 8+ caracteres, com letra e número"` |
| Termos | checkbox marcado | `"É preciso aceitar os termos"` |

Erros só aparecem depois de o campo ser tocado (`touched = true` no primeiro `onValueChange`),
para não pintar a tela de vermelho ao abrir.

**Contrato exato:**
```kotlin
fun emailSinteticoDe(telefone: String): String
// "11987654321"      → "11987654321@merceariadamaria.app"
// "(11) 98765-4321"  → "11987654321@merceariadamaria.app"   (só dígitos)

fun telefoneDeEmailSintetico(email: String): String
// "11987654321@merceariadamaria.app" → "11987654321"
// e-mail de outro domínio            → ""
```

**Exemplo concreto 1 — caminho feliz:**
```
Eventos:
  NomeChanged("Ana")             → nameError = null
  SobrenomeChanged("Silva")      → lastNameError = null
  TelefoneChanged("11987654321") → phoneError = null
  SenhaChanged("mercearia1")     → passwordError = null
  TermosChanged(true)            → termsError = null

SignUpUiState(name="Ana", lastName="Silva", phone="11987654321",
              password="mercearia1", termsAccepted=true,
              nameError=null, lastNameError=null, phoneError=null,
              passwordError=null, termsError=null,
              isSubmitting=false, isFormValid=true, error=null)

Submit →
  Auth: createUserWithEmailAndPassword("11987654321@merceariadamaria.app", "mercearia1")
        → uid = "abc123"
  Firestore usuarios/abc123 = {
      uid: "abc123", nome: "Ana", sobrenome: "Silva",
      telefone: "11987654321", email: "11987654321@merceariadamaria.app",
      perfil: "CLIENTE", criadoEm: <serverTimestamp>,
      termosAceitosEm: <serverTimestamp>
  }
  efeito: NavigateTo(Routes.CATALOGO)
```

**Exemplo concreto 2 — telefone curto (nenhuma chamada de rede):**
```
TelefoneChanged("1198765432")   // 10 dígitos
→ phoneError = "Telefone deve ter DDD + 9 dígitos", isFormValid = false
Submit → nada: Auth não é chamado, Firestore não é chamado, não navega.
```

**Exemplo concreto 3 — máscara ignorada:**
```
TelefoneChanged("(11) 98765-4321")
→ dígitos = "11987654321" → phoneError = null
→ grava telefone = "11987654321"   (SEMPRE só dígitos no Firestore)
```

**Exemplo concreto 4 — telefone já cadastrado:**
```
Auth já tem "11987654321@merceariadamaria.app".
Submit → FirebaseAuthUserCollisionException
→ SignUpUiState(phoneError="Este telefone já está cadastrado", isSubmitting=false)
→ nenhum documento criado, não navega.
```

**Exemplo concreto 5 — falha ao gravar o documento (compensação):**
```
Passo 3 OK (uid="abc123"). Passo 5 falha (permissão/rede).
→ chama user.delete()  para não deixar conta Auth sem documento
→ error = "Não foi possível concluir o cadastro. Tente novamente."
→ isSubmitting = false, não navega
```

**Mapeamento de exceção → mensagem (obrigatório, sem exceção genérica vazando para a UI):**

| Exceção | Mensagem exibida |
|---|---|
| `FirebaseAuthUserCollisionException` | `"Este telefone já está cadastrado"` (em `phoneError`) |
| `FirebaseAuthWeakPasswordException` | `"Senha precisa de 8+ caracteres, com letra e número"` |
| `FirebaseNetworkException` | `"Sem conexão. Verifique sua internet."` |
| `FirebaseTooManyRequestsException` | `"Muitas tentativas. Tente novamente em alguns minutos."` |
| `TimeoutCancellationException` (10 s) | `"Tempo esgotado. Verifique sua conexão."` |
| qualquer outra | `"Não foi possível concluir o cadastro. Tente novamente."` |

**Critério de aceitação.**
- [ ] `SignUpViewModel` expõe `StateFlow<SignUpUiState>`; teste unitário cobre cada uma das 5
      regras em ambos os sentidos (válido e inválido).
- [ ] `isFormValid` é `true` **se e somente se** os 5 erros são `null` e nenhum campo está vazio.
- [ ] `Submit` com `isFormValid == false` não chama Auth nem Firestore (verificado com fake).
- [ ] Telefone é gravado apenas com dígitos, qualquer que seja a máscara digitada.
- [ ] Documento gravado tem `perfil == "CLIENTE"`, sempre.
- [ ] `emailSinteticoDe` e `telefoneDeEmailSintetico` são inversas nos 3 casos do contrato.
- [ ] Cada linha da tabela de exceções tem um teste com fake que lança e verifica a mensagem.
- [ ] Falha na gravação do documento dispara `user.delete()` (verificado com fake).

---

### F2 — Login

**Comportamento.** Tela nova em `Routes.LOGIN` — a rota já está declarada em `AppNavHost.kt:17`
mas não tem `composable()` nem tela; esta feature preenche a lacuna. Campos: telefone e senha
(reaproveita `MyTextField` e `PasswordTextField`). Autentica pelo e-mail sintético.

**Mensagem de erro é sempre genérica** (`"Telefone ou senha inválidos"`) tanto para telefone
inexistente quanto para senha errada — não revelar quais telefones existem.

**Sequência exata:**
```
1. Valida telefone (11 dígitos) e senha não vazia. Inválido → para.
2. FirebaseAuth.signInWithEmailAndPassword(emailSinteticoDe(telefone), senha)
3. Lê usuarios/{uid}
4. Documento ausente → cria o mínimo (ver B5) e segue
5. Roteia por perfil: CLIENTE → Routes.CATALOGO;  ADMIN → Routes.GESTAO_HOME
```

**Exemplo concreto:**
```
Submit("11987654321", "mercearia1")  → perfil CLIENTE
→ LoginUiState(isSubmitting=false, error=null); efeito NavigateTo(Routes.CATALOGO)

Submit("11900000000", "<senha da Maria>")  → perfil ADMIN
→ efeito NavigateTo(Routes.GESTAO_HOME)

Submit("11987654321", "errada")
→ FirebaseAuthInvalidCredentialsException
→ LoginUiState(error="Telefone ou senha inválidos")

Submit("11911111111", "qualquer")     // telefone inexistente
→ FirebaseAuthInvalidUserException
→ LoginUiState(error="Telefone ou senha inválidos")   ← MESMA string

Submit("119876543", "mercearia1")     // 9 dígitos
→ LoginUiState(phoneError="Telefone deve ter DDD + 9 dígitos"); Auth não é chamado
```

**Critério de aceitação.**
- [ ] Senha correta autentica; senha errada não autentica.
- [ ] Telefone inexistente e senha errada produzem **a mesma** string de erro (teste compara).
- [ ] Destino é decidido pelo `perfil` lido do Firestore: `CLIENTE`→catálogo, `ADMIN`→gestão.
- [ ] Telefone com formato inválido não dispara chamada de rede.
- [ ] `FirebaseTooManyRequestsException` (bloqueio do próprio Firebase) exibe
      `"Muitas tentativas. Tente novamente em alguns minutos."`

---

### F3 — Sessão, roteamento por perfil e logout

**Comportamento.** O Firebase Auth já persiste a sessão em disco — **não** usar DataStore nem
SharedPreferences para isso (§8.2). Ao abrir o app, uma tela de splash decide o destino:

```
FirebaseAuth.currentUser == null              → Routes.LOGIN
currentUser != null, doc perfil = "CLIENTE"   → Routes.CATALOGO
currentUser != null, doc perfil = "ADMIN"     → Routes.GESTAO_HOME
currentUser != null, doc ausente              → cria doc mínimo (B5) → Routes.CATALOGO
currentUser != null, leitura falha por rede   → Routes.LOGIN + "Sem conexão. Verifique sua internet."
```

Logout: `FirebaseAuth.signOut()` e navega para `LOGIN` **limpando o back stack**
(`popUpTo(0) { inclusive = true }`) — o botão voltar do sistema não deve retornar à área
autenticada.

**Contrato exato:**
```kotlin
sealed interface DestinoInicial {
    data object Login : DestinoInicial
    data object Catalogo : DestinoInicial
    data object Gestao : DestinoInicial
}
suspend fun resolverDestinoInicial(): DestinoInicial
```

**Exemplo concreto:**
```
App fechado e reaberto com sessão de CLIENTE → abre no catálogo, sem pedir senha
App reaberto com sessão de ADMIN            → abre na gestão
Logout → LOGIN; pressionar voltar → o app fecha (não volta ao catálogo)
```

**Critério de aceitação.**
- [ ] Sessão sobrevive a fechar e reabrir o app (teste manual documentado + teste do resolver).
- [ ] `resolverDestinoInicial` tem teste para os 5 casos da tabela.
- [ ] Depois do logout o back stack está vazio (teste de UI Compose).
- [ ] Nenhuma ocorrência de `DataStore`/`SharedPreferences` guardando id de usuário.

---

### F4 — Catálogo de produtos (cliente)

**Comportamento.** Lista os produtos com `ativo = true`, ordenados por `nomeNormalizado`
ascendente. Campo de busca filtra por **substring** do nome, insensível a acento e caixa, com
**debounce de 300 ms**. Produto com `quantidade <= 0` aparece na lista rotulado
`"Sem estoque"`. A lista é alimentada por um **snapshot listener** (`Flow`), então mudanças
feitas pela Maria aparecem no app do cliente sem recarregar.

**A filtragem é feita em memória, no cliente** — o Firestore não faz busca por substring.
Ver §8.4.

**Contrato exato:**
```kotlin
fun normalizar(texto: String): String
// remove acentos (Normalizer NFD, descarta diacríticos), lowercase, trim
// "Açúcar Cristal " → "acucar cristal"

fun formatarBrl(centavos: Long): String
// 0         → "R$ 0,00"
// 549       → "R$ 5,49"
// 2890      → "R$ 28,90"
// 123456    → "R$ 1.234,56"
// 100000000 → "R$ 1.000.000,00"
```

**Exemplo concreto:**
```
Coleção produtos:
  Açúcar Cristal 1kg   precoCentavos=549   quantidade=12  ativo=true
  Arroz Tipo 1 5kg     precoCentavos=2890  quantidade=0   ativo=true
  Café Torrado 500g    precoCentavos=1799  quantidade=7   ativo=true
  Sabão em Pó          precoCentavos=1290  quantidade=3   ativo=false

busca = ""       → [Açúcar Cristal 1kg, Arroz Tipo 1 5kg, Café Torrado 500g]
                   (Sabão em Pó ausente: inativo)
                   Arroz exibe "Sem estoque"
busca = "aç"     → [Açúcar Cristal 1kg]
busca = "ac"     → [Açúcar Cristal 1kg]          // insensível a acento
busca = "CAFE"   → [Café Torrado 500g]           // insensível a caixa
busca = "cristal"→ [Açúcar Cristal 1kg]          // substring no meio do nome
busca = "xyz"    → []  e a tela exibe "Nenhum produto encontrado"
```

**Critério de aceitação.**
- [ ] Produtos inativos nunca aparecem na lista do cliente.
- [ ] Busca acha "Açúcar Cristal" digitando `ac`, `AC`, `açu`, `Açú` e `cristal`.
- [ ] Produto com `quantidade <= 0` é listado e rotulado "Sem estoque".
- [ ] Lista vazia mostra estado vazio com texto, não tela em branco.
- [ ] `formatarBrl` passa nos 5 casos do contrato; `normalizar` passa em 4 casos com acento.
- [ ] Alteração de preço feita na gestão reflete no catálogo sem reabrir a tela.

---

### F5 — Gestão de produtos (ADMIN)

**Comportamento.** CRUD de produto. Os campos de negócio são exatamente três — **nome,
quantidade e preço**. Excluir é **soft delete** (`ativo = false`): nunca `delete()` físico,
porque lançamentos de fiado antigos referenciam o produto e o extrato precisa continuar legível.

**Regras de validação:**

| Campo | Regra | Mensagem |
|---|---|---|
| Nome | 2–60 chars após trim; **único** entre produtos ativos, comparando `normalizar(nome)` | `"Informe o nome do produto"` / `"Já existe um produto com esse nome"` |
| Preço | inteiro em centavos > 0 | `"Preço deve ser maior que zero"` |
| Quantidade | inteiro ≥ 0 | `"Quantidade não pode ser negativa"` |

**Exemplo concreto:**
```
Criar(nome="Feijão Carioca 1kg", precoCentavos=899, quantidade=20)
→ produtos/{id} = { nome: "Feijão Carioca 1kg", nomeNormalizado: "feijao carioca 1kg",
                    precoCentavos: 899, quantidade: 20, ativo: true,
                    criadoEm: <serverTimestamp>, atualizadoEm: <serverTimestamp> }

Criar(nome="feijao carioca 1kg", precoCentavos=999, quantidade=5)
→ normalizado colide com o de cima
→ nomeError = "Já existe um produto com esse nome"; nada gravado

Criar(nome="Leite", precoCentavos=0, quantidade=10)
→ precoError = "Preço deve ser maior que zero"; nada gravado

Criar(nome="Leite", precoCentavos=599, quantidade=-1)
→ quantidadeError = "Quantidade não pode ser negativa"; nada gravado

Editar(id="p5", precoCentavos=1899)
→ atualiza precoCentavos e atualizadoEm; criadoEm inalterado

Excluir(id="p5")
→ produtos/p5.ativo = false
→ documento continua existindo; ausente do catálogo e do seletor de venda;
  visível na gestão marcado "inativo"; pode ser reativado
```

**Critério de aceitação.**
- [ ] Nome duplicado com acento/caixa diferentes é rejeitado (`"Açúcar"` vs `"acucar"`).
- [ ] Preço 0 e negativo são rejeitados; quantidade negativa é rejeitada.
- [ ] "Excluir" só marca `ativo = false`; a contagem de documentos da coleção não diminui.
- [ ] `nomeNormalizado` é sempre gravado junto e igual a `normalizar(nome)` — teste garante que
      não é possível gravar um produto com os dois campos dessincronizados.
- [ ] Editar preserva `criadoEm` e atualiza `atualizadoEm`.

---

### F6 — Lançar venda a prazo no fiado, montada pelo catálogo (ADMIN)

**Comportamento.** A Maria **não digita** produto nem preço. Ela escolhe o cliente, e o app
mostra o catálogo com um seletor de quantidade por produto. O total é calculado pelo app
(`preço × quantidade`, somado). Ao confirmar, **uma única transação Firestore** faz três coisas:
cria o lançamento, atualiza o saldo da conta de fiado e **decrementa a quantidade de cada
produto vendido**.

**Estoque nunca fica negativo.** Se algum item pedir mais do que há disponível, o lançamento
**inteiro** falha e nada é gravado — nem o lançamento, nem o saldo, nem o estoque dos outros
itens.

**Só produtos `ativo = true` aparecem no seletor.** Produto com `quantidade == 0` é exibido
rotulado `"Sem estoque"` e o botão `+` fica desabilitado. O `+` também é desabilitado ao
alcançar a quantidade disponível.

**Contrato exato:**
```kotlin
data class ItemSelecionado(
    val produtoId: String,
    val nome: String,
    val precoUnitarioCentavos: Long,   // preço exibido na tela; revalidado na transação
    val quantidade: Int,               // >= 1
    val estoqueDisponivel: Int,        // usado só para limitar o seletor na UI
)

data class NovoDebitoUiState(
    val clienteUid: String,
    val clienteNome: String,
    val busca: String = "",
    val produtos: List<Produto> = emptyList(),
    val itens: List<ItemSelecionado> = emptyList(),
    val totalCentavos: Long = 0L,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
// invariante: totalCentavos == itens.sumOf { it.precoUnitarioCentavos * it.quantidade }

suspend fun lancarDebito(
    clienteUid: String,
    itens: List<ItemSelecionado>,
): Result<Unit>

fun descreverItens(itens: List<ItemLancamento>): String
// [(nome="Café Torrado 500g", qtd=2), (nome="Açúcar Cristal 1kg", qtd=1)]
// → "2x Café Torrado 500g, 1x Açúcar Cristal 1kg"
// resultado maior que 120 chars é truncado em 119 + "…"
```

**Sequência exata da transação.** O Firestore exige que **todas as leituras venham antes de
todas as escritas** dentro de uma transação, e `runTransaction` pode reexecutar o bloco inteiro
em caso de concorrência — então as validações precisam estar dentro dele, não antes.

```
LEITURAS
  1. get fiados/{clienteUid}                      → conta (pode não existir)
  2. get usuarios/{clienteUid}                    → nome/telefone (só se a conta não existir)
  3. get produtos/{id} para cada item selecionado → preço e quantidade ATUAIS

VALIDAÇÕES (qualquer falha aborta a transação inteira, nada é gravado)
  4. itens vazio                       → "Selecione ao menos um produto"
  5. mais de 50 itens distintos        → "Máximo de 50 produtos por lançamento"
  6. produto ausente ou ativo = false  → "Produto {nome} não está mais disponível"
  7. quantidade pedida > quantidade em estoque
                                       → "Estoque insuficiente para {nome} (disponível: {n})"
  8. precoCentavos atual != precoUnitarioCentavos exibido
                                       → "O preço de {nome} mudou. Confira o total."
  9. total = soma(precoCentavos atual × quantidade)

ESCRITAS
 10. para cada item: produtos/{id}.quantidade = quantidade − pedida
 11. fiados/{clienteUid}: cria (com clienteNome/clienteTelefone denormalizados) ou atualiza;
     saldoCentavos += total;  atualizadoEm = serverTimestamp
 12. fiados/{clienteUid}/lancamentos/{novoId} = {
         tipo: "DEBITO", valorCentavos: total, itens: [...congelados...],
         descricao: descreverItens(itens), criadoEm: serverTimestamp,
         criadoPorUid: <uid da Maria>
     }
```

O id do lançamento é pré-gerado com `collection("lancamentos").document()` **antes** da
transação e gravado com `transaction.set(ref, dados)` — não existe `add()` dentro de transação.

**Exemplo concreto 1 — venda a prazo com sucesso:**
```
Cliente "abc123" (Ana Silva, 11987654321), fiados/abc123 já existe com saldoCentavos = 1348.
Produtos: Café Torrado 500g (id="p3", preco=1799, quantidade=7)
          Açúcar Cristal 1kg (id="p1", preco=549,  quantidade=12)

Seleção: Café x2, Açúcar x1
Tela exibe: Total R$ 41,47      (1799*2 + 549*1 = 4147)

Confirmar →
  produtos/p3.quantidade: 7  → 5
  produtos/p1.quantidade: 12 → 11
  fiados/abc123.saldoCentavos: 1348 → 5495
  fiados/abc123/lancamentos/{id} = {
      tipo: "DEBITO", valorCentavos: 4147,
      itens: [
        { produtoId: "p3", nomeProduto: "Café Torrado 500g",
          precoUnitarioCentavos: 1799, quantidade: 2, subtotalCentavos: 3598 },
        { produtoId: "p1", nomeProduto: "Açúcar Cristal 1kg",
          precoUnitarioCentavos: 549,  quantidade: 1, subtotalCentavos: 549 }
      ],
      descricao: "2x Café Torrado 500g, 1x Açúcar Cristal 1kg",
      criadoEm: <serverTimestamp>, criadoPorUid: "<uid da Maria>"
  }
```

**Exemplo concreto 2 — estoque insuficiente aborta tudo:**
```
Café quantidade = 1;  Açúcar quantidade = 12.  Seleção: Café x2, Açúcar x1.
Confirmar →
  nenhum lançamento criado
  fiados/abc123.saldoCentavos INALTERADO (1348)
  produtos/p3.quantidade INALTERADO (1)
  produtos/p1.quantidade INALTERADO (12)        ← nem o Açúcar baixou
  error = "Estoque insuficiente para Café Torrado 500g (disponível: 1)"
  a seleção da tela é preservada, para a Maria ajustar
```

**Exemplo concreto 3 — preço mudou entre abrir a tela e confirmar:**
```
Tela montada com Café a R$ 17,99. Enquanto a Maria escolhia, o preço foi para R$ 18,99.
Confirmar →
  nada gravado
  error = "O preço de Café Torrado 500g mudou. Confira o total."
  a tela recarrega os preços e recalcula o total
```

**Exemplo concreto 4 — primeira compra do cliente (conta ainda não existe):**
```
fiados/def456 não existe. Cliente Bruno Lima, telefone 11955554444. Seleção: Açúcar x1.
Confirmar →
  cria fiados/def456 = { clienteUid: "def456", clienteNome: "Bruno Lima",
                         clienteTelefone: "11955554444", saldoCentavos: 549,
                         atualizadoEm: <serverTimestamp> }
  cria o lançamento; produtos/p1.quantidade: 12 → 11
```

**Exemplo concreto 5 — seleção vazia:**
```
itens = []  → error = "Selecione ao menos um produto"; botão confirmar desabilitado;
              nenhuma transação iniciada
```

**Critério de aceitação.**
- [ ] `totalCentavos` é sempre a soma exata de `preço × quantidade` — teste com 3 itens.
- [ ] Selecionar o mesmo produto duas vezes soma a quantidade e mantém **uma** linha.
- [ ] Quantidade 0 remove o item da seleção.
- [ ] O seletor não permite passar da quantidade disponível (`+` desabilitado no limite).
- [ ] Produtos inativos não aparecem no seletor.
- [ ] Lançamento decrementa a quantidade de **cada** produto exatamente pelo pedido.
- [ ] Estoque insuficiente em **um** item deixa lançamento, saldo e **todos** os estoques
      intocados — teste verifica os 4 documentos.
- [ ] Preço divergente aborta com a mensagem exata e nada é gravado.
- [ ] Estoque nunca fica negativo em nenhum cenário.
- [ ] Conta de fiado é criada na mesma transação quando não existe, com nome e telefone.
- [ ] `itens` gravados são snapshot: alterar o preço do produto depois **não** muda o
      `valorCentavos` nem os `itens` do lançamento já criado.
- [ ] `descreverItens` produz exatamente `"2x Café Torrado 500g, 1x Açúcar Cristal 1kg"` e
      trunca em 120 chars.
- [ ] `valorCentavos` gravado é positivo.

---

### F7 — Receber pagamento / lançar crédito (ADMIN)

**Comportamento.** Pagamento é dinheiro, não produto: a Maria **digita** o valor recebido e,
opcionalmente, uma observação de até 120 caracteres. Um botão **"Quitar tudo"** preenche o campo
com o saldo atual (habilitado só quando `saldoCentavos > 0`). Crédito **não** mexe em estoque.

**Contrato exato:**
```kotlin
suspend fun lancarCredito(
    clienteUid: String,
    valorCentavos: Long,      // > 0
    observacao: String,       // 0..120 chars, pode ser vazia
): Result<Unit>
```

Transação: lê `fiados/{clienteUid}`, escreve `saldoCentavos -= valorCentavos` e cria o
lançamento com `tipo = "CREDITO"`, `itens = []` e `descricao = observacao.ifBlank { "Pagamento recebido" }`.

**Exemplo concreto:**
```
fiados/abc123.saldoCentavos = 5495

lancarCredito("abc123", 2000, "Pagamento em dinheiro")
→ saldoCentavos = 3495
→ lancamentos/{id} = { tipo: "CREDITO", valorCentavos: 2000, itens: [],
                       descricao: "Pagamento em dinheiro", criadoEm: ..., criadoPorUid: ... }

lancarCredito("abc123", 3495, "")        // botão "Quitar tudo"
→ saldoCentavos = 0; descricao = "Pagamento recebido"
→ cliente SAI da lista de devedores

lancarCredito("abc123", 5000, "")  com saldo 3495
→ aceito; saldoCentavos = -1505 (crédito a favor do cliente); segue fora da lista

lancarCredito("abc123", 0, "")     → Result.failure "Informe um valor maior que zero"; nada gravado
lancarCredito("abc123", -500, "")  → Result.failure, mesma mensagem; nada gravado

Conta de fiado inexistente → Result.failure "Este cliente não tem conta de fiado"
```

**Critério de aceitação.**
- [ ] `CREDITO` subtrai do `saldoCentavos`.
- [ ] Valor ≤ 0 é rejeitado antes de qualquer escrita.
- [ ] `valorCentavos` gravado é **positivo** — o sinal vem do `tipo`, nunca do valor.
- [ ] Observação vazia grava `"Pagamento recebido"`.
- [ ] "Quitar tudo" preenche exatamente o saldo atual e fica desabilitado com saldo ≤ 0.
- [ ] Crédito não altera nenhum documento em `produtos` (teste verifica).
- [ ] Pagamento maior que o saldo é aceito e deixa o saldo negativo.

---

### F8 — Caderneta: devedores e extrato (ADMIN)

**Comportamento.** Lista os clientes com `saldoCentavos > 0`, maior saldo primeiro, mostrando
nome, telefone e saldo formatado. Abrir um cliente mostra o extrato — lançamentos por
`criadoEm` desc — e dá acesso a lançar débito (§F6) e crédito (§F7). Cada `DEBITO` do extrato
pode ser expandido para ver os itens congelados.

Para escolher o cliente de um novo débito, a Maria busca em `usuarios` por nome ou telefone
(`perfil = "CLIENTE"`), inclusive clientes que ainda não têm conta de fiado.

**Exemplo concreto:**
```
fiados: abc123 (Ana Silva, saldo 5495), def456 (Bruno Lima, saldo 549), ghi789 (saldo 0)

Lista de devedores →
  [Ana Silva  11987654321  R$ 54,95,
   Bruno Lima 11955554444  R$ 5,49]
  (ghi789 ausente: saldo == 0)

Extrato de abc123 (criadoEm desc) →
  [CREDITO 2000 "Pagamento em dinheiro",
   DEBITO  4147 "2x Café Torrado 500g, 1x Açúcar Cristal 1kg",
   DEBITO  1348 "..."]
  Expandir o DEBITO 4147 → 2x Café Torrado 500g  R$ 35,98
                           1x Açúcar Cristal 1kg R$ 5,49

Nenhum devedor → "Ninguém está devendo agora"
```

**Critério de aceitação.**
- [ ] Lista mostra apenas `saldoCentavos > 0`, ordenada desc.
- [ ] Extrato ordenado por `criadoEm` desc.
- [ ] `DEBITO` expandido mostra os itens congelados com subtotal formatado.
- [ ] Busca de cliente encontra por nome e por telefone, e inclui cliente sem conta de fiado.
- [ ] Estado vazio explícito quando não há devedores.

---

### F9 — Meu fiado (cliente)

**Comportamento.** O cliente logado vê o próprio saldo formatado e o próprio extrato. Leitura
apenas — o cliente **não** pode lançar nada (garantido por regra de segurança, §F11, não só
pela ausência de botão).

**Exemplo concreto:**
```
Cliente "abc123" com saldoCentavos = 3495
→ "Você deve R$ 34,95" + extrato

Cliente "abc123" com saldoCentavos = 0      → "Você está em dia" + extrato
Cliente "abc123" com saldoCentavos = -1505  → "Você tem R$ 15,05 de crédito"
Cliente "def456" sem documento em fiados    → "Você está em dia", extrato vazio,
                                              SEM erro e SEM crash
```

**Critério de aceitação.**
- [ ] Cliente sem conta de fiado vê "em dia", não erro.
- [ ] Os 3 textos (deve / em dia / crédito) são escolhidos pelo sinal do saldo — teste unitário.
- [ ] Cliente A não consegue ler o fiado do cliente B (teste das regras, §F11).
- [ ] A tela não tem nenhuma ação de escrita.

---

### F10 — Termos de uso e política de privacidade

**Comportamento.** Preserva o comportamento atual: os links no texto do cadastro navegam para
`Routes.TERMS_AND_CONDITIONS` e `Routes.PRIVACY_POLITICS`. As duas telas hoje têm apenas um
título; passam a ter conteúdo real de texto rolável e botão de voltar que retorna ao cadastro
**sem perder o que já foi digitado** (o `SignUpViewModel` sobrevive por estar no escopo do
grafo de navegação, não da tela).

**Exemplo concreto:**
```
No cadastro: nome="Ana", telefone="11987654321" digitados
Toca em "Termos de uso" → TermsAndConditionsScreen
Toca em voltar          → SignUpScreen com nome="Ana" e telefone="11987654321" preservados
```

**Critério de aceitação.**
- [ ] Ambas as telas são alcançáveis pelos links do cadastro (teste de UI Compose).
- [ ] Voltar preserva o estado do formulário.
- [ ] Conteúdo é rolável em tela pequena, sem cortar texto.
- [ ] O typo `"Politícas de privacidade"` em `PrivacyPoliticsScreen.kt:19` é corrigido para
      `"Política de privacidade"` e a string vai para `strings.xml`.

---

### F11 — Regras de segurança do Firestore

**Comportamento.** As regras são um artefato versionado (`firestore.rules`) e são a **única**
garantia real de autorização — a ausência de um botão na UI não é segurança. Escrita exigida:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function autenticado() {
      return request.auth != null;
    }
    function ehAdmin() {
      return autenticado()
        && get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.perfil == 'ADMIN';
    }

    match /usuarios/{uid} {
      allow get:    if autenticado() && (request.auth.uid == uid || ehAdmin());
      allow list:   if ehAdmin();
      allow create: if autenticado()
                    && request.auth.uid == uid
                    && request.resource.data.perfil == 'CLIENTE'
                    && request.resource.data.uid == uid
                    && request.resource.data.telefone is string
                    && request.resource.data.telefone.size() == 11;
      allow update: if autenticado()
                    && request.auth.uid == uid
                    && request.resource.data.perfil == resource.data.perfil;
      allow delete: if false;
    }

    match /produtos/{produtoId} {
      allow read:  if autenticado();
      allow write: if ehAdmin();
    }

    match /fiados/{clienteUid} {
      allow read:  if autenticado() && (request.auth.uid == clienteUid || ehAdmin());
      allow write: if ehAdmin();

      match /lancamentos/{lancamentoId} {
        allow read:  if autenticado() && (request.auth.uid == clienteUid || ehAdmin());
        allow write: if ehAdmin();
      }
    }

    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

**Nota importante.** A transação de F6 escreve em `produtos` **e** em `fiados` — as duas exigem
`ehAdmin()`. Cada avaliação de `ehAdmin()` faz um `get()` em `usuarios`, que conta como leitura
faturada e entra no limite de 10 `get()` por avaliação de regra. Com o teto de 50 itens por
lançamento (§F6, validação 5), o limite não é atingido, porque as regras são avaliadas **por
documento escrito**, não uma vez para a transação inteira.

**Exemplo concreto (comportamento verificável):**
```
Cliente autenticado uid=A:
  ler  usuarios/A                 → permitido
  ler  usuarios/B                 → NEGADO
  criar usuarios/A perfil=CLIENTE → permitido
  criar usuarios/A perfil=ADMIN   → NEGADO           ← não se autopromove
  atualizar usuarios/A mudando perfil para ADMIN → NEGADO
  ler  produtos/*                 → permitido
  criar/editar produtos/*         → NEGADO           ← não muda preço nem estoque
  ler  fiados/A                   → permitido
  ler  fiados/B                   → NEGADO
  criar fiados/A/lancamentos      → NEGADO           ← não zera a própria dívida
  editar fiados/A.saldoCentavos   → NEGADO
  deletar usuarios/A              → NEGADO

ADMIN autenticado:
  listar usuarios                     → permitido
  criar/editar/excluir produtos       → permitido
  escrever fiados/* e lancamentos/*   → permitido

Não autenticado:
  qualquer leitura ou escrita → NEGADO
```

**Critério de aceitação.**
- [ ] `firestore.rules` existe na raiz do repositório e está commitado.
- [ ] Cliente não consegue gravar `perfil: "ADMIN"` (verificado no Rules Playground do Console,
      com o resultado registrado).
- [ ] Cliente não consegue criar lançamento nem alterar o saldo do próprio fiado.
- [ ] Cliente não consegue escrever em `produtos` (nem preço, nem quantidade).
- [ ] Cliente não consegue ler dados de outro cliente.
- [ ] Usuário não autenticado não lê nada.
- [ ] A regra catch-all final nega tudo que não foi explicitamente permitido.

---

### F12 — Rede, offline e erros

**Comportamento.** O app adota uma estratégia **offline-first**. O banco de dados local **Room** já foi implementado para persistência e acesso offline imediato aos dados, servindo como fonte local. O **WorkManager** será implementado posteriormente para gerenciar a sincronização em segundo plano das alterações com o **Cloud Firestore**.

**Persistência é habilitada explicitamente na inicialização** (uma vez, antes do primeiro uso):
```kotlin
FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings {
    isPersistenceEnabled = true
}
```

**Armadilha que a SPEC obriga a tratar:** offline, o `Task` de uma escrita **não completa** até
reconectar — um `await()` fica pendurado para sempre. Portanto:

- Toda chamada suspensa a Auth ou Firestore é envolvida em `withTimeout(10_000)`.
- Para **escritas simples**, timeout **não** é erro: a mutação já está aplicada no cache local e
  será sincronizada. A UI mostra `"Salvo. Será sincronizado quando houver conexão."`
- **Exceção crítica: transações não funcionam offline.** `runTransaction` precisa do servidor —
  não há caminho local. Logo os lançamentos de fiado (F6 e F7) **falham** offline com
  `"Sem conexão. O lançamento precisa de internet."` e a seleção da tela é preservada.
- Para **leituras** e para **Auth** (que não tem cache offline), timeout é erro:
  `"Sem conexão. Verifique sua internet."`

**Exemplo concreto:**
```
Offline, ADMIN salva produto novo (escrita simples)
→ enfileirada; catálogo local já mostra o produto
→ "Salvo. Será sincronizado quando houver conexão."
→ ao reconectar, sincroniza sozinho

Offline, ADMIN tenta lançar débito no fiado (transação)
→ FALHA: "Sem conexão. O lançamento precisa de internet."
→ nada é gravado; a seleção de produtos é preservada na tela

Offline, cliente abre o catálogo (já visitado antes)
→ lista servida do cache + aviso "Exibindo dados salvos no aparelho"

Offline, cliente abre o catálogo pela primeira vez (cache vazio)
→ estado vazio + "Sem conexão. Verifique sua internet." + botão "Tentar de novo"

Offline, tentativa de login
→ Auth não funciona offline → "Sem conexão. Verifique sua internet."
  (nunca fica girando para sempre)
```

**Critério de aceitação.**
- [ ] `isPersistenceEnabled = true` é configurado uma única vez, antes de qualquer uso do
      Firestore (o Firestore lança `IllegalStateException` se as settings mudarem após o uso).
- [ ] Nenhuma chamada de rede sem `withTimeout` — verificado por hook/grep no code review.
- [ ] Nenhum indicador de carregamento fica indefinidamente ativo: todo `isLoading = true` tem
      caminho garantido para `false` (sucesso, erro ou timeout).
- [ ] Timeout de escrita simples produz mensagem de sucesso-diferido, não de erro.
- [ ] Falha de transação offline produz erro explícito e preserva a seleção da tela.
- [ ] Timeout de leitura e de Auth produzem `"Sem conexão. Verifique sua internet."`

---

## 5. Schema de dados (contratos exatos)

Cloud Firestore, modo nativo. **Três coleções de topo, e somente três.**

```kotlin
enum class Perfil { CLIENTE, ADMIN }
enum class TipoLancamento { DEBITO, CREDITO }
```

### 5.1 `usuarios/{uid}` — ID do documento = UID do Firebase Auth

| Campo | Tipo Firestore | Regra |
|---|---|---|
| `uid` | String | igual ao ID do documento (redundante, exigido pela regra de `create`) |
| `nome` | String | 2–40 chars |
| `sobrenome` | String | 2–40 chars |
| `telefone` | String | exatamente 11 dígitos, só dígitos |
| `email` | String | e-mail sintético: `"<telefone>@merceariadamaria.app"` |
| `perfil` | String | `"CLIENTE"` ou `"ADMIN"` |
| `criadoEm` | Timestamp | `FieldValue.serverTimestamp()` |
| `termosAceitosEm` | Timestamp | `FieldValue.serverTimestamp()` |

### 5.2 `produtos/{produtoId}` — ID gerado pelo Firestore

| Campo | Tipo | Regra |
|---|---|---|
| `nome` | String | 2–60 chars após trim |
| `nomeNormalizado` | String | `normalizar(nome)` — busca e unicidade |
| `precoCentavos` | Number (Long) | > 0 |
| `quantidade` | Number (Long) | ≥ 0 — decrementado pela transação de F6 |
| `ativo` | Boolean | `false` = soft delete |
| `criadoEm` | Timestamp | serverTimestamp, imutável |
| `atualizadoEm` | Timestamp | serverTimestamp a cada escrita |

### 5.3 `fiados/{clienteUid}` — ID do documento = UID do cliente

| Campo | Tipo | Regra |
|---|---|---|
| `clienteUid` | String | igual ao ID do documento |
| `clienteNome` | String | denormalizado de `usuarios` (§8.3) |
| `clienteTelefone` | String | denormalizado de `usuarios` |
| `saldoCentavos` | Number (Long) | agregado; pode ser negativo |
| `atualizadoEm` | Timestamp | serverTimestamp |

### 5.4 `fiados/{clienteUid}/lancamentos/{lancamentoId}` — subcoleção

| Campo | Tipo | Regra |
|---|---|---|
| `tipo` | String | `"DEBITO"` ou `"CREDITO"` |
| `valorCentavos` | Number (Long) | **> 0 sempre**; o sinal vem do `tipo` |
| `itens` | Array de Map | itens congelados; **vazio** quando `tipo = "CREDITO"` |
| `descricao` | String | 0–120 chars. Em `DEBITO`, gerada por `descreverItens`. Em `CREDITO`, a observação ou `"Pagamento recebido"` |
| `criadoEm` | Timestamp | serverTimestamp |
| `criadoPorUid` | String | UID de quem lançou (a Maria) |

Cada elemento de `itens` (snapshot imutável — mudar o produto depois não altera isto):

| Campo | Tipo | Regra |
|---|---|---|
| `produtoId` | String | referência informativa; sem integridade referencial |
| `nomeProduto` | String | nome no momento da venda |
| `precoUnitarioCentavos` | Number (Long) | preço no momento da venda |
| `quantidade` | Number (Long) | ≥ 1 |
| `subtotalCentavos` | Number (Long) | `precoUnitarioCentavos × quantidade` |

Invariantes: `valorCentavos == itens.sumOf { subtotalCentavos }` quando `tipo = "DEBITO"`;
`itens.isEmpty()` quando `tipo = "CREDITO"`.

### 5.5 Modelos de domínio

A UI **nunca** vê `DocumentSnapshot` nem `Map<String, Any>`. Camada `domain` com data classes
puras (`Usuario`, `Produto`, `ContaFiado`, `Lancamento`, `ItemLancamento`) e mappers na camada
`data` (`DocumentSnapshot.toUsuario()`, `Produto.toMap()`, …). Mapper de leitura **nunca lança**:
documento com campo faltando ou de tipo errado retorna `null` e é descartado da lista, com log.

### 5.6 Índices compostos exigidos

| Coleção | Campos | Usado por |
|---|---|---|
| `produtos` | `ativo` asc, `nomeNormalizado` asc | catálogo do cliente (F4) e seletor de venda (F6) |
| `fiados` | `saldoCentavos` desc | lista de devedores (F8) |
| `usuarios` | `perfil` asc, `nome` asc | busca de cliente para lançar débito (F8) |

Ficam em `firestore.indexes.json`, versionado.

### 5.7 Rotas (estender o `object Routes` existente em `ui/navigation/AppNavHost.kt`)

```kotlin
const val SPLASH = "splash"                            // NOVO: resolve o destino inicial (F3)
const val LOGIN = "login"                              // já declarada, hoje sem tela
const val SIGN_UP = "sign_up"
const val TERMS_AND_CONDITIONS = "terms_and_conditions"
const val PRIVACY_POLITICS = "privacy_politics"
const val CATALOGO = "catalogo"                        // cliente
const val MEU_FIADO = "meu_fiado"                      // cliente
const val GESTAO_HOME = "gestao"                       // admin
const val GESTAO_PRODUTOS = "gestao/produtos"
const val GESTAO_PRODUTO_FORM = "gestao/produtos/form?produtoId={produtoId}"
const val GESTAO_FIADO = "gestao/fiado"                // lista de devedores
const val GESTAO_FIADO_EXTRATO = "gestao/fiado/{clienteUid}"
const val GESTAO_NOVO_DEBITO = "gestao/fiado/{clienteUid}/debito"   // seletor do catálogo (F6)
const val GESTAO_NOVO_CREDITO = "gestao/fiado/{clienteUid}/credito" // pagamento (F7)
const val GESTAO_BUSCAR_CLIENTE = "gestao/clientes"                 // escolher cliente (F8)
```

---

## 6. Casos de borda (comportamento obrigatório quando dá erro)

| # | Situação | Comportamento exigido |
|---|---|---|
| B1 | Campo com só espaços (`"   "`) | Tratado como vazio: erro de obrigatório |
| B2 | Telefone com máscara, letras ou `+55` | Só dígitos contam; `+5511987654321` = 13 dígitos → inválido |
| B3 | Cadastro com telefone já existente | `FirebaseAuthUserCollisionException` → `phoneError` específico; nada gravado |
| B4 | Auth cria a conta mas a gravação do documento falha | Compensa com `user.delete()`; erro exibido; não navega |
| B5 | Login OK mas `usuarios/{uid}` não existe (B4 com `delete()` também falhando) | O app **cria** o documento mínimo: `uid`, `telefone` derivado do e-mail sintético, `nome`/`sobrenome` vazios, `perfil: "CLIENTE"`. Segue para o catálogo. Nunca deixa o usuário travado no deadlock "já cadastrado" + "cadastro incompleto" |
| B6 | Sem conexão em leitura com cache preenchido | Serve do cache + aviso "Exibindo dados salvos no aparelho" |
| B7 | Sem conexão em leitura com cache vazio | Estado vazio + "Sem conexão. Verifique sua internet." + "Tentar de novo" |
| B8 | Sem conexão em escrita simples | Enfileira; "Salvo. Será sincronizado quando houver conexão." (não é erro) |
| B9 | Sem conexão em **transação** (F6/F7) | **Falha**: transação exige servidor. "Sem conexão. O lançamento precisa de internet." Seleção preservada |
| B10 | Sem conexão no login/cadastro | Auth não tem cache: "Sem conexão. Verifique sua internet." |
| B11 | `await()` pendurado offline | `withTimeout(10_000)` em toda chamada; nenhum loading infinito |
| B12 | Regra de segurança nega a operação | `PERMISSION_DENIED` → "Você não tem permissão para esta ação." **e log**, porque é sintoma de bug de UI expondo ação indevida |
| B13 | Documento com campo faltando ou tipo errado | Mapper retorna `null`, item descartado da lista, log emitido; **nunca crash** |
| B14 | Estoque insuficiente em qualquer item do lançamento | Transação inteira aborta: nenhum estoque baixa, saldo e lançamento intocados |
| B15 | Preço do produto mudou entre montar a tela e confirmar | Aborta com "O preço de {nome} mudou. Confira o total."; a tela recarrega |
| B16 | Produto ficou inativo entre montar a tela e confirmar | Aborta com "Produto {nome} não está mais disponível" |
| B17 | Produto vendido em lançamento antigo é excluído (soft delete) | Extrato continua legível: `itens` são snapshot, não referência |
| B18 | Mais de 50 itens distintos num lançamento | Rejeitado: "Máximo de 50 produtos por lançamento" (folga contra o limite de 500 escritas por transação) |
| B19 | Duas vendas simultâneas do mesmo produto | `runTransaction` reexecuta o bloco e revalida o estoque; a segunda falha se o estoque acabou. Estoque nunca fica negativo |
| B20 | Preço/quantidade digitados como texto não numérico | Campo não aceita o caractere; o estado nunca recebe valor inválido |
| B21 | Valor de crédito 0 ou negativo | Rejeitado antes de qualquer escrita |
| B22 | Pagamento maior que o saldo | Aceito; saldo fica negativo; cliente sai da lista de devedores |
| B23 | Crédito em cliente sem conta de fiado | "Este cliente não tem conta de fiado"; nada gravado |
| B24 | Rotação de tela / mudança de configuração | Estado sobrevive: vive no `ViewModel`, nunca em `remember` de composable |
| B25 | Duplo toque rápido em "Criar conta" / "Salvar" / "Lançar" | `isSubmitting = true` bloqueia reentrada; nunca dois documentos |
| B26 | Cliente tenta se autopromover a ADMIN por qualquer caminho | Negado pela regra de `create` **e** de `update` (§F11) |
| B27 | `google-services.json` ausente | O build **falha** com erro do plugin `google-services`. É pré-requisito de setup, documentado no `CLAUDE.md` |
| B28 | Overflow de valor monetário | `Long` em centavos; limite prático inalcançável. **Proibido** usar `Int` para valor, total ou saldo |

---

## 7. Stack e dependências

**Já no projeto** (fonte única de versões: `gradle/libs.versions.toml`):

| Dependência | Versão |
|---|---|
| AGP | 8.6.0 |
| Kotlin | 1.9.0 |
| Compose Compiler Extension | 1.5.1 |
| Compose BOM | 2024.04.01 |
| androidx.compose.material (M2) | 1.6.8 |
| navigation-compose | 2.9.4 |
| core-ktx | 1.13.1 |
| lifecycle-runtime-ktx | 2.8.4 |
| activity-compose | 1.9.1 |
| JUnit 4 | 4.13.2 |
| androidx.test.ext:junit | 1.2.1 |
| espresso-core | 3.6.1 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 26 |
| jvmTarget | 1.8 |

**A adicionar** (nenhuma outra dependência além destas sem aprovação explícita):

| Dependência | Versão | Para quê |
|---|---|---|
| `com.google.gms.google-services` (plugin) | 4.4.2 | processa o `google-services.json` |
| `com.google.firebase:firebase-bom` | 33.1.2 | alinha as versões do Firebase |
| `com.google.firebase:firebase-auth` | via BOM | autenticação (F1, F2, F3) |
| `com.google.firebase:firebase-firestore` | via BOM | as 3 coleções (F4–F9) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-play-services` | 1.8.1 | `.await()` em `Task<T>` do Firebase |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.8.4 | `viewModel()` em Compose |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.8.1 | `runTest`, `TestDispatcher` |
| `androidx.arch.core:core-testing` | 2.2.0 | utilitários de teste de arquitetura |
| `androidx.room` (runtime, ktx, compiler) | 2.6.1 | Persistência local offline-first |
| `androidx.work:work-runtime-ktx` | 2.9.0 | Sincronização em segundo plano (A implementar) |

**Removido do escopo** (esteve em versões anteriores desta SPEC): Robolectric,
DataStore. O Auth já persiste sessão.

**Proibido sem aprovação:** Retrofit/OkHttp/Ktor, Hilt/Koin (quando não adotado), Firebase Analytics/Crashlytics/Messaging/Storage, Compose Material 2 em código novo,
bibliotecas de imagem remota, mocking framework (usar fakes escritos à mão).

**Pré-requisitos de setup** (ações humanas, não de código — sem elas o build falha):
1. Criar o projeto no Console do Firebase e registrar o app Android com o package
   `com.example.merceariadamaria`.
2. Baixar `google-services.json` e colocar em `app/google-services.json`.
3. Habilitar o provider **Email/Password** em Authentication → Sign-in method.
4. Criar o Firestore em modo produção e publicar o `firestore.rules` do §F11 e os índices do §5.6.
5. Criar a conta ADMIN da Maria conforme §2.3.

**Nota de build.** Se o AGP acusar necessidade de desugaring de APIs Java 8+ ao adicionar o
Firebase, habilitar em `app/build.gradle.kts`:
`compileOptions { isCoreLibraryDesugaringEnabled = true }` +
`coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")`. Não mudar `jvmTarget` nem
`sourceCompatibility` sem aprovação — é mudança de stack.

---

## 8. Decisões de projeto (o "porquê" registrado, para ninguém reabrir)

### 8.1 E-mail sintético em vez de Phone Auth
O Firebase Auth **não tem** provider "telefone + senha". Tem Email/Senha, ou Telefone via SMS
OTP (que dispensa senha). A tela de cadastro existente coleta telefone **e** senha.
**Decisão:** usar o provider Email/Senha com um e-mail montado pelo app
(`"<telefone>@merceariadamaria.app"`), invisível para o usuário.
- Ganho: a tela fica **exatamente** como está; sem SMS, sem SHA-1, sem plano Blaze, sem custo.
- Custo aceito: não existe "esqueci minha senha" — o domínio não recebe e-mail, então o link de
  redefinição não chega a ninguém. Fica em §10.
- Consequência: o domínio é uma constante única no código (`AuthConstants.DOMINIO_SINTETICO`).
  Mudá-lo invalida todas as contas existentes.

### 8.2 Sessão é do Firebase Auth, não do app
`FirebaseAuth` já persiste a sessão em disco e a renova. Guardar o UID em DataStore em paralelo
cria duas fontes de verdade que divergem no logout. **Decisão:** `FirebaseAuth.currentUser` é a
única fonte. DataStore saiu da stack.

### 8.3 Saldo de fiado é agregado armazenado, não calculado
No Firestore, calcular o saldo somando a subcoleção custa uma leitura por lançamento — a lista
de devedores viraria centenas de leituras a cada abertura. **Decisão:** `saldoCentavos` é
mantido em `fiados/{clienteUid}` e atualizado na **mesma transação** do lançamento. Nome e
telefone do cliente também são denormalizados ali, para a lista de devedores não fazer uma
leitura extra em `usuarios` por linha.
Risco assumido: se o cliente mudar de nome, o denormalizado fica velho. Não há tela de editar
perfil nesta entrega, então o risco não se materializa (§10).

### 8.4 Busca é filtrada no cliente, não no Firestore
O Firestore não faz busca por substring — só por prefixo. Buscar `"cristal"` e achar
`"Açúcar Cristal 1kg"` é impossível server-side. **Decisão:** o catálogo de uma mercearia de
bairro é pequeno (centenas de itens); o app carrega os produtos ativos via snapshot listener e
filtra em memória com `normalizar()`. Se a coleção passar de ~1.000 produtos, esta decisão
precisa ser revisada (paginação ou serviço de busca) — mas isso está em §10.

### 8.5 A venda a prazo é a única baixa de estoque automática do app
Não há pedidos nem carrinho (§8.10). **Decisão:** o lançamento de `DEBITO` (§F6) é montado a
partir do catálogo e decrementa a `quantidade` de cada produto na mesma transação. Sem isso, o
campo `quantidade` só mudaria por edição manual e não representaria o estoque real.
Consequência assumida: vendas pagas à vista no balcão **não** passam pelo app, então o estoque
delas continua sendo ajustado à mão na tela de produtos (§F5). Registrar venda à vista está em
§10.

### 8.6 Preço é congelado no lançamento, e divergência aborta
Os `itens` do lançamento guardam `precoUnitarioCentavos` — mudar o preço do produto depois não
altera o histórico. E se o preço mudar **entre** a Maria montar a tela e confirmar, a transação
**aborta** em vez de gravar silenciosamente um total diferente do que ela viu na tela. Gravar um
valor que a pessoa não conferiu é pior do que pedir para conferir de novo.

### 8.7 Dinheiro é `Long` em centavos
`Double` para dinheiro produz `0.1 + 0.2 != 0.3`. Nenhum ponto flutuante em preço, valor, total
ou saldo. Formatação só na borda da UI, via `formatarBrl`.

### 8.8 Injeção de dependência manual
Sem Hilt. Um `AppContainer` construído na classe `Application` expõe `FirebaseAuth`,
`FirebaseFirestore` e os repositórios; os `ViewModel` recebem repositórios via
`ViewModelProvider.Factory`. Motivo: o projeto é pequeno e Hilt + KSP + Kotlin 1.9 adiciona
superfície de build sem retorno. Efeito colateral desejado: os repositórios são interfaces e os
testes usam fakes — nenhuma chamada real ao Firebase em teste unitário.

### 8.9 `dynamicColor` deve ser desligado
`ui/theme/Theme.kt:40` tem `dynamicColor: Boolean = true`, o que faz o Android 12+ substituir a
paleta do app pelo papel de parede do usuário — a identidade visual desaparece no device da
maioria dos usuários. **Decisão:** passar o default para `false`. Mudança visual deliberada.

### 8.10 Estado de UI vive no ViewModel
Nenhum dado de formulário ou de domínio em `remember { mutableStateOf(...) }` dentro de
composable. Componentes são *stateless* e recebem `value` + `onValueChange` (state hoisting).
Motivo: hoje o valor digitado nem chega à tela (§9.1), e `remember` não sobrevive à rotação.

### 8.11 Material 3 é o padrão
O projeto mistura `androidx.compose.material` (M2, usado só por `Divider` e `ClickableText`)
com Material 3. Código novo usa **apenas** M3; os dois usos de M2 são substituídos por
`HorizontalDivider` (M3) e `Text` com `LinkAnnotation` — padrão já adotado em
`ClickableTermsPoliticsTextComponent`.

### 8.12 Sem pedidos nem carrinho para o cliente
Decisão de escopo do dono do produto: as três coleções são `usuarios`, `produtos` e `fiados`.
O cliente **consulta** catálogo e fiado; não monta pedido pelo app. Quem monta uma cesta é a
Maria, e o resultado é um lançamento de fiado (§F6), não um pedido. Fica em §10.

---

## 9. Baseline: o que existe hoje e o que está quebrado

Documentado porque é o contrato de partida — e porque vários destes itens são bugs reais que a
SPEC exige corrigir.

**Funciona hoje:**
- `MainActivity` → `TrocoTheme` → `MerceariaApp()` (NavHost) com 3 destinos;
  `startDestination = Routes.SIGN_UP`.
- `SignUpScreen` renderiza todos os campos; os links de termos e política navegam corretamente.
- 9 componentes em `components/AppComponents.kt`; tema e tipografia.
- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` → **BUILD SUCCESSFUL** (21 s).

**Quebrado / incompleto (a SPEC acima corrige):**

1. **Estado órfão** — `MyTextField` (`AppComponents.kt:91`) e `PasswordTextField`
   (`AppComponents.kt:117`) guardam o valor em `remember` interno. O que o usuário digita
   **nunca sai do componente**: a tela não tem acesso ao nome, telefone ou senha.
   (Corrigido por F1 + §8.10.)
2. **Ações mortas** — `ButtonComponent` (`AppComponents.kt:263`) tem `onClick = { /*TODO*/ }`
   fixo; `CheckboxComponent` (`AppComponents.kt:167`) não expõe o estado marcado;
   `ClickableLoginTextComponent { }` recebe lambda vazia em `SignUpScreen.kt:93`.
   (Corrigido por F1, F2.)
3. **`Routes.LOGIN` órfã** — declarada em `AppNavHost.kt:17`, sem `composable()` e sem tela.
   (Corrigido por F2.)
4. **`legacy/`** — `MerceariaRouter.kt` (navegação por `Crossfade`, substituída por Navigation
   Compose) e `MerceariaApp.kt` inteiro comentado. Código morto: **deve ser removido**.
5. **Testes são placeholders** — `ExampleUnitTest` (`assertEquals(4, 2+2)`) e
   `ExampleInstrumentedTest`. Nenhuma cobertura real.
6. **APIs depreciadas** — `ClickableText` e `Divider` (M2). Ver §8.11.
7. **Sem tratamento de teclado** — `SignUpScreen` usa `Column` sem scroll; com o teclado aberto
   em tela pequena o botão fica inalcançável. Corrigir com `verticalScroll` + `imePadding`.
8. **Typo em produção** — `"Politícas de privacidade"` em `PrivacyPoliticsScreen.kt:19`, e o
   texto está hardcoded no composable em vez de `strings.xml`. (Corrigido por F10.)
9. **`Icon` sem `contentDescription`** — `AppComponents.kt:111` e `:143` passam `""`. Os ícones
   de campo são decorativos, então `null` é o correto; `""` é uso incorreto da API.

---

## 10. Fora de escopo (explicitamente NÃO faremos)

- **Pedidos e carrinho do cliente.** Decisão §8.12. O cliente consulta; não compra pelo app.
- **Registro de venda à vista** (dinheiro/PIX/cartão). Só a venda a prazo passa pelo app; a
  venda à vista continua fora, e seu estoque é ajustado à mão (§8.5).
- **Estorno ou exclusão de lançamento de fiado.** Erro se corrige com um lançamento contrário
  (débito indevido → crédito de igual valor). O estoque desse estorno é ajustado à mão.
- **"Esqueci minha senha".** Consequência direta do e-mail sintético (§8.1): não há caixa de
  entrada para receber o link. Recuperação é presencial com a Maria.
- **Verificação de e-mail / SMS / 2FA.**
- **Promoção de usuário a ADMIN pela interface.** O ADMIN é criado no Console (§2.3).
- **Tela de editar perfil.** Consequência: o denormalizado de nome no fiado nunca envelhece (§8.3).
- **Pagamento real** (gateway, PIX, maquininha). O fiado é registro contábil, não cobrança.
- **Imagem de produto.** Catálogo é texto. Sem câmera, galeria ou Firebase Storage.
- **Unidade de medida do produto** (`UN`/`KG`/`L`). Os campos do produto são exatamente três:
  nome, quantidade, preço. Se precisar, coloque na string do nome ("Açúcar 1kg").
- **Venda fracionada a granel** (0,350 kg de queijo). Quantidade é inteira.
- **Notificações push, Cloud Functions, Crashlytics, Analytics, Remote Config.**
- **Busca server-side / paginação do catálogo.** Ver §8.4; vale até ~1.000 produtos.
- **Relatórios, gráficos, faturamento, exportação, recibo impresso.**
- **Modo escuro desenhado.** O tema tem `DarkColorScheme`, mas as telas são especificadas e
  validadas em claro. Não há critério de aceitação para escuro.
- **Internacionalização.** Só pt-BR. Sem `values-en/`.
- **Acessibilidade além do básico.** Exigido: `contentDescription` correto em `Icon`
  (`null` para decorativo, texto para interativo). Não exigido: auditoria de TalkBack,
  contraste WCAG, fontes dinâmicas.
- **Emulador do Firebase no CI.** As regras (§F11) são validadas manualmente no Rules Playground
  e o resultado é registrado; não há suíte automatizada de regras nesta entrega.
- **Multi-tenant / múltiplas mercearias.**

---

## 11. Checklist de qualidade da SPEC

```
[x] Cada funcionalidade tem ao menos 1 exemplo concreto?
    → F1 tem 5, F6 tem 5, F11 tem um bloco de 18 asserções, as demais têm 1 a 6 cada,
      todas com entrada e saída explícitas.
[x] Não existem requisitos implícitos (tudo que importa está escrito)?
    → Validações com mensagem exata; mapeamento exceção→mensagem; sequências numeradas do
      submit e da transação (com a ordem leituras-antes-de-escritas que o Firestore exige);
      compensação de falha parcial (B4/B5); timeout de 10 s e o que fazer em cada caso,
      incluindo que transação não funciona offline; regras de segurança em texto final;
      índices compostos; normalização e formatação de moeda determinísticas; ordenações;
      congelamento de preço e o que fazer quando ele diverge.
[x] A SPEC está dividida em seções executáveis independentemente?
    → F1–F12 numeradas. Funções puras (formatarBrl, normalizar, emailSinteticoDe,
      telefoneDeEmailSintetico, descreverItens) e os validadores são implementáveis e
      testáveis sem Firebase.
[x] Existem critérios de aceitação testáveis para cada funcionalidade?
    → Bloco "Critério de aceitação" com checkboxes em todas as 12.
[x] Casos de borda documentados (o que acontece quando dá erro)?
    → §6, B1–B28, cada um com o comportamento exigido, incluindo os 6 modos de falha de rede
      e os 4 modos de aborto da transação de venda.
[x] Stack e dependências estão explícitas?
    → §7 com versões exatas do que existe, do que entra, do que saiu, lista de proibidos e os
      5 pré-requisitos de setup humano.
[x] O que está FORA do escopo está claramente delimitado?
    → §10, 19 itens, cada um com o motivo ou a decisão que o originou.
```

**Estratégia de execução:** seção pequena → implementar → checkpoint (`git diff`) → ajustar →
próxima. Teste é a prova objetiva de aderência; ver ordem e gates em `PLAN.md`.
