# Repository Guidelines

## Project Structure & Module Organization

This is a multi-module Android app built with Kotlin and Jetpack Compose. The application entry point is in `app/`. Shared layers live under `core/`: `model` (domain models), `domain` (repository contracts), `data` (Room/data implementations), `ui` (theme and reusable UI), `navigation` (shared route contracts), `pdf`, and `dice`. Feature code is under `feature/compendium`, `feature/character`, `feature/settings`, `feature/wizard`, and `feature/dice`; `character/` is a legacy exception module under active migration. Production sources are in each module's `src/main`, JVM tests in `src/test`, and instrumented tests in `src/androidTest`.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper from the repository root:

```powershell
.\gradlew.bat assembleDebug       # Build the debug APK
.\gradlew.bat test                # Run all local JVM tests
.\gradlew.bat connectedCheck      # Run instrumented tests on a device/emulator
.\gradlew.bat :app:lint           # Run Android lint for the app
```

Android Studio can install/run the `app` configuration. Prefer targeted tasks while iterating, such as `:feature:compendium:test`.

## Coding Style & Naming Conventions

Follow Kotlin official formatting: four-space indentation, trailing commas where the formatter adds them, `PascalCase` types/composables, `camelCase` functions/properties, and descriptive `*ViewModel`, `*Screen`, `*Navigation`, and `*NavGraph` names. Keep state in ViewModels and expose `StateFlow`; collect it with lifecycle-aware Compose APIs. Use typed `@Serializable` navigation routes, small ID-based arguments, and callbacks/effects for feature boundaries. Do not place `NavController` in ViewModels; root cross-feature navigation belongs in `app`.

## Testing Guidelines

Use JUnit 4 and `kotlinx-coroutines-test` for unit tests, with Compose/instrumentation tests for UI behavior. Name tests by behavior (for example, ``givenValidInput_whenSaved_thenRepositoryUpdates``). Add or update tests with every navigation or ViewModel behavior change, and run the narrow module test before the full suite.

## Commit & Pull Request Guidelines

Existing commits use short, imperative-style prefixes such as `ci: test workflow` and `fix: make gradlew executable`. Keep commits focused and use a concise `<type>: <change>` subject (`feat`, `fix`, `test`, `refactor`, `ci`, or `docs`). Pull requests should explain the user-visible and architectural impact, list verification commands, link the relevant issue when one exists, and include screenshots or recordings for Compose UI changes. Call out module/dependency changes explicitly.

## Security & Configuration Tips

Do not commit signing keys, OAuth credentials, Drive tokens, generated APKs, or local Gradle/build output. Keep secrets in local or CI-provided configuration, and review dependency changes in `gradle/libs.versions.toml`.
