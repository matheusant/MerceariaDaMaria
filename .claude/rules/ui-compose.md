---
paths:
  - "app/src/main/java/com/example/merceariadamaria/ui/**/*.kt"
  - "app/src/main/java/com/example/merceariadamaria/components/*.kt"
  - "app/src/main/java/com/example/merceariadamaria/MainActivity.kt"
---
# Regras da camada Compose

- **Composable é stateless.** Recebe `value` + `onValueChange`. `remember { mutableStateOf() }`
  é permitido **só** para estado puramente visual: scroll, expandido/colapsado, visibilidade de
  senha. Dado de formulário ou de domínio ali é o bug de `SPEC.md §9.1` voltando, e não
  sobrevive à rotação (`B24`).
- **Nada de `import com.google.firebase.*`.** O dado chega por interface de repositório injetada
  pelo `AppContainer`.
- **Material 3 apenas.** `HorizontalDivider` em vez de `Divider` (M2); `Text` com
  `LinkAnnotation` em vez de `ClickableText`.
- **Toda string visível vem de `strings.xml`** via `stringResource`.
- **`contentDescription`:** `null` para ícone decorativo, texto para ícone interativo. `""` está
  errado nos dois casos.
- **Tela com formulário usa `verticalScroll` + `imePadding`** — senão o botão fica atrás do
  teclado em tela pequena (`SPEC.md §9.7`).
- **Lista vazia mostra texto**, nunca tela em branco: "Nenhum produto encontrado", "Ninguém está
  devendo agora", "Você está em dia".
- **Preço nunca é exibido cru.** Sempre `formatarBrl(centavos)`. Preço digitado em reais vira
  `Long` em centavos sem passar por `Double`/`Float`.
- **`ui/navigation/AppNavHost.kt` é editado apenas pela Task 5.2 do `PLAN.md`.** Nas tasks das
  Fases 6–10, preencha o corpo das telas nas próprias pastas; se a rota não existir lá, pare e
  sinalize. Sem isso, 15 tasks disputam o mesmo arquivo.
- Uma pasta por feature em `ui/screen/<feature>/`: `XScreen.kt`, `XViewModel.kt`, `XUiState.kt`.
