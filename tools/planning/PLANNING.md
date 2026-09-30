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

### FEATURE: OKT

| Status | Scope | Task                                                                                                                                                                                                                                                  |
|--------|-------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[x]`  | OKT   | Add stamp locations to the selected OKT section. Its a horizontally scrollable list with stamp cards. Follow the design. Hardcode the distances first.                                                                                                |
| `[x]`  | OKT   | Don't add stamp locations if the selected route is the whole OKT, only is a sub-section is selected.                                                                                                                                                  |
| `[x]`  | OKT   | On the stamp card click, move the camera cener to the stamp location, and open its info window. Do not zoom in, keep the previous zoom.                                                                                                               |
| `[x]`  | OKT   | If a stamp card is clicked, the stamp markers should be in front of the START,END icons to make them visible.                                                                                                                                         |
| `[x]`  | OKT   | Update the "..." actions icon design to match the design. (light blue circle outlined)                                                                                                                                                                |
| `[x]`  | OKT   | When one of OKT feature opens. Hide the top floating actions from the map, as we do in HIKE_MODE state: keep only settings fab and layers fab, other actions in the top is hidden. (e.g. searchbar, support, discover, etc)                           |
| `[x]`  | OKT   | Use a bubble indicator in the bottom of info windows                                                                                                                                                                                                  |
| `[x]`  | OKT   | Calculate the stamp locations distances relative to each other, along the section route (not as the crow files).                                                                                                                                      |
| `[x]`  | OKT   | Add a reverse action to stamp locations                                                                                                                                                                                                               |
| `[x]`  | OKT   | Add a new row to the info window: Distance from my location which is calculated by my actual location along the route (not as the crow flies). Also display the estimated time along the route. Do not display it if you are far away from the route. |
| `[x]`  | OKT   | Create a Start section button in actions which sets STARTED state to the OKT section.                                                                                                                                                                 |
| `[x]`  | OKT   | When STARTED state: Sheet becomes hidden. OKT FAB is shown with the selected OKT tag.                                                                                                                                                                 
| `[x]`  | OKT   | Increase the base size of the OKT sheet to have more space to browse. Adjust the OKT map offset if necessary.                                                                                                                                         |
| `[x]`  | OKT   | The selected OKT route should be restored on app kill, so if somebody goes away to e.g. to camera app, OS kills the app and restores it, it should be opened again.                                                                                   |

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

### FEATURE: HikingRoutes

| Status | Scope        | Task                                   |
|--------|--------------|----------------------------------------|
| `[ ]`  | HikingRoutes | Convert Hiking Route to GPX + altitude |

### FEATURE: Favourites / Flags

| Status | Scope      | Task                                                  |
|--------|------------|-------------------------------------------------------|
| `[ ]`  | Favourites | Feature: Waypoints only GPX creation in route planner |

---

