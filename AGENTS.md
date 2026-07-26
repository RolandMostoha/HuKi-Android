# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

HuKi is an Android hiking app for Hungary, built around OpenStreetMap data and the OsmDroid tile renderer. It is published to Google Play under `hu.mostoha.mobile.android.huki`.

## Build & Test Commands

The project uses Gradle (Kotlin DSL). All commands run from the repo root.

- Build debug APK: `./gradlew assembleDebug`
- Static analysis: `./gradlew lint detekt` (detekt config at `tools/quality/HuKi-detekt.yml`, `allRules = true`, `buildUponDefaultConfig = true`)
- Unit tests: `./gradlew testDebugUnitTest`
- Single unit test class: `./gradlew :app:testDebugUnitTest --tests "hu.mostoha.mobile.android.huki.<FQCN>"`
- Instrumentation test APK: `./gradlew assembleDebugAndroidTest`
- Run instrumentation tests on a connected device: `./gradlew connectedDebugAndroidTest` (uses AndroidX Test Orchestrator + `clearPackageData`)
- E2E helper scripts: `tools/scripts/run-e2e-tests.sh`. Other dev helpers in `tools/scripts/` toggle dark mode, locale, animations, internet, etc.

## Architecture

A single-Activity MVVM app (no Fragments, no Jetpack Navigation). The layered flow is:

```
Activity / Views ── ViewModel (StateFlow) ── Interactor (Flow) ── Repository (suspend) ── Network / Local
```

Layer responsibilities (see `README.md` for the canonical table):

- **`ui/`** — `HomeActivity` is the only screen. UI state is driven by multiple `StateFlow`s on `HomeViewModel` and feature-specific ViewModels (`LayersViewModel`, etc.). ViewModels map domain models → UI models.
- **`interactor/`** — converts `suspend` repository calls into `Flow` streams and orchestrates repository calls.
- **`repository/`** — fetches data via network or local sources using `suspend` functions; maps network DTOs to domain models. Examples: `OsmPlacesRepository`, `FileBasedHikingLayerRepository`.
- **`model/domain/`** vs **`model/network/`** vs **`model/ui/`** — keep boundaries strict; mapping happens at repo (network→domain) and VM (domain→ui) layers.
- **`osmdroid/`** — custom `TileSource`s, overlays, and OsmDroid integration (this is where map rendering lives, not in `ui/`).
- **`di/`** — Hilt modules. App entry point is `HukiApplication` (`@HiltAndroidApp`).
- **`service/`** — foreground/location services (Google Fused Location Provider integrated with OsmDroid).
- **`database/`** — Room.
- **`network/`** — Retrofit + Moshi (codegen via KSP) + OkHttp logging interceptor.
- **`provider/`, `configuration/`, `data/`** — DataStore Preferences, remote/feature config, static data.

### Modules

- `:app` — main Android app.
- `:osm-overpasser` — internal Overpass API client used as a library module.
- `:test-data` — shared test fixtures, depended on by both `testImplementation` and `androidTestImplementation` in `:app`.

### General
- Don't fight the framework → use the native side best practices, avoid platform anti-patterns
- Use comments only if necessary. If necessary, preferred: 1 line, max: 2 lines. If need more than 3 lines: ask.

## Tests

- **Unit tests** (`app/src/test`): JUnit4 + Truth + MockK + Turbine + `kotlinx-coroutines-test`.
- **Instrumentation tests** (`app/src/androidTest`): Espresso + Hilt testing. Custom runner is `hu.mostoha.mobile.android.huki.HiltTestRunner` (set in `defaultConfig.testInstrumentationRunner`).
- Both test source sets depend on `:test-data` for fixtures.

## Mappers
- Type mappers between layers (data↔domain, domain↔domain, platform↔domain) are **top-level extension functions** named `to<Target>()`, grouped by the domain concept they map in `model/mapper/<Concept>Mapper.kt` (e.g. `GpxMapper.kt`, `MapboxMapper.kt`).
- Do **not** place mappers on the model classes themselves: a `model/data` class must not import `model/domain` types (and vice versa), so co-locating a mapper in the model file leaks a cross-layer dependency.
- Keep mappers out of repositories/ViewModels — they belong in `model/mapper` so they stay reusable and unit-testable.

## Chores

Chores is a checklist which should be checked for every "feature complete" code review.

- Unit tests
- Instrumentation tests (e.g. Repository tests)
- UI tests (Espresso E2E) - should work on both platforms
- Lint passes — detekt, lint
- Potential re-usable View UI components
- UI styling - Material Design
- Use official Material Design, avoid custom UI solutions
- Dark mode (Colors)
- Device landscape mode
- Translations
- Analytics: check for worth-to-measure events
- Always ask: what happens with this feature in offline mode? → for a hiking app offline mode is crucial
- Permissions denied / not-granted paths
- Docs updated — AGENTS.md / README.md
