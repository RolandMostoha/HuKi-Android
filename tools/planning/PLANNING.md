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

| Status | Feature                              |
|--------|--------------------------------------|
| `[ ]`  | Remove Androd UI tests from pipeline |

### Bugs

| Status | Scope | Bug                                                      |
|--------|-------|----------------------------------------------------------|
| `[ ]`  | GPX   | BUG: GPX roundtrip distance to my location not displayed |

### FEATURE: Map

| Status | Scope | Task                                          |
|--------|-------|-----------------------------------------------|
| `[ ]`  | Map   | Status message on hike mode changes           |
| `[ ]`  | Map   | Offline detection + status message at the top |

### FEATURE: MyLocation

| Status | Scope      | Task                          |
|--------|------------|-------------------------------|
| `[ ]`  | MyLocation | Send my location via deeplink |

### FEATURE: RoutePlanner

| Status | Scope        | Task                                              |
|--------|--------------|---------------------------------------------------|
| `[ ]`  | RoutePlanner | Update Route Planner settings icon for visibility |

### FEATURE: RoutePlanner: HuKi-Routing follow-up

After the "A Kéktúra napja" weekend, once HuKi-KMP has the fallback and most Android users updated,
decide by the weekend analytics:

- **A: HuKi-first in Hungary**, GraphHopper fallback (if HuKi-Routing proved reliable)
- **B: Drop the reserve**: GraphHopper → on 429: HuKi in Hungary

| Status | Scope        | Task                                                                                                                                                            |
|--------|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[ ]`  | Analytics    | Review weekend events: `routing_huki_served`, `routing_huki_failed`, `routing_gh_limit_hit`, `route_planner_limit_reached`                                      |
| `[ ]`  | HuKi-KMP     | Port the HuKi-Routing fallback to HuKi-KMP (iOS)                                                                                                                |
| `[ ]`  | RoutePlanner | Decide A or B                                                                                                                                                   |
| `[ ]`  | RoutePlanner | Remove `GRAPHHOPPER_RESERVE_CREDITS`, `updateReserveReached` and the reserve branch in `RoutePlannerRepository`                                                 |
| `[ ]`  | RoutePlanner | `GraphhopperLimitRepository`: drop `reserveReachedUntil` + `GRAPHHOPPER_RESERVE_REACHED_UNTIL` key, replace `GraphhopperLimitState` with `isBlocked(nowMillis)` |
| `[ ]`  | Analytics    | Remove `routing_gh_reserve_reached`, `routing_gh_after_huki_failure` (B) or rename the latter to GraphHopper-after-HuKi fallback (A)                            |
| `[ ]`  | RoutePlanner | A only: HuKi-first in Hungary, GraphHopper fallback; consider "Powered by" text for HuKi-served routes                                                          |
| `[ ]`  | RoutePlanner | Update unit tests, `GraphhopperLimitRepositoryTest`, AGENTS.md "Route Planner backends"                                                                         |

### FEATURE: HikingRoutes

| Status | Scope        | Task                                   |
|--------|--------------|----------------------------------------|
| `[ ]`  | HikingRoutes | Convert Hiking Route to GPX + altitude |

### FEATURE: Favourites / Flags

| Status | Scope      | Task                                                  |
|--------|------------|-------------------------------------------------------|
| `[ ]`  | Favourites | Feature: Waypoints only GPX creation in route planner |

---

