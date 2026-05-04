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

## Tests

- **Unit tests** (`app/src/test`): JUnit4 + Truth + MockK + Turbine + `kotlinx-coroutines-test`.
- **Instrumentation tests** (`app/src/androidTest`): Espresso + Hilt testing. Custom runner is `hu.mostoha.mobile.android.huki.HiltTestRunner` (set in `defaultConfig.testInstrumentationRunner`).
- Both test source sets depend on `:test-data` for fixtures.
