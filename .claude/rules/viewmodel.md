---
paths:
  - "app/src/main/java/com/example/merceariadamaria/ui/screen/**/*ViewModel.kt"
  - "app/src/main/java/com/example/merceariadamaria/ui/screen/**/*UiState.kt"
---
# Regras de ViewModel e UiState

- **Um `UiState` imutável por tela** (`data class`), exposto como `StateFlow<XUiState>`. A tela
  coleta com `collectAsStateWithLifecycle()`. Nunca `MutableStateFlow` público.
- **Nenhum indicador de carregamento sem saída.** Todo caminho que liga `isLoading = true`
  precisa levá-lo de volta a `false` em **três** cenários: sucesso, erro e timeout. Um `catch`
  que esquece o `finally` deixa a tela girando para sempre.
- **`isSubmitting` bloqueia reentrada.** Duplo toque em "Criar conta" / "Salvar" / "Lançar" não
  pode gerar dois documentos (`SPEC.md B25`).
- **Erro é `String?` no UiState**, já traduzido para a mensagem da SPEC. O ViewModel não repassa
  `Throwable` nem `e.message` para a tela.
- **Erro por campo** (`nameError`, `phoneError`, …) e erro geral (`error`) são campos separados:
  o primeiro pinta o campo, o segundo vira snackbar.
- **`touched` por campo:** o erro de validação só aparece depois do primeiro `onValueChange`
  daquele campo — senão a tela abre toda vermelha.
- **`isFormValid` é derivado**, nunca setado à mão: `true` se e somente se todos os erros são
  `null` e nenhum campo obrigatório está vazio.
- **Validação local antes de qualquer I/O.** Submit com formulário inválido não chama repositório.
- **Teste com fake escrito à mão** de `app/src/test/.../fake/`, `runTest` e `TestDispatcher`.
  Sem mockk, sem Firebase real. O teste de ViewModel é o teste principal desta camada.
- ViewModel recebe repositório por construtor, via `ViewModelProvider.Factory` (`SPEC.md §8.8`).
  Nunca instancia Firebase nem lê `AppContainer` direto de dentro.
