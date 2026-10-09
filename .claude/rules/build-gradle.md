---
paths:
  - "**/*.gradle.kts"
  - "gradle/libs.versions.toml"
  - "gradle.properties"
---
# Regras de configuração de build

- **Versão vive no catálogo.** Toda dependência entra primeiro em `gradle/libs.versions.toml`;
  `app/build.gradle.kts` referencia por alias (`libs.firebase.auth`). Coordenada Maven literal
  com versão é proibida — a única exceção aprovada é
  `coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")` (`SPEC.md §7`).
- **Nenhuma dependência fora de `SPEC.md §7`** sem aprovação humana. É mudança de stack.
- **Estes valores são congelados** e mudá-los exige aprovação humana explícita:
  `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`, `jvmTarget = "1.8"`,
  `sourceCompatibility/targetCompatibility = VERSION_1_8`, Kotlin `1.9.0`, AGP `8.6.0`.
- **Kotlin 1.9.0 está casado com `kotlinCompilerExtensionVersion = "1.5.1"`.** Trocar um sem o
  outro quebra o build do Compose.
- **Se o Firebase exigir APIs Java 8+**, o único caminho aprovado é
  `isCoreLibraryDesugaringEnabled = true` + `desugar_jdk_libs`. **Não** suba o `jvmTarget`.
- **O plugin `google-services` tem de continuar aplicado** enquanto houver dependência do
  Firebase. Removê-lo é o contorno proibido para a ausência de `app/google-services.json`: sem o
  plugin, o JSON não é processado e o Firebase falha em runtime com erro obscuro.
- **Nunca editar** `local.properties` (caminho do SDK desta máquina) nem `gradle/wrapper/`.
- Bibliotecas proibidas sem aprovação: Retrofit/OkHttp/Ktor, Hilt/Koin, Room, Firebase
  Analytics/Crashlytics/Messaging/Storage, mocking framework, biblioteca de imagem remota.
- Depois de mexer em resolução de dependência, confirme com
  `./gradlew :app:dependencies --configuration debugRuntimeClasspath`.
