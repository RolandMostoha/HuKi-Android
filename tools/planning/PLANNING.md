# HuKi-Android — Plan Board

## Legend

| Status | Meaning                     |
|--------|-----------------------------|
| `[ ]`  | Not started                 |
| `[L]`  | Required for Go-Live        |
| `[~]`  | In progress                 |
| `[x]`  | Done                        |
| `[-]`  | Cancelled / deprioritized   |
| `[?]`  | Questionable / spike needed |

---

## Backlog

### General / tech tasks

| Status | Feature                                        |
|--------|------------------------------------------------|
| `[x]`  | Bug: Location IQ rate limits                   |
| `[~]`  | Google Play Billing SDK update -> Aug deadline |
| `[x]`  | Update OKT routes                              |

### Bugs

| Status | Scope | Bug                                                      |
|--------|-------|----------------------------------------------------------|
| `[ ]`  | GPX   | LocationIQ rate limits on Route Planner / Waypoints      |
| `[ ]`  | GPX   | BUG: GPX roundtrip distance to my location not displayed |

### FEATURE: Map

| Status | Scope | Task                                          |
|--------|-------|-----------------------------------------------|
| `[ ]`  | Map   | Status message on hike mode changes           |
| `[ ]`  | Map   | Offline detection + status message at the top |

### FEATURE: HikingRoutes

| Status | Scope        | Task                                   |
|--------|--------------|----------------------------------------|
| `[ ]`  | HikingRoutes | Convert Hiking Route to GPX + altitude |

### FEATURE: RoutePlanner

| Status | Scope        | Task                                              |
|--------|--------------|---------------------------------------------------|
| `[ ]`  | RoutePlanner | Update Route Planner settings icon for visibility |

### FEATURE: Favourites / Flags

| Status | Scope      | Task                                                  |
|--------|------------|-------------------------------------------------------|
| `[ ]`  | Favourites | Feature: Waypoints only GPX creation in route planner |

### FEATURE: Billing / Supporters

Two-release plan to reach Billing 8.x without existing one-time supporters losing their badge.

**Why two releases:** Billing 8.x removes `queryPurchaseHistoryAsync` entirely, and there is no
Play Developer API endpoint to look up a user's lifetime purchases (only per-purchase-token
lookups). One-time products are consumed on purchase so they can be re-bought, so after the
upgrade Play can no longer tell us who supported before. Release 1 (still on 7.1.1) reads the
legacy history one last time and backfills it into local DataStore; release 2 does the upgrade
and reads only that local record.

#### Release 1 — bridge (Billing 7.1.1 + migration)

| Status | Scope   | Task                                                                                                  |
|--------|---------|-------------------------------------------------------------------------------------------------------|
| `[x]`  | Billing | `SupportRepository` + `SupportMapper`: persist one-time purchases with counts                         |
| `[x]`  | Billing | `migrateLegacyOneTimePurchaseHistory()`: backfill from `queryPurchaseHistory`                         |
| `[x]`  | Billing | `recordAndConsumeOwnedOneTimePurchases()`: sweep stuck unconsumed purchases                           |
| `[x]`  | Billing | Supporter badge shows purchase count (`2x` + product icon)                                            |
| `[x]`  | Billing | Recording is idempotent per purchase token (`RECORDED_PURCHASE_TOKENS`), so a re-swept or re-backfilled purchase never counts twice |
| `[x]`  | Billing | Migration is one-shot per install (`LEGACY_HISTORY_MIGRATED`), set only after Play answers `OK`       |
| `[x]`  | Billing | `billing_legacy_purchase_backfilled` analytics event — measures release 1 adoption for the gate below |
| `[x]`  | Billing | `SupportMapperTest` + `ProductsUiModelMapperTest` unit tests, `SupportRepositoryTest` instrumentation tests |
| `[ ]`  | Billing | Manually verify on device: purchase -> badge -> re-purchase -> count increments                       |
| `[ ]`  | Billing | Manually verify: kill the app between purchase and consume -> next launch sweeps it, count stays right |
| `[ ]`  | Billing | Restore `applicationIdSuffix = ".debug"` in `app/build.gradle.kts` (commented out for billing testing) |
| `[L]`  | Billing | Ship release 1 to production, keep `billing = "7.1.1"` — **must be submitted before end of Aug 2026** |

#### Release 2 — upgrade (Billing 8.3.0), after release 1 has soaked

No hard deadline: the Google cutoff applies to new submissions only, so the published 7.1.1
build stays live and serving. Release 2 can wait as long as needed for adoption. Caveat: once
the cutoff passes, *any* update — including an unrelated hotfix — must already be on 8.x, so
release 2 becomes a prerequisite for shipping anything else.

| Status | Scope   | Task                                                                                 |
|--------|---------|--------------------------------------------------------------------------------------|
| `[ ]`  | Billing | Check adoption of release 1 before starting: `billing_legacy_purchase_backfilled` in Firebase (see risk below) |
| `[ ]`  | Billing | Bump `billing = "8.3.0"` in `gradle/libs.versions.toml`                              |
| `[ ]`  | Billing | Delete `migrateLegacyOneTimePurchaseHistory()` + its `QueryPurchaseHistory*` imports |
| `[ ]`  | Billing | Delete `SupportRepository.recordLegacyPurchase()` + `is/setLegacyHistoryMigrated()` + the `LEGACY_HISTORY_MIGRATED` key. Keep `RECORDED_PURCHASE_TOKENS`: it guards the consume sweep, not the migration |
| `[ ]`  | Billing | Delete `AnalyticsService.legacyPurchaseBackfilled()` + its Firebase/Fake implementations |
| `[ ]`  | Billing | Re-verify badge + re-purchase flow still work on 8.3.0                               |
| `[ ]`  | Billing | Review/trim the temporary `Timber.d("Billing: ...")` logs in `ProductsViewModel`     |

#### Risks / known limitations

| Status | Scope   | Note                                                                                                                                                                                                                                                           |
|--------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[x]`  | Billing | ~~**Timing:** Aug deadline leaves little soak time.~~ Resolved: the cutoff (end of Aug 2026) applies to new submissions only, so the live 7.1.1 build keeps serving and release 2 can soak freely. Only constraint: get release 1 submitted before the cutoff. |
| `[-]`  | Billing | **Accepted:** users who never install release 1 (dormant, then updating straight to a post-cutoff build) lose their badge. Unavoidable without a backend.                                                                                                      |
| `[-]`  | Billing | **Accepted:** supporter record is local only (per install), so reinstall / new device / cleared data loses the badge. Fixing this needs a backend + user IDs — rejected as too much scope.                                                                     |
| `[?]`  | Billing | Legacy backfill can only seed `count = 1`; Play's history API never returned a quantity. Pre-migration repeat buyers show `1x` until they buy again.                                                                                                           |
| `[-]`  | Billing | **Accepted:** the migration is one-shot, so a user who signs into a different Play account after it ran never gets that account's history backfilled. Edge case, and the API is gone in release 2 anyway.                                                      |
| `[-]`  | Billing | **Accepted:** `RECORDED_PURCHASE_TOKENS` grows by one ~60-char token per purchase and is never pruned. Bounded in practice by how often a person buys.                                                                                                        |

---

