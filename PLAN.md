# PLAN.md

> Contrato de execução. O agent que executa uma task tem **zero contexto** da conversa que gerou
> este plano — tudo que ele precisa está escrito aqui e em `SPEC.md`.
>
> Hierarquia: **Sprint → Fase → Task**. Tasks da mesma fase são independentes e rodam em
> paralelo. Fases são sequenciais: a próxima só começa quando o **Gate** da atual passa.
>
> Gate mínimo global, válido em toda fase:
> `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` → `BUILD SUCCESSFUL`.

---

## Sprint 1 — Um cliente se cadastra, fecha o app, reabre e continua logado; a Maria entra e cai na área de gestão

### Fase 0 — Base técnica e limpeza do baseline
> Dependências: nenhuma (mas Task 0.1 exige o setup humano do Firebase — ver `CLAUDE.md §3`)
> Paralelismo: 0.1, 0.2, 0.3 e 0.4 rodam em paralelo — arquivos disjuntos, verificado abaixo
> Gate: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest && test -f firestore.rules`

#### Task 0.1 — Adicionar Firebase à build
- Agent: build-engineer
- Input: `app/google-services.json` já colocado pelo humano (`CLAUDE.md §3`, passo 2); versões em `SPEC.md §7`
- Output: `gradle/libs.versions.toml` com as entradas `googleServices = "4.4.2"`, `firebaseBom = "33.1.2"`, `coroutinesPlayServices = "1.8.1"`, `lifecycleViewmodelCompose = "2.8.4"`, `coroutinesTest = "1.8.1"`, `archCoreTesting = "2.2.0"` e as libs/plugins correspondentes; `build.gradle.kts` (raiz) com `alias(libs.plugins.google.services) apply false`; `app/build.gradle.kts` aplicando o plugin e declarando `platform(libs.firebase.bom)`, `firebase-auth`, `firebase-firestore`, `coroutines-play-services`, `lifecycle-viewmodel-compose`, `testImplementation(libs.coroutines.test)`, `testImplementation(libs.arch.core.testing)`. **Nenhuma string de versão literal em `app/build.gradle.kts`.**
- Testes críticos:
  - [ ] `./gradlew :app:dependencies --configuration debugRuntimeClasspath` lista `firebase-auth` e `firebase-firestore` resolvidos via BOM 33.1.2
  - [ ] `./gradlew :app:compileDebugKotlin` termina em `BUILD SUCCESSFUL` (prova que o `google-services.json` foi processado)
  - [ ] `grep -nE '"[0-9]+\.[0-9]+\.[0-9]+"' app/build.gradle.kts` não retorna nenhuma versão de dependência (só `versionName`)

#### Task 0.2 — Tornar os componentes stateless e remover o código morto
- Agent: compose-ui
- Input: `SPEC.md §8.10`, `§8.11`, `§9.1`, `§9.2`, `§9.4`, `§9.6`, `§9.9`
- Output: em `components/AppComponents.kt` — `MyTextField(labelValue: String, painterResource: Painter, value: String, onValueChange: (String) -> Unit, errorMessage: String? = null)`; `PasswordTextField(labelValue: String, painterResource: Painter, value: String, onValueChange: (String) -> Unit, errorMessage: String? = null)` (a visibilidade da senha continua em `remember` — é estado visual, permitido); `CheckboxComponent(checked: Boolean, onCheckedChange: (Boolean) -> Unit)`; `ButtonComponent(value: String, onClick: () -> Unit, enabled: Boolean = true)`; `ClickableLoginTextComponent(onTextSelected: () -> Unit)` reescrito com `LinkAnnotation` no lugar de `ClickableText`; `Divider` (M2) trocado por `HorizontalDivider` (M3); `contentDescription = null` nos ícones decorativos das linhas 111 e 143. Pasta `legacy/` **deletada**. `SignUpScreen.kt` ajustado só o suficiente para compilar com as novas assinaturas (estado real vem na Task 6.1). **Não editar `strings.xml` nesta task** (é da Task 0.4).
- Testes críticos:
  - [ ] `./gradlew :app:compileDebugKotlin` passa após a mudança de assinaturas
  - [ ] `grep -rn "remember" components/AppComponents.kt` só aparece para `passwordVisibility` — nenhum `remember` guardando valor de campo
  - [ ] `grep -rn "androidx.compose.material\." app/src/main` não retorna `Divider` nem `ClickableText`
  - [ ] `test ! -d app/src/main/java/com/example/merceariadamaria/legacy` (pasta removida)

#### Task 0.3 — Criar as regras e os índices do Firestore
- Agent: firebase-data
- Input: `SPEC.md §F11` (texto final das regras) e `§5.6` (tabela de índices)
- Output: `firestore.rules` na raiz, **verbatim** do `SPEC.md §F11`; `firestore.indexes.json` na raiz com os 3 índices compostos: `produtos(ativo asc, nomeNormalizado asc)`, `fiados(saldoCentavos desc)`, `usuarios(perfil asc, nome asc)`
- Testes críticos:
  - [ ] `firestore.rules` contém `rules_version = '2'` e a função `ehAdmin()`
  - [ ] `firestore.rules` termina com o catch-all `match /{document=**} { allow read, write: if false; }`
  - [ ] `firestore.indexes.json` é JSON válido (`node -e "JSON.parse(require('fs').readFileSync('firestore.indexes.json','utf8'))"`) e tem exatamente 3 entradas em `indexes`
  - [ ] `grep -c "perfil == 'CLIENTE'" firestore.rules` retorna ≥ 1 (a regra que impede autopromoção)

#### Task 0.4 — Desligar dynamicColor e centralizar as strings
- Agent: compose-ui
- Input: `SPEC.md §8.9`, `§F10` (typo), `§9.8`
- Output: `ui/theme/Theme.kt` com `dynamicColor: Boolean = false` como default; `res/values/strings.xml` com as strings novas: `terms_of_use`, `privacy_policy` (`"Política de privacidade"` — corrige o typo `"Politícas de privacidade"`), `no_products_found`, `out_of_stock`, `no_connection`, `saved_offline`, `try_again`, `logout`, `total`, `search`. **Não editar `AppComponents.kt` nesta task** (é da Task 0.2).
- Testes críticos:
  - [ ] `grep -n "dynamicColor: Boolean = false" ui/theme/Theme.kt` casa
  - [ ] `grep -c "Politícas" app/src/main` retorna 0 em todo o `main`
  - [ ] `./gradlew :app:compileDebugKotlin` passa (nenhuma string referenciada que não existe)

---

### Fase 1 — Núcleo puro (`core/`): funções sem Android e sem Firebase
> Dependências: Fase 0 (só pelo gate de build; o código desta fase não depende de nada da 0)
> Paralelismo: 1.1, 1.2, 1.3 e 1.4 rodam em paralelo — um arquivo de produção e um de teste cada
> Gate: `./gradlew :app:testDebugUnitTest --tests "com.example.merceariadamaria.core.*"`

#### Task 1.1 — Formatadores
- Agent: core-kotlin
- Input: contratos de `SPEC.md §F4`
- Output: `core/Formatadores.kt` com `fun formatarBrl(centavos: Long): String` e `fun normalizar(texto: String): String`. `formatarBrl` é determinística e **não** usa `Locale` do device. `normalizar` usa `java.text.Normalizer` NFD + descarte de diacríticos + lowercase + trim.
- Testes críticos:
  - [ ] `FormatadoresTest`: `formatarBrl` retorna exatamente `"R$ 0,00"`, `"R$ 5,49"`, `"R$ 28,90"`, `"R$ 1.234,56"`, `"R$ 1.000.000,00"` para `0, 549, 2890, 123456, 100000000`
  - [ ] `formatarBrl(-1505)` retorna `"-R$ 15,05"` (saldo negativo é caso real — `SPEC.md §F9`)
  - [ ] `normalizar("Açúcar Cristal ")` retorna `"acucar cristal"`; `normalizar("CAFÉ")` retorna `"cafe"`; `normalizar("")` retorna `""`

#### Task 1.2 — E-mail sintético
- Agent: core-kotlin
- Input: contratos de `SPEC.md §F1`, decisão `§8.1`
- Output: `core/AuthConstants.kt` com `const val DOMINIO_SINTETICO = "merceariadamaria.app"`, `fun emailSinteticoDe(telefone: String): String` (remove não-dígitos antes de montar) e `fun telefoneDeEmailSintetico(email: String): String` (retorna `""` para domínio diferente)
- Testes críticos:
  - [ ] `emailSinteticoDe("11987654321")` e `emailSinteticoDe("(11) 98765-4321")` retornam ambos `"11987654321@merceariadamaria.app"`
  - [ ] `telefoneDeEmailSintetico("11987654321@merceariadamaria.app")` retorna `"11987654321"`
  - [ ] `telefoneDeEmailSintetico("ana@gmail.com")` retorna `""` (domínio estranho não vira telefone)

#### Task 1.3 — Validadores
- Agent: core-kotlin
- Input: tabelas de validação de `SPEC.md §F1` e `§F5`, borda `B1`
- Output: `core/Validadores.kt` com `fun validarNome(valor: String): String?`, `validarSobrenome`, `validarTelefone`, `validarSenha`, `validarTermos(aceito: Boolean): String?`, `validarNomeProduto`, `validarPrecoCentavos(valor: Long): String?`, `validarQuantidade(valor: Int): String?`. Cada uma retorna `null` quando válido, ou **exatamente** a mensagem da tabela da SPEC.
- Testes críticos:
  - [ ] `validarNome("Ana")` → `null`; `validarNome("A")`, `validarNome("   ")` e `validarNome("Ana1")` → `"Informe um nome válido"`
  - [ ] `validarTelefone("11987654321")` → `null`; `validarTelefone("1198765432")` (10 dígitos), `validarTelefone("+5511987654321")` (13) e `validarTelefone("11887654321")` (3º dígito ≠ 9) → `"Telefone deve ter DDD + 9 dígitos"`
  - [ ] `validarSenha("mercearia1")` → `null`; `validarSenha("merceari")` (sem dígito) e `validarSenha("merc1")` (curta) → `"Senha precisa de 8+ caracteres, com letra e número"`
  - [ ] `validarPrecoCentavos(0)` e `validarPrecoCentavos(-1)` → `"Preço deve ser maior que zero"`; `validarQuantidade(-1)` → `"Quantidade não pode ser negativa"`

#### Task 1.4 — Descrição de itens de lançamento
- Agent: core-kotlin
- Input: contrato `descreverItens` de `SPEC.md §F6`
- Output: `core/Descricoes.kt` com `fun descreverItens(itens: List<ItemDescritivel>): String` onde `interface ItemDescritivel { val nomeProduto: String; val quantidade: Int }` — formato `"{qtd}x {nome}"` unido por `", "`, truncado em 119 chars + `"…"` quando exceder 120
- Testes críticos:
  - [ ] 2 itens (Café x2, Açúcar x1) → exatamente `"2x Café Torrado 500g, 1x Açúcar Cristal 1kg"`
  - [ ] lista vazia → `""`
  - [ ] 20 itens de nome longo → resultado com `length == 120` e terminando em `"…"`

---

### Fase 2 — Contratos de domínio
> Dependências: Fase 1
> Paralelismo: nenhum — task única (as interfaces referenciam os modelos do mesmo entregável)
> Gate: `./gradlew :app:compileDebugKotlin`

#### Task 2.1 — Modelos e interfaces de repositório
- Agent: firebase-data
- Input: `SPEC.md §5.1`–`§5.5`
- Output: `domain/model/` com `enum class Perfil { CLIENTE, ADMIN }`, `enum class TipoLancamento { DEBITO, CREDITO }`, e as data classes `Usuario(uid, nome, sobrenome, telefone, email, perfil, criadoEmMillis)`, `Produto(id, nome, nomeNormalizado, precoCentavos: Long, quantidade: Int, ativo: Boolean)`, `ContaFiado(clienteUid, clienteNome, clienteTelefone, saldoCentavos: Long)`, `ItemLancamento(produtoId, nomeProduto, precoUnitarioCentavos: Long, quantidade: Int, subtotalCentavos: Long)` (implementa `ItemDescritivel`), `Lancamento(id, tipo, valorCentavos: Long, itens: List<ItemLancamento>, descricao, criadoEmMillis, criadoPorUid)`. E `domain/repository/` com `UsuarioRepository`, `ProdutoRepository`, `FiadoRepository` — todas interfaces, métodos `suspend` retornando `Result<T>` ou `Flow<List<T>>`, **sem nenhum tipo do Firebase na assinatura**.
- Testes críticos:
  - [ ] `./gradlew :app:compileDebugKotlin` passa
  - [ ] `grep -rn "com.google.firebase" domain/` retorna 0 linhas (domínio não conhece Firebase)
  - [ ] `grep -rnE "(Double|Float)" domain/model/` retorna 0 linhas (dinheiro é `Long` — `SPEC.md §8.7`)
  - [ ] `ItemLancamentoTest`: `subtotalCentavos == precoUnitarioCentavos * quantidade` para 3 combinações

---

### Fase 3 — Infraestrutura de dados
> Dependências: Fase 2
> Paralelismo: 3.1 e 3.2 rodam em paralelo — arquivos disjuntos
> Gate: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`

#### Task 3.1 — Mappers Firestore → domínio
- Agent: firebase-data
- Input: `SPEC.md §5.1`–`§5.5`, borda `B13`
- Output: `data/mapper/Mappers.kt` com `fun DocumentSnapshot.toUsuarioOrNull(): Usuario?`, `toProdutoOrNull()`, `toContaFiadoOrNull()`, `toLancamentoOrNull()`, e os inversos `Usuario.toMap()`, `Produto.toMap()`. Os mappers de leitura **nunca lançam**: campo ausente, `null` ou de tipo errado → retorna `null` e emite `Log.w`. `Timestamp` → `Long` em millis.
- Testes críticos:
  - [ ] Mapa completo e válido → objeto de domínio com todos os campos corretos (um teste por entidade, usando um duplo de `DocumentSnapshot`)
  - [ ] Mapa sem o campo `perfil` → `toUsuarioOrNull()` retorna `null` **sem lançar exceção**
  - [ ] Mapa com `precoCentavos` como `String` → `toProdutoOrNull()` retorna `null` sem lançar
  - [ ] `Produto.toMap()` sempre inclui `nomeNormalizado == normalizar(nome)` (`SPEC.md §F5`)

#### Task 3.2 — Application e settings do Firestore
- Agent: firebase-data
- Input: `SPEC.md §F12` (persistência habilitada uma única vez)
- Output: `MerceariaApplication.kt` estendendo `Application`, com `onCreate()` configurando `isPersistenceEnabled = true` **antes de qualquer uso** do Firestore, e `android:name=".MerceariaApplication"` registrado no `<application>` do `AndroidManifest.xml`
- Testes críticos:
  - [ ] `grep -n "android:name=\".MerceariaApplication\"" app/src/main/AndroidManifest.xml` casa
  - [ ] `grep -c "isPersistenceEnabled" app/src/main` retorna exatamente 1 (configurar duas vezes lança `IllegalStateException`)
  - [ ] `./gradlew :app:assembleDebug` passa

---

### Fase 4 — Repositórios Firebase
> Dependências: Fase 3
> Paralelismo: 4.1, 4.2 e 4.3 rodam em paralelo — um arquivo de produção e um fake cada
> Gate: `./gradlew :app:testDebugUnitTest`

#### Task 4.1 — UsuarioRepository
- Agent: firebase-data
- Input: `SPEC.md §F1`, `§F2`, `§F3`, bordas `B4`, `B5`, `B10`, `B11`
- Output: `data/firebase/UsuarioRepositoryFirebase.kt` implementando `cadastrar(nome, sobrenome, telefone, senha): Result<Usuario>` (sequência de 7 passos de `§F1`, incluindo a compensação com `user.delete()`), `entrar(telefone, senha): Result<Usuario>`, `usuarioAtual(): Result<Usuario?>` (cria o documento mínimo se ausente — `B5`), `sair()`, `buscarClientes(termo: String): Result<List<Usuario>>`. Todo `await()` dentro de `withTimeout(10_000)`. Exceção mapeada para as mensagens da tabela de `§F1` — nenhuma exceção crua na borda. Mais `app/src/test/.../fake/FakeUsuarioRepository.kt`.
- Testes críticos:
  - [ ] `cadastrar` com dados válidos retorna `Result.success` com `perfil == CLIENTE`
  - [ ] Falha na gravação do documento chama `delete()` da conta Auth e retorna `Result.failure` com `"Não foi possível concluir o cadastro. Tente novamente."`
  - [ ] `FirebaseAuthUserCollisionException` vira a mensagem `"Este telefone já está cadastrado"`
  - [ ] `usuarioAtual()` com documento ausente cria o mínimo com `perfil == CLIENTE` e telefone derivado do e-mail sintético
  - [ ] `grep -c "withTimeout" data/firebase/UsuarioRepositoryFirebase.kt` ≥ número de chamadas `await()` no arquivo

#### Task 4.2 — ProdutoRepository
- Agent: firebase-data
- Input: `SPEC.md §F4`, `§F5`, bordas `B12`, `B13`
- Output: `data/firebase/ProdutoRepositoryFirebase.kt` com `observarAtivos(): Flow<List<Produto>>` (snapshot listener, ordenado por `nomeNormalizado`), `observarTodos(): Flow<List<Produto>>` (gestão, inclui inativos), `criar(nome, precoCentavos, quantidade): Result<String>`, `atualizar(produto): Result<Unit>` (preserva `criadoEm`, atualiza `atualizadoEm`), `definirAtivo(id, ativo): Result<Unit>` (soft delete), `nomeJaExiste(nomeNormalizado, ignorandoId: String?): Result<Boolean>`. Mais `fake/FakeProdutoRepository.kt`.
- Testes críticos:
  - [ ] `observarAtivos()` emite só produtos com `ativo == true`, ordenados por `nomeNormalizado`
  - [ ] `criar` grava `nomeNormalizado == normalizar(nome)` e `ativo == true`
  - [ ] `definirAtivo(id, false)` não remove o documento — a contagem da coleção não muda
  - [ ] `nomeJaExiste("acucar cristal 1kg", null)` retorna `true` quando existe um ativo com esse normalizado, e `false` quando o único match está inativo

#### Task 4.3 — FiadoRepository (transações)
- Agent: firebase-data
- Input: `SPEC.md §F6` (sequência numerada da transação), `§F7`, `§F8`, bordas `B9`, `B14`–`B19`, `B21`–`B23`
- Output: `data/firebase/FiadoRepositoryFirebase.kt` com `observarDevedores(): Flow<List<ContaFiado>>` (`saldoCentavos > 0`, desc), `observarConta(clienteUid): Flow<ContaFiado?>`, `observarLancamentos(clienteUid): Flow<List<Lancamento>>` (`criadoEm` desc), `lancarDebito(clienteUid, itens: List<ItemSelecionado>): Result<Unit>` e `lancarCredito(clienteUid, valorCentavos, observacao): Result<Unit>` — ambos via `runTransaction`, leituras antes de escritas, id pré-gerado com `collection.document()`. Mais `fake/FakeFiadoRepository.kt`.
- Testes críticos:
  - [ ] `lancarDebito` com estoque suficiente: cria o lançamento, soma o total ao saldo e decrementa a quantidade de **cada** produto
  - [ ] `lancarDebito` com estoque insuficiente em 1 de 2 itens: `Result.failure` com `"Estoque insuficiente para {nome} (disponível: {n})"` e **nenhum** dos 4 documentos alterado
  - [ ] `lancarDebito` com preço divergente: `Result.failure` com `"O preço de {nome} mudou. Confira o total."` e nada gravado
  - [ ] `lancarDebito` em cliente sem conta cria a conta na mesma transação, com `clienteNome`/`clienteTelefone` denormalizados
  - [ ] `lancarDebito` com 51 itens distintos → `"Máximo de 50 produtos por lançamento"`
  - [ ] `lancarCredito` subtrai do saldo e grava `valorCentavos` **positivo** com `itens` vazio
  - [ ] `lancarCredito(uid, 0, "")` e `lancarCredito(uid, -500, "")` → `"Informe um valor maior que zero"`, nada gravado
  - [ ] `lancarCredito` não altera nenhum documento de `produtos`

---

### Fase 5 — Container e esqueleto de navegação
> Dependências: Fase 4
> Paralelismo: 5.1 e 5.2 rodam em paralelo — `di/AppContainer.kt` vs `ui/navigation/AppNavHost.kt`
> Gate: `./gradlew :app:assembleDebug`

#### Task 5.1 — AppContainer (injeção manual)
- Agent: firebase-data
- Input: `SPEC.md §8.8`; repositórios da Fase 4
- Output: `di/AppContainer.kt` expondo `firebaseAuth`, `firestore`, `usuarioRepository`, `produtoRepository`, `fiadoRepository` como `by lazy`; instanciado em `MerceariaApplication` e alcançável pelos `ViewModel` via `ViewModelProvider.Factory`. Uma `viewModelFactory` genérica em `di/ViewModelFactories.kt`.
- Testes críticos:
  - [ ] `AppContainerTest`: cada repositório é criado uma única vez (duas leituras da mesma propriedade retornam a mesma instância)
  - [ ] `grep -rn "com.google.firebase" ui/` retorna 0 linhas — o container é a única fronteira

#### Task 5.2 — Rotas e NavHost completo com placeholders
- Agent: compose-ui
- Input: `SPEC.md §5.7` (lista de rotas), `§F3` (roteamento por perfil)
- Output: `ui/navigation/AppNavHost.kt` com `object Routes` contendo **todas** as 14 constantes de `§5.7`, e um `composable()` registrado para **cada** rota. Telas ainda não implementadas recebem um placeholder `PlaceholderScreen(nome: String)`. Grafos separados por perfil (`clienteGraph`, `gestaoGraph`). `startDestination = Routes.SPLASH`. **Esta é a única task que edita `AppNavHost.kt`** — as tasks das Fases 6–10 só preenchem os arquivos das próprias telas, nunca este.
- Testes críticos:
  - [ ] Toda constante de `Routes` tem um `composable()` correspondente (teste que reflete sobre `Routes` e compara com a lista de rotas registradas)
  - [ ] `Routes.LOGIN` tem `composable()` — fecha a lacuna de `SPEC.md §9.3`
  - [ ] `./gradlew :app:assembleDebug` passa e o app abre no `SPLASH`

---

### Fase 6 — Identidade: cadastro, login e splash
> Dependências: Fase 5
> Paralelismo: 6.1, 6.2 e 6.3 rodam em paralelo — pastas de feature disjuntas; nenhuma edita `AppNavHost.kt`
> Gate: `./gradlew :app:testDebugUnitTest --tests "*ViewModelTest"` e cadastro→reabrir→continua logado verificado em device

#### Task 6.1 — Cadastro com estado real
- Agent: compose-ui
- Input: `SPEC.md §F1` completo (5 exemplos + tabela de exceções); `Validadores` (1.3); `FakeUsuarioRepository` (4.1)
- Output: `ui/screen/signup/SignUpUiState.kt`, `SignUpViewModel.kt` (expõe `StateFlow<SignUpUiState>`, eventos `NomeChanged`/`SobrenomeChanged`/`TelefoneChanged`/`SenhaChanged`/`TermosChanged`/`Submit`, flag `touched` por campo) e `SignUpScreen.kt` refatorada para consumir o ViewModel, com `verticalScroll` + `imePadding` (`SPEC.md §9.7`). O ViewModel vive no escopo do **grafo**, não da tela (`SPEC.md §F10`).
- Testes críticos:
  - [ ] Estado do "caminho feliz" de `§F1` exemplo 1: os 5 erros `null` e `isFormValid == true`
  - [ ] `TelefoneChanged("1198765432")` → `phoneError == "Telefone deve ter DDD + 9 dígitos"` e `isFormValid == false`
  - [ ] `Submit` com `isFormValid == false` **não** chama o repositório (fake registra 0 chamadas)
  - [ ] `TelefoneChanged("(11) 98765-4321")` faz o repositório receber `"11987654321"`
  - [ ] Colisão de telefone no fake → `phoneError == "Este telefone já está cadastrado"` e `isSubmitting == false`
  - [ ] Dois `Submit` disparados em sequência com o primeiro em andamento → o repositório é chamado **uma** vez (`B25`)

#### Task 6.2 — Tela de login
- Agent: compose-ui
- Input: `SPEC.md §F2`; `FakeUsuarioRepository` (4.1)
- Output: `ui/screen/login/LoginUiState.kt`, `LoginViewModel.kt`, `LoginScreen.kt`, reaproveitando `MyTextField` e `PasswordTextField`. Efeito de navegação decidido pelo `Perfil`.
- Testes críticos:
  - [ ] Credencial correta de `CLIENTE` → efeito `NavigateTo(Routes.CATALOGO)`; de `ADMIN` → `NavigateTo(Routes.GESTAO_HOME)`
  - [ ] `FirebaseAuthInvalidUserException` e `FirebaseAuthInvalidCredentialsException` produzem **a mesma** string `"Telefone ou senha inválidos"` (o teste compara as duas)
  - [ ] Telefone com 9 dígitos → `phoneError` preenchido e **0** chamadas ao repositório

#### Task 6.3 — Splash e roteamento por perfil
- Agent: compose-ui
- Input: `SPEC.md §F3` (tabela de 5 casos)
- Output: `ui/screen/splash/SplashViewModel.kt` com `resolverDestinoInicial(): DestinoInicial` e `SplashScreen.kt`; ação de logout com `popUpTo(0) { inclusive = true }` exposta para as telas autenticadas
- Testes críticos:
  - [ ] Os 5 casos da tabela de `§F3` retornam o `DestinoInicial` correto (sem sessão → `Login`; `CLIENTE` → `Catalogo`; `ADMIN` → `Gestao`; doc ausente → `Catalogo`; falha de rede → `Login`)
  - [ ] Falha de rede na leitura do perfil não lança exceção — retorna `Login` com a mensagem `"Sem conexão. Verifique sua internet."`

---

## Sprint 2 — A Maria cadastra um produto e o cliente vê esse produto no catálogo com preço formatado

### Fase 7 — Leitura de produtos
> Dependências: Fase 6
> Paralelismo: 7.1 e 7.2 rodam em paralelo — pastas de feature disjuntas
> Gate: `./gradlew :app:testDebugUnitTest --tests "*CatalogoViewModelTest" --tests "*ProdutosListViewModelTest"`

#### Task 7.1 — Catálogo do cliente
- Agent: compose-ui
- Input: `SPEC.md §F4`; `Formatadores` (1.1); `FakeProdutoRepository` (4.2)
- Output: `ui/screen/catalogo/CatalogoUiState.kt`, `CatalogoViewModel.kt` (coleta `observarAtivos()`, busca com debounce de 300 ms filtrando por substring de `normalizar`), `CatalogoScreen.kt` (lista, rótulo "Sem estoque" para `quantidade <= 0`, estado vazio, aviso de cache offline)
- Testes críticos:
  - [ ] Busca `""` lista os 3 ativos do exemplo de `§F4` e omite o inativo
  - [ ] Busca `"ac"`, `"AC"`, `"açu"` e `"cristal"` todas retornam `[Açúcar Cristal 1kg]`
  - [ ] Busca `"xyz"` → lista vazia e `isEmpty == true` (estado vazio, não erro)
  - [ ] Produto com `quantidade == 0` aparece na lista com `semEstoque == true`
  - [ ] Debounce: 3 mudanças de busca em menos de 300 ms resultam em **1** filtragem

#### Task 7.2 — Lista de produtos na gestão
- Agent: compose-ui
- Input: `SPEC.md §F5`; `FakeProdutoRepository` (4.2)
- Output: `ui/screen/gestaoprodutos/ProdutosListUiState.kt`, `ProdutosListViewModel.kt` (coleta `observarTodos()`), `ProdutosListScreen.kt` mostrando nome, preço formatado, quantidade e o selo "inativo"; navegação para o formulário
- Testes críticos:
  - [ ] Lista inclui produtos inativos, marcados com `inativo == true`
  - [ ] Preço é exibido via `formatarBrl` (teste verifica a string, não o número)
  - [ ] Lista vazia → estado vazio explícito

---

### Fase 8 — Escrita de produtos
> Dependências: Fase 7
> Paralelismo: 8.1 e 8.2 rodam em paralelo — `ProdutoFormScreen`/`ProdutoFormViewModel` vs `ProdutosListViewModel`
> Gate: `./gradlew :app:testDebugUnitTest --tests "*ProdutoForm*"` e criar produto em device refletindo no catálogo

#### Task 8.1 — Formulário de criar e editar produto
- Agent: compose-ui
- Input: `SPEC.md §F5` (tabela de validação + 6 exemplos); `Validadores` (1.3)
- Output: `ui/screen/gestaoprodutos/ProdutoFormUiState.kt`, `ProdutoFormViewModel.kt` (modo criar e modo editar por `produtoId`, valida antes de gravar, checa unicidade com `nomeJaExiste`), `ProdutoFormScreen.kt` com os **três** campos de negócio: nome, quantidade e preço (preço digitado em reais e convertido para centavos sem `Double`)
- Testes críticos:
  - [ ] Criar válido chama o repositório uma vez com `nomeNormalizado == normalizar(nome)`
  - [ ] Nome que colide com um ativo (`"feijao carioca 1kg"` vs `"Feijão Carioca 1kg"`) → `nomeError == "Já existe um produto com esse nome"` e **0** gravações
  - [ ] Preço `0` → `precoError == "Preço deve ser maior que zero"`, nada gravado
  - [ ] Quantidade `-1` → `quantidadeError == "Quantidade não pode ser negativa"`, nada gravado
  - [ ] Editar preserva `criadoEm` e altera `atualizadoEm`
  - [ ] Conversão de `"28,90"` digitado resulta em `2890` centavos sem passar por `Double`/`Float`

#### Task 8.2 — Excluir e reativar produto
- Agent: compose-ui
- Input: `SPEC.md §F5` (soft delete); `ProdutosListViewModel` (7.2)
- Output: ações `excluir(id)` e `reativar(id)` no `ProdutosListViewModel`, chamando `definirAtivo`, com diálogo de confirmação na `ProdutosListScreen`. **Não editar `ProdutoFormViewModel`** (é da Task 8.1).
- Testes críticos:
  - [ ] `excluir(id)` chama `definirAtivo(id, false)` — nunca um delete físico
  - [ ] Produto excluído desaparece do `observarAtivos()` mas continua no `observarTodos()`
  - [ ] `reativar(id)` chama `definirAtivo(id, true)` e o produto volta ao catálogo

---

## Sprint 3 — A Maria monta uma venda a prazo pelo catálogo, o estoque baixa, e o cliente vê a dívida no próprio app

### Fase 9 — Consulta de fiado
> Dependências: Fase 8
> Paralelismo: 9.1, 9.2 e 9.3 rodam em paralelo — três pastas de feature disjuntas
> Gate: `./gradlew :app:testDebugUnitTest --tests "*Fiado*"`

#### Task 9.1 — Lista de devedores (ADMIN)
- Agent: compose-ui
- Input: `SPEC.md §F8`; `FakeFiadoRepository` (4.3)
- Output: `ui/screen/gestaofiado/DevedoresUiState.kt`, `DevedoresViewModel.kt` (coleta `observarDevedores()`), `DevedoresScreen.kt` com nome, telefone e saldo formatado
- Testes críticos:
  - [ ] Só contas com `saldoCentavos > 0` aparecem; saldo `0` e negativo são omitidos
  - [ ] Ordenação decrescente por saldo
  - [ ] Nenhum devedor → `"Ninguém está devendo agora"`

#### Task 9.2 — Extrato do cliente (ADMIN)
- Agent: compose-ui
- Input: `SPEC.md §F8`; `FakeFiadoRepository` (4.3)
- Output: `ui/screen/gestaofiado/ExtratoUiState.kt`, `ExtratoViewModel.kt` (coleta `observarLancamentos`), `ExtratoScreen.kt` com lançamentos em `criadoEm` desc e `DEBITO` expansível mostrando os itens congelados com subtotal formatado
- Testes críticos:
  - [ ] Lançamentos vêm em `criadoEm` desc
  - [ ] `DEBITO` expandido lista os `itens` com `subtotalCentavos` formatado por `formatarBrl`
  - [ ] `CREDITO` não oferece expansão (`itens` vazio) e não quebra a tela

#### Task 9.3 — Meu fiado (cliente)
- Agent: compose-ui
- Input: `SPEC.md §F9`; `FakeFiadoRepository` (4.3)
- Output: `ui/screen/meufiado/MeuFiadoUiState.kt`, `MeuFiadoViewModel.kt`, `MeuFiadoScreen.kt`. **Somente leitura** — nenhuma ação de escrita na tela.
- Testes críticos:
  - [ ] Saldo `3495` → `"Você deve R$ 34,95"`; saldo `0` → `"Você está em dia"`; saldo `-1505` → `"Você tem R$ 15,05 de crédito"`
  - [ ] Cliente sem conta de fiado → `"Você está em dia"`, extrato vazio, **sem erro e sem crash**
  - [ ] `grep -rn "lancar" ui/screen/meufiado/` retorna 0 linhas (nenhuma escrita)

---

### Fase 10 — Lançamentos de fiado
> Dependências: Fase 9
> Paralelismo: 10.1, 10.2 e 10.3 rodam em paralelo — três pastas de feature disjuntas
> Gate: `./gradlew :app:testDebugUnitTest --tests "*NovoDebito*" --tests "*NovoCredito*"` e venda a prazo verificada em device com o estoque baixando

#### Task 10.1 — Buscar cliente para lançar
- Agent: compose-ui
- Input: `SPEC.md §F8` (busca de cliente); `FakeUsuarioRepository` (4.1)
- Output: `ui/screen/gestaoclientes/BuscarClienteUiState.kt`, `BuscarClienteViewModel.kt`, `BuscarClienteScreen.kt` — busca por nome ou telefone entre `perfil == CLIENTE`, incluindo quem ainda não tem conta de fiado
- Testes críticos:
  - [ ] Busca por nome parcial encontra o cliente; busca por telefone parcial também
  - [ ] Cliente sem conta de fiado aparece nos resultados
  - [ ] Usuário `ADMIN` **não** aparece nos resultados
  - [ ] Busca sem resultado → estado vazio explícito

#### Task 10.2 — Novo débito montado pelo catálogo
- Agent: compose-ui
- Input: `SPEC.md §F6` completo (contratos, sequência da transação, 5 exemplos); `FakeProdutoRepository` (4.2); `FakeFiadoRepository` (4.3)
- Output: `ui/screen/gestaofiado/NovoDebitoUiState.kt` (conforme o contrato de `§F6`), `NovoDebitoViewModel.kt` (seleção com `+`/`−`, total recalculado, chama `lancarDebito`), `NovoDebitoScreen.kt` — lista de produtos ativos com seletor de quantidade, `"Sem estoque"` quando `quantidade == 0`, `+` desabilitado no limite do estoque, total formatado e botão confirmar
- Testes críticos:
  - [ ] `totalCentavos == itens.sumOf { precoUnitarioCentavos * quantidade }` com 3 itens
  - [ ] Selecionar o mesmo produto duas vezes soma a quantidade e mantém **uma** linha
  - [ ] `alterarQuantidade(produto, 0)` remove o item da seleção
  - [ ] Tentar passar do estoque disponível não altera a seleção (`+` inerte no limite)
  - [ ] Produtos inativos não aparecem no seletor
  - [ ] Estoque insuficiente no fake → erro `"Estoque insuficiente para {nome} (disponível: {n})"` e a seleção da tela **preservada**
  - [ ] Preço divergente no fake → erro `"O preço de {nome} mudou. Confira o total."` e nada gravado
  - [ ] Seleção vazia → botão confirmar desabilitado e **0** chamadas ao repositório
  - [ ] Falha de transação offline → `"Sem conexão. O lançamento precisa de internet."` com a seleção preservada (`B9`)

#### Task 10.3 — Receber pagamento
- Agent: compose-ui
- Input: `SPEC.md §F7`; `FakeFiadoRepository` (4.3)
- Output: `ui/screen/gestaofiado/NovoCreditoUiState.kt`, `NovoCreditoViewModel.kt`, `NovoCreditoScreen.kt` — valor digitado, observação opcional (0–120 chars) e botão "Quitar tudo"
- Testes críticos:
  - [ ] Valor válido chama `lancarCredito` com `valorCentavos` **positivo**
  - [ ] Valor `0` e negativo → `"Informe um valor maior que zero"` e **0** chamadas ao repositório
  - [ ] "Quitar tudo" preenche exatamente o saldo atual; com saldo ≤ 0 o botão fica desabilitado
  - [ ] Observação vazia resulta em `descricao == "Pagamento recebido"`

---

### Fase 11 — Robustez e fechamento
> Dependências: Fase 10
> Paralelismo: 11.1, 11.2 e 11.3 rodam em paralelo — telas vs verificação manual vs `androidTest/`
> Gate: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lintDebug` e `./gradlew :app:connectedDebugAndroidTest` com device

#### Task 11.1 — Estados de rede e erro em todas as telas
- Agent: compose-ui
- Input: `SPEC.md §F12`, bordas `B6`–`B12`
- Output: em cada tela com I/O — indicador de carregamento com caminho garantido para `false`, mensagem de erro mapeada, aviso de cache offline em leitura, mensagem de sucesso-diferido em escrita simples, e botão "Tentar de novo" quando o cache está vazio
- Testes críticos:
  - [ ] Todo `UiState` com `isLoading` tem teste provando que erro e timeout levam `isLoading` de volta a `false`
  - [ ] Timeout de escrita simples produz `"Salvo. Será sincronizado quando houver conexão."` (não é erro)
  - [ ] Timeout de leitura produz `"Sem conexão. Verifique sua internet."`
  - [ ] `PERMISSION_DENIED` produz `"Você não tem permissão para esta ação."` **e** emite log

#### Task 11.2 — Auditoria das regras do Firestore
- Agent: firestore-rules-auditor
- Input: `firestore.rules` (0.3) e a lista de 18 asserções de `SPEC.md §F11`
- Output: `docs/verificacao-regras.md` com o resultado de cada uma das 18 asserções testadas no Rules Playground do Console (permitido/negado, data e quem verificou), e a lista de divergências encontradas
- Testes críticos:
  - [ ] Cliente autenticado **não** consegue criar `usuarios/{uid}` com `perfil: "ADMIN"` — negado
  - [ ] Cliente **não** consegue criar lançamento nem alterar `saldoCentavos` do próprio fiado — negado
  - [ ] Cliente **não** consegue escrever em `produtos` — negado
  - [ ] Cliente A **não** consegue ler `usuarios/B` nem `fiados/B` — negado
  - [ ] Usuário não autenticado não lê nada — negado
  - [ ] As 18 asserções estão registradas com resultado; nenhuma divergência em aberto

#### Task 11.3 — Testes de UI dos fluxos principais
- Agent: compose-ui
- Input: `SPEC.md §F1`, `§F3`, `§F10`; telas das Fases 6–10
- Output: em `app/src/androidTest/` — `SignUpFlowTest`, `LoginFlowTest`, `TermosNavigationTest`, `LogoutTest`. `ExampleInstrumentedTest` e `ExampleUnitTest` (placeholders de `SPEC.md §9.5`) removidos.
- Testes críticos:
  - [ ] Preencher o cadastro e submeter navega para o catálogo
  - [ ] Ir para os Termos e voltar preserva nome e telefone digitados (`§F10`)
  - [ ] Após logout, o botão voltar do sistema não retorna à área autenticada (`§F3`)
  - [ ] `test ! -f app/src/test/java/com/example/merceariadamaria/ExampleUnitTest.kt`

---

## Paralelismo e contagem de agents

**Ondas (waves) — cada onda é um conjunto de tasks que podem rodar simultaneamente:**

| Wave | Fase | Tasks em paralelo | Nº |
|---|---|---|---|
| 1 | Fase 0 | 0.1, 0.2, 0.3, 0.4 | 4 |
| 2 | Fase 1 | 1.1, 1.2, 1.3, 1.4 | 4 |
| 3 | Fase 2 | 2.1 | 1 |
| 4 | Fase 3 | 3.1, 3.2 | 2 |
| 5 | Fase 4 | 4.1, 4.2, 4.3 | 3 |
| 6 | Fase 5 | 5.1, 5.2 | 2 |
| 7 | Fase 6 | 6.1, 6.2, 6.3 | 3 |
| 8 | Fase 7 | 7.1, 7.2 | 2 |
| 9 | Fase 8 | 8.1, 8.2 | 2 |
| 10 | Fase 9 | 9.1, 9.2, 9.3 | 3 |
| 11 | Fase 10 | 10.1, 10.2, 10.3 | 3 |
| 12 | Fase 11 | 11.1, 11.2, 11.3 | 3 |

**Total: 32 tasks em 12 fases / 3 sprints.**

**Concorrência máxima: 4 agents simultâneos** (Wave 1 e Wave 2). Uma execução sequencial usa
1 agent; a paralela nunca precisa de mais de 4 ao mesmo tempo.

**Agents por papel** (definidos em `.claude/agents/`), com as tasks de cada um:

| Agent | Tasks | Total |
|---|---|---|
| `build-engineer` | 0.1 | 1 |
| `compose-ui` | 0.2, 0.4, 5.2, 6.1, 6.2, 6.3, 7.1, 7.2, 8.1, 8.2, 9.1, 9.2, 9.3, 10.1, 10.2, 10.3, 11.1, 11.3 | 18 |
| `core-kotlin` | 1.1, 1.2, 1.3, 1.4 | 4 |
| `firebase-data` | 0.3, 2.1, 3.1, 3.2, 4.1, 4.2, 4.3, 5.1 | 8 |
| `firestore-rules-auditor` | 11.2 | 1 |
| `test-runner` | roda os gates de todas as 12 fases | — |
| `code-reviewer` | revisa **toda** task ao final, antes de avançar | — |

**Verificação da regra anti-conflito** (tasks da mesma fase não tocam os mesmos arquivos):

| Fase | Arquivos por task |
|---|---|
| 0 | 0.1 = arquivos gradle · 0.2 = `AppComponents.kt` + `SignUpScreen.kt` + remove `legacy/` · 0.3 = `firestore.rules` + `firestore.indexes.json` · 0.4 = `Theme.kt` + `strings.xml` |
| 1 | 1.1 = `Formatadores.kt` · 1.2 = `AuthConstants.kt` · 1.3 = `Validadores.kt` · 1.4 = `Descricoes.kt` |
| 3 | 3.1 = `data/mapper/` · 3.2 = `MerceariaApplication.kt` + `AndroidManifest.xml` |
| 4 | 4.1 = `UsuarioRepositoryFirebase.kt` · 4.2 = `ProdutoRepositoryFirebase.kt` · 4.3 = `FiadoRepositoryFirebase.kt` |
| 5 | 5.1 = `di/` · 5.2 = `ui/navigation/AppNavHost.kt` |
| 6 | 6.1 = `ui/screen/signup/` · 6.2 = `ui/screen/login/` · 6.3 = `ui/screen/splash/` |
| 7 | 7.1 = `ui/screen/catalogo/` · 7.2 = `ui/screen/gestaoprodutos/ProdutosList*` |
| 8 | 8.1 = `ProdutoForm*` · 8.2 = `ProdutosListViewModel.kt` + `ProdutosListScreen.kt` |
| 9 | 9.1 = `Devedores*` · 9.2 = `Extrato*` · 9.3 = `ui/screen/meufiado/` |
| 10 | 10.1 = `ui/screen/gestaoclientes/` · 10.2 = `NovoDebito*` · 10.3 = `NovoCredito*` |
| 11 | 11.1 = telas existentes · 11.2 = `docs/verificacao-regras.md` · 11.3 = `app/src/androidTest/` |

**Ponto de atenção da Fase 8:** 8.2 edita `ProdutosListViewModel.kt`, criado em 7.2 (fase
anterior — sem conflito), e 8.1 edita só os arquivos `ProdutoForm*`. Se as duas precisarem tocar
`ProdutosListScreen.kt`, rode-as **sequencialmente** (8.1 primeiro).

**Ponto de atenção geral:** `ui/navigation/AppNavHost.kt` é editado **apenas** pela Task 5.2, que
registra todas as rotas com placeholders. Nenhuma task das Fases 6–10 edita esse arquivo — elas
só substituem o corpo das telas nas próprias pastas. Sem essa regra, 15 tasks disputariam o
mesmo arquivo.

---

## Próxima sessão

_(Preencher no encerramento de cada sessão — ver `/entrega`.)_

- **Onde paramos:** Harness recém-criado. Nenhuma task do PLAN executada ainda.
- **Estado do build:** `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` →
  `BUILD SUCCESSFUL` no baseline (antes de qualquer feature), ~21 s.
- **Bloqueio ativo:** os 5 pré-requisitos de `CLAUDE.md §3` são **humanos** e ainda não foram
  confirmados. A Task 0.1 falha sem `app/google-services.json`.
- **Próxima ação:** confirmar o setup do Firebase e rodar a **Wave 1** (Fase 0: tasks 0.1, 0.2,
  0.3, 0.4). Se o setup do Firebase ainda não estiver pronto, comece pela **Fase 1** (`core/`),
  que é Kotlin puro e não depende do Firebase — e volte à 0.1 depois.
- **Decisões pendentes:** nenhuma.
