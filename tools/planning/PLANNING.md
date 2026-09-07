# HuKi-Android — Plan Board

## Legend

| Status | Meaning                     |
|--------|-----------------------------|
| `[ ]`  | Not started                 |
| `[R]`  | Required for Next Release   |
| `[~]`  | In progress                 |
| `[x]`  | Done                        |
| `[-]`  | Cancelled / deprioritized   |
| `[?]`  | Questionable / spike needed |

---

## Backlog

### General / tech tasks

| Status | Feature                           |
|--------|-----------------------------------|
| `[R]`  | Add smart labels to Github README |
| `[R]`  | Revamp support screen             |

### Bugs

| Status | Scope | Bug                                                                                                                                   |
|--------|-------|---------------------------------------------------------------------------------------------------------------------------------------|
| `[ ]`  | GPX   | BUG: GPX roundtrip distance to my location not displayed                                                                              |

### FEATURE: Map

| Status | Scope | Task                                                      |
|--------|-------|-----------------------------------------------------------|
| `[ ]`  | Map   | Status message on hike mode changes                       |
| `[ ]`  | Map   | Offline detection + status message at the top             |
| `[x]`  | Home  | On My Location long click -> trigger FollowingLiveCompass |

### FEATURE: RoutePlanner

| Status | Scope        | Task                                                                                                                                                |
|--------|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `[R]`  | RoutePlanner | Add a dedicated error message if Graphhopper daily limit is reached. "We've reached the route planner service daily limit. Please try it tomorrow." |
| `[ ]`  | RoutePlanner | Update Route Planner settings icon for visibility                                                                                                   |

### FEATURE: HikingRoutes

| Status | Scope        | Task                                   |
|--------|--------------|----------------------------------------|
| `[ ]`  | HikingRoutes | Convert Hiking Route to GPX + altitude |

### FEATURE: Favourites / Flags

| Status | Scope      | Task                                                  |
|--------|------------|-------------------------------------------------------|
| `[ ]`  | Favourites | Feature: Waypoints only GPX creation in route planner |

### FEATURE: Billing / Supporters

Two-release plan to reach Billing 9.x without existing one-time supporters losing their badge.

**Why two releases:** Billing 8.x removes `queryPurchaseHistoryAsync` entirely, and there is no
Play Developer API endpoint to look up a user's lifetime purchases (only per-purchase-token
lookups). One-time products are consumed on purchase so they can be re-bought, so after the
upgrade Play can no longer tell us who supported before. Release 1 (still on 7.1.1) read the
legacy history one last time and backfilled it into local DataStore; release 2 does the upgrade
and reads only that local record.

#### Release 1 — bridge (Billing 7.1.1 + migration) — SHIPPED

Prod `v1.2.1` on 2026-07-27 (`e887ad5`). GA4 confirms the backfill ran:
`billing_legacy_purchase_backfilled` fired for **83 distinct users** (89 events), tailing off week
over week (17 / 25 / 11 / 18 / 14). `ProductsViewModel` is created by `HomeActivity` at startup,
not only by the Support screen, so the migration ran on plain app launch.

#### Release 2 — upgrade (Billing 9.1.0)

Target is **9.1.0**, not the originally planned 8.3.0: the API surface we use is identical,
`minSdk 23` / `targetSdk 35` are already satisfied (we are on 26 / 37), and Billing 9 is not
deprecated until **Aug 31, 2028** — a full extra year over Billing 8 (Aug 31, 2027).

**`billing-ktx` restored:** it was dropped during release 2 because its Kotlin metadata requires
Kotlin 2.3 for 9.x (and 2.2 for 8.1+) while the project compiled with 2.1.20 — only `billing-ktx`
8.0.0 would have been consumable — so the plain Java `billing` artifact was used with hand-written
suspend wrappers. With Kotlin at 2.4.10 the project is back on `billing-ktx` 9.1.0 and
`billing/BillingClientExtensions.kt` is deleted; the official
`queryProductDetails` / `queryPurchasesAsync` / `acknowledgePurchase` / `consumePurchase`
extensions are drop-in equivalents, except that `ProductDetailsResult.productDetailsList` is
nullable there.

| Status | Scope   | Task                                                                                                                          |
|--------|---------|-------------------------------------------------------------------------------------------------------------------------------|
| `[ ]`  | Billing | Re-verify badge + re-purchase flow on device on 9.1.0 (needs `applicationIdSuffix` commented out — restore before committing) |
| `[ ]`  | Billing | Review/trim the temporary `Timber.d("Billing: ...")` logs in `ProductsViewModel`                                              |
| `[R]`  | Billing | Ship release 2 to production — **blocks every other release** until it is out                                                 |

---

