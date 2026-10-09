---
name: nova-tela
description: >
  Cria uma tela Compose completa neste projeto: UiState imutável, ViewModel com StateFlow e
  fábrica, Screen stateless, strings em strings.xml, registro de rota e teste de ViewModel com
  fake. Use sempre que o pedido envolver criar ou adicionar uma tela, um ecrã, um ViewModel ou um
  UiState. Exemplos de trigger: "criar a tela de login", "nova tela de catálogo", "adicionar
  ViewModel para o fiado", "preciso de um UiState para o formulário de produto".
---

# Nova tela Compose

Argumento: `$ARGUMENTS` — o nome da feature (ex.: `catalogo`, `novo-debito`).

## Quando usar

- Criar uma tela nova em `ui/screen/<feature>/`.
- Refatorar uma tela existente para o padrão UiState + ViewModel.

## Quando NÃO usar

- **Componente reutilizável** sem tela própria — vai em `components/AppComponents.kt`.
- **Rota nova** que não existe no `object Routes` — pare e sinalize: `AppNavHost.kt` é editado
  apenas pela Task 5.2 do `PLAN.md`, senão 15 tasks disputam o mesmo arquivo.
- **Lógica pura** (formatação, validação) — vai em `core/`, com o agent `core-kotlin`.
- **Acesso a dado** — vai em `data/`, com o agent `firebase-data`. Use `/novo-repositorio`.

## Passo a passo

### 1. Confirme a rota e leia a SPEC
A rota já tem de existir em `Routes` com um `composable()` registrado (`SPEC.md §5.7`). Leia a
feature correspondente na SPEC: as strings de erro, os estados vazios e os exemplos são contrato.

### 2. Crie os 3 arquivos em `ui/screen/<feature>/`

`<Feature>UiState.kt` — imutável, com valores default:
```kotlin
data class CatalogoUiState(
    val busca: String = "",
    val produtos: List<Produto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val avisoOffline: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && produtos.isEmpty()
}
```

`<Feature>ViewModel.kt` — `StateFlow` privado com backing, repositório por construtor:
```kotlin
class CatalogoViewModel(private val repo: ProdutoRepository) : ViewModel() {
    private val _state = MutableStateFlow(CatalogoUiState())
    val state: StateFlow<CatalogoUiState> = _state.asStateFlow()
    // eventos como funções públicas: onBuscaChanged(...), onRetry()
}
```

`<Feature>Screen.kt` — stateless, recebe o estado e lambdas:
```kotlin
@Composable
fun CatalogoScreen(state: CatalogoUiState, onBuscaChanged: (String) -> Unit, onRetry: () -> Unit)
```

### 3. Respeite as regras da camada
- Estado de domínio **nunca** em `remember` — só scroll, expandido/colapsado, senha visível.
- Nenhum `import com.google.firebase.*`.
- Preço só via `formatarBrl`; nunca `Double`/`Float` para dinheiro.
- Toda string visível em `strings.xml`, via `stringResource`.
- Estado vazio com texto; nunca tela em branco.
- `contentDescription`: `null` se decorativo, texto se interativo.
- Formulário: `verticalScroll` + `imePadding`.
- Todo `isLoading = true` tem caminho garantido para `false` em sucesso, erro **e** timeout.

### 4. Teste o ViewModel primeiro (RED → GREEN)
Em `app/src/test/.../ui/screen/<feature>/`, com o fake de `test/.../fake/`, `runTest` e
`TestDispatcher`. Cubra: caminho feliz, cada validação, um caminho de erro com a mensagem exata da
SPEC, e a prova de que ação inválida **não** chama o repositório.

### 5. Verifique
```bash
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```
Depois `/review`.

## Exemplo

**Input:** `/nova-tela catalogo`

**Output esperado:**
```
ui/screen/catalogo/CatalogoUiState.kt   (data class + isEmpty derivado)
ui/screen/catalogo/CatalogoViewModel.kt (StateFlow, debounce 300ms, ProdutoRepository)
ui/screen/catalogo/CatalogoScreen.kt    (stateless, LazyColumn, "Sem estoque", estado vazio)
res/values/strings.xml                  (+3 strings)
test/.../catalogo/CatalogoViewModelTest.kt (7 testes: RED 7/7 → GREEN 7/7)

Rota Routes.CATALOGO já registrada na Task 5.2 — AppNavHost.kt não foi tocado.
BUILD SUCCESSFUL
```
