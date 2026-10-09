# CLAUDE.md — Mercearia da Maria

> Contexto que **todo** agent recebe ao entrar neste projeto. Contém só o que não é inferível
> lendo o código. Detalhes de um módulo específico ficam em `.claude/rules/`; workflows
> multi-passo ficam em `.claude/skills/`.

---

## 1. O que é este projeto

App Android nativo (Kotlin + Jetpack Compose) para uma mercearia de bairro, com **Firebase**
(Authentication + Cloud Firestore) como back-end. Dois perfis: **CLIENTE** (vê catálogo e o
próprio fiado) e **ADMIN** (a Maria: mantém produtos e a caderneta de fiado).

**Três coleções no Firestore, e somente três:** `usuarios`, `produtos`, `fiados`.

O contrato completo está em **`SPEC.md`**. A ordem de execução está em **`PLAN.md`**.
Nenhuma implementação sem seção correspondente na SPEC.

---

## 2. Stack exata (fonte única de versões: `gradle/libs.versions.toml`)

| Item | Versão | Observação |
|---|---|---|
| AGP | 8.6.0 | |
| Kotlin | 1.9.0 | Compose Compiler Extension **1.5.1** — casado com esta versão |
| JDK local | 17 | mas `sourceCompatibility`/`jvmTarget` = **1.8** |
| compileSdk / targetSdk / minSdk | 36 / 36 / **26** | |
| Compose BOM | 2024.04.01 | Material 3 |
| navigation-compose | 2.9.4 | |
| Firebase BOM | 33.1.2 | auth + firestore, versões via BOM |
| google-services (plugin) | 4.4.2 | |
| coroutines-play-services | 1.8.1 | `.await()` em `Task<T>` |
| lifecycle-viewmodel-compose | 2.8.4 | |
| JUnit 4 / coroutines-test | 4.13.2 / 1.8.1 | |

**Toda dependência nova entra em `gradle/libs.versions.toml` primeiro**, nunca com string
literal em `app/build.gradle.kts`. Adicionar dependência fora da lista de `SPEC.md §7` exige
aprovação humana — é mudança de stack.

**Não mudar** `jvmTarget`, `sourceCompatibility`, `minSdk`, `compileSdk` nem a versão do Kotlin.
Se o Firebase exigir desugaring, o caminho aprovado está em `SPEC.md §7` (nota de build).

---

## 3. Pré-requisitos de setup (sem eles o build FALHA)

Ações humanas, feitas uma vez. Um agent **não** consegue executá-las.

1. Projeto criado no Console do Firebase, com app Android registrado no package
   `com.example.merceariadamaria`.
2. **`app/google-services.json` presente.** Sem esse arquivo o plugin `google-services` falha o
   build inteiro. É o primeiro suspeito de qualquer erro de configuração de build.
3. Provider **Email/Password** habilitado em Authentication → Sign-in method.
4. Firestore criado, com `firestore.rules` e `firestore.indexes.json` publicados.
5. **Conta ADMIN da Maria criada no Console** (o app nunca cria ADMIN — ver `SPEC.md §2.3`):
   Auth → Add user → `11900000000@merceariadamaria.app`; depois um documento em
   `usuarios/{uid}` com `perfil: "ADMIN"`.

Se o build falhar com erro do `google-services`, **não** contorne removendo o plugin: pare e
avise que falta o passo 2.

---

## 4. Comandos

```bash
# GATE MÍNIMO — tem de passar antes de qualquer entrega
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest

# individualmente
./gradlew :app:compileDebugKotlin      # compila
./gradlew :app:testDebugUnitTest       # testes unitários JVM (rápido, sem device)
./gradlew :app:assembleDebug           # gera o APK debug
./gradlew :app:lintDebug               # lint do AGP
./gradlew :app:connectedDebugAndroidTest   # testes de UI Compose — EXIGE device/emulador

# um teste só
./gradlew :app:testDebugUnitTest --tests "*SignUpViewModelTest*"
```

**Windows.** O shell padrão da sessão é PowerShell; o Bash tool é Git Bash. `./gradlew` funciona
nos dois. Em PowerShell, `&&` não existe — use `;` ou `if ($?) { ... }`. Scripts de hook são
sempre invocados como `bash .claude/hooks/...`, nunca direto.

**`jq` não está instalado nesta máquina.** Hooks fazem parsing de JSON com `grep`/`sed`.

---

## 5. Estrutura de pastas (alvo)

```
SPEC.md                  # o contrato: o QUE construir
PLAN.md                  # a ordem: sprints, fases, tasks, gates
CLAUDE.md                # este arquivo: COMO trabalhar aqui
firestore.rules          # autorização real (SPEC §F11) — versionado
firestore.indexes.json   # índices compostos (SPEC §5.6) — versionado
.claudeignore            # o que os agents não leem/editam
.claude/                 # o Harness (ver §9)

app/src/main/java/com/example/merceariadamaria/
├── MerceariaApplication.kt   # Application: monta AppContainer + firestoreSettings (uma vez!)
├── MainActivity.kt           # ComponentActivity, edge-to-edge, tema
├── di/AppContainer.kt        # injeção MANUAL: Firebase + repositórios (SPEC §8.8)
├── core/                     # Kotlin puro: SEM Android, SEM Firebase, 100% testável em JVM
│   ├── Formatadores.kt       #   formatarBrl, normalizar
│   ├── Validadores.kt        #   validação de nome/telefone/senha/produto
│   └── AuthConstants.kt      #   DOMINIO_SINTETICO, emailSinteticoDe, telefoneDeEmailSintetico
├── domain/
│   ├── model/                # data classes puras (Usuario, Produto, ContaFiado, Lancamento…)
│   └── repository/           # INTERFACES de repositório (o que a UI conhece)
├── data/
│   ├── mapper/               # DocumentSnapshot → domain. NUNCA lança: erro → null + log
│   └── firebase/             # implementações dos repositórios com Auth/Firestore
├── components/               # componentes Compose reutilizáveis (fica onde está)
└── ui/
    ├── navigation/           # object Routes + NavHost + grafos por perfil
    ├── screen/<feature>/     # 1 pasta por feature: Screen.kt + ViewModel.kt + UiState.kt
    └── theme/                # Color, Theme, Type

app/src/test/java/...         # JUnit local: core, validadores, ViewModels com fakes
├── fake/                     # fakes de repositório ESCRITOS À MÃO (sem mockk)
app/src/androidTest/java/...  # testes de UI Compose (exigem device)
```

**Regra de dependência entre camadas (direção única):**
```
ui → domain ← data          core não depende de ninguém
```
- `ui` **nunca** importa `com.google.firebase.*`. Se um `import` de Firebase aparecer em
  `ui/` ou em `components/`, está errado.
- `domain` não conhece Firebase nem Android.
- `core` é Kotlin puro — é onde mora a lógica que dá teste rápido e barato.

---

## 6. Convenções (só o não-inferível)

**Dinheiro.** Sempre `Long` em centavos, nomeado com sufixo `Centavos`
(`precoCentavos`, `saldoCentavos`, `totalCentavos`). **`Double`/`Float` para valor monetário é
proibido** e o code review reprova. Formatação só na borda da UI, com `formatarBrl`.

**Idioma.** Domínio, código e mensagens em **português** (`Produto`, `saldoCentavos`,
`lancarDebito`). Palavras-chave e APIs em inglês, óbvio. Toda string visível ao usuário vai para
`res/values/strings.xml` — nada de texto hardcoded em composable.

**Estado de UI.** Vive no `ViewModel`, exposto como `StateFlow<XUiState>`. Composables são
*stateless*: recebem `value` + `onValueChange`. **Nenhum dado de formulário ou de domínio em
`remember { mutableStateOf(...) }`** — `remember` só para estado puramente visual
(scroll, expandido/colapsado). Isto é a correção do bug de `SPEC.md §9.1`.

**Erro nunca vaza cru.** Exceção do Firebase nunca chega à UI como `e.message`. Todo caminho de
erro mapeia para uma das strings definidas em `SPEC.md` (F1 tem a tabela de referência).

**Toda chamada de rede tem timeout.** `withTimeout(10_000)` em volta de qualquer `await()` de
Auth ou Firestore. Sem exceção. Motivo em `SPEC.md §F12`: offline, um `Task` de escrita nunca
completa e o `await()` pendura para sempre.

**Transação Firestore.** Todas as leituras antes de todas as escritas; validações dentro do
bloco (ele reexecuta em contenção); id de documento novo pré-gerado com `collection.document()`
porque `add()` não existe dentro de transação. Ver a sequência numerada em `SPEC.md §F6`.

**Repositório é interface.** Implementação Firebase em `data/firebase/`. Teste unitário usa
**fake escrito à mão** em `app/src/test/.../fake/` — sem mockk, sem Firebase real em teste.

**Material 3 apenas** em código novo. `androidx.compose.material` (M2) só sobrevive nos dois
usos legados que a SPEC manda substituir (`Divider` → `HorizontalDivider`, `ClickableText` →
`Text` com `LinkAnnotation`).

**`contentDescription`.** `null` para ícone decorativo, texto para ícone interativo. `""` é
errado nos dois casos.

---

## 7. TDD — teste primeiro, sem exceção

O ciclo é **RED → GREEN**, e cada task do `PLAN.md` já traz os testes críticos definidos:

1. Escreva o teste primeiro e **rode para confirmar que falha** (RED).
2. Implemente o mínimo para passar (GREEN).
3. Rode o gate mínimo (§4) e marque o checkbox no `PLAN.md`.

Onde mora a lógica testável (escreva teste aqui primeiro, é rápido e não precisa de device):
- `core/` — funções puras: `formatarBrl`, `normalizar`, `emailSinteticoDe`, `descreverItens`,
  validadores. Teste em JUnit puro.
- `ui/screen/*/XViewModel.kt` — teste com fake de repositório + `runTest`.
- Regras do Firestore (`firestore.rules`) — validadas no Rules Playground do Console, com o
  resultado registrado. Não há suíte automatizada nesta entrega.

Não escreva teste de UI Compose para lógica que cabe num teste de ViewModel: é mais lento, exige
device e falha por motivos que não são o que você quer testar.

---

## 8. Restrições — o que NUNCA fazer

1. **Não editar `SPEC.md`, `PLAN.md` nem este `CLAUDE.md` durante a execução de uma task.**
   A única exceção é marcar checkbox de task concluída no `PLAN.md`.
2. **Não mudar a stack.** Nenhuma dependência fora de `SPEC.md §7`; nenhuma mudança de
   `jvmTarget`/`minSdk`/`compileSdk`/versão de Kotlin. Se parecer necessário, **pare e pergunte**.
3. **Não tocar em `app/google-services.json`.** Não criar, não editar, não commitar substituto,
   não gerar um falso para "fazer o build passar".
4. **Não relaxar `firestore.rules`** para fazer um teste passar. Se a regra nega, o bug é na UI
   ou no repositório, não na regra.
5. **Não usar `Double`/`Float` para dinheiro.**
6. **Não deixar chamada de rede sem `withTimeout`.**
7. **Não implementar nada fora do `PLAN.md`.** Se achou um bug fora do escopo da task, registre
   e siga — não conserte de carona.
8. **Não editar arquivos gerados** (`app/build/`, `.gradle/`, `.idea/`, `local.properties`,
   `gradle/wrapper/gradle-wrapper.jar`).
9. **Não commitar segredos** (`*.jks`, `*.keystore`, `local.properties`, `.env`).
10. **Se uma task travar por 2 tentativas, pare e sinalize.** Não invente solução alternativa
    nem mude o escopo por conta própria.

---

## 9. Mapa do Harness

| Artefato | Onde | Para quê |
|---|---|---|
| **SPEC** | `SPEC.md` | O contrato: features F1–F12, schema, casos de borda B1–B28, decisões |
| **Plano** | `PLAN.md` | Sprint → Fase → Task, com gate verificável por comando |
| **Onboarding** | `CLAUDE.md` | Este arquivo |
| **Ignore** | `.claudeignore` | Gerados e sensíveis que os agents não leem |
| **Agents** | `.claude/agents/*.md` | Quem executa o quê, com tools mínimas |
| **Skills** | `.claude/skills/<nome>/SKILL.md` | Workflows sob demanda; também viram `/<nome>` |
| **Rules** | `.claude/rules/*.md` | Regras incondicionais + escopadas por path |
| **Hooks** | `.claude/hooks/<agent>/*.sh` | Enforcement determinístico (`exit 2` bloqueia) |
| **Config** | `.claude/settings.json` | Hooks globais e config compartilhada |
| **Regras Firestore** | `firestore.rules` | A autorização de verdade (`SPEC.md §F11`) |

Comandos de workflow: `/orquestrar` (executa uma fase do PLAN), `/implementar` (uma task com
TDD), `/review` (chama o `@code-reviewer`), `/entrega` (gates de encerramento).

Todo o `.claude/` é arquitetura e **deve ser commitado**.

---

## 10. Gate mínimo de entrega

Nada é considerado pronto sem isto:

```bash
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```

Deve terminar em `BUILD SUCCESSFUL`. Referência: no baseline (antes de qualquer feature) este
comando passa em ~21 s.

Além do build: os checkboxes da task marcados no `PLAN.md`, e o `@code-reviewer` sem nenhum
achado **BLOQUEANTE**.
