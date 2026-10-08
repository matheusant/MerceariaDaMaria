# Troco 🛒

Aplicativo Android nativo desenvolvido em **Kotlin** e **Jetpack Compose**, integrando-se com **Firebase (Authentication + Cloud Firestore)** para gerenciar as operações de uma mercearia de bairro e facilitar a interação entre clientes e comerciantes.

---

## 🎯 Objetivo do Projeto

**Troco** tem como objetivo digitalizar e otimizar a gestão de um mercadinho tradicional de bairro, oferecendo uma experiência moderna e prática tanto para a lojista quanto para os clientes da comunidade.

O sistema divide-se em dois perfis principais de usuário:
- **Cliente:** Pode navegar pelo catálogo de produtos da mercearia, consultar preços, verificar o histórico de compras e acompanhar o extrato do seu fiado ("caderneta").
- **Admin:** Possui ferramentas completas para gerenciamento de estoque e catálogo de produtos, além do controle rigoroso da caderneta de fiado dos clientes (registro de débitos, pagamentos e saldos devedores).

---

## 🛠️ Tecnologias e Arquitetura

O projeto foi construído utilizando as melhores práticas e ferramentas modernas do ecossistema Android:

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **Interface de Usuário:** [Jetpack Compose](https://developer.android.com/jetpack/compose) com Material 3 (UI 100% declarativa).
- **Arquitetura:** Clean Architecture / MVVM (Model-View-ViewModel) com separação clara de responsabilidades (Camadas de `data`, `domain` e `ui`).
- **Backend & Banco de Dados em Nuvem:** [Firebase Authentication](https://firebase.google.com/docs/auth) e [Cloud Firestore](https://firebase.google.com/docs/firestore).
- **Banco de Dados Local & Offline-First:** [Room](https://developer.android.com/training/data-storage/room) já implementado para persistência e cache local (estratégia offline-first). O **WorkManager** será integrado em breve para gerenciar a sincronização em segundo plano com o Firestore.
- **Assincronicidade:** Kotlin Coroutines & Flow.
- **Navegação:** Jetpack Navigation Compose.

---

## 👥 Perfis de Acesso

1. **Cliente:**
   - Visualização do catálogo de produtos disponíveis.
   - Consulta do próprio saldo e histórico de fiado.
   - Perfil do usuário.

2. **Administrador (Admin):**
   - Gestão de produtos (Adicionar, editar, remover, atualizar estoque e preços).
   - Gerenciamento de fiados e caderneta dos clientes.

---

## 📂 Documentação de Apoio

Para detalhes técnicos avançados, regras de negócio e planejamento, consulte os arquivos na raiz do projeto:
- [`SPEC.md`](SPEC.md) — Especificação técnica e funcional completa (Contrato do projeto).
- [`PLAN.md`](PLAN.md) — Planejamento de fases, sprints e tarefas de desenvolvimento.
- [`CLAUDE.md`](CLAUDE.md) — Diretrizes de desenvolvimento e engenharia.

---

## 🚀 Como Executar o Projeto

1. **Pré-requisitos:**
   - Android Studio (versão Hedgehog ou superior recomendada).
   - JDK 17.
   - Configurar o projeto no Firebase Console e adicionar o arquivo `app/google-services.json`.

2. **Compilação e Testes (via Gradle):**
   ```bash
   # Compilar o código Kotlin em modo debug
   ./gradlew :app:compileDebugKotlin

   # Executar testes unitários
   ./gradlew :app:testDebugUnitTest
   ```

---

*Este README será atualizado continuamente conforme novas funcionalidades e evoluções forem implementadas no projeto.*
