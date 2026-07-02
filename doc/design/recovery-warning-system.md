# Recovery Warning System — Per-Stage Dual/Single/Drogueless Detection

## Overview

Recovery deployment speed warnings are evaluated **per stage**, independently, using the rocket's component configuration.

---

## Warning Paths

Each stage gets one of three warning paths when a recovery device deploys:

| Stage condition | Warning(s) fired |
|---|---|
| Has a drogue device (auto-detected) | `HighSpeedMainDeployment` + `LowSpeedMainDeployment` for the main chute |
| Marked as drogueless | `HighSpeedMainDeployment` only |
| Neither (no drogue, not drogueless) | `RecoveryHighSpeedDeployment` (current single deploy) |

**Drogueless** covers the case where a rocket freefalls or tumbles from apogee down to a lower deployment altitude, then deploys the main directly — no drogue chute is used to slow the descent. Because the rocket is in uncontrolled freefall until main deployment, a low-speed main warning doesn't apply (the main deploying too slowly is not a concern in this mode).

---

## How Detection Works

At every recovery device deployment event, the engine:

1. Checks if the **deploying stage** is flagged `isDrogueless()` → drogueless path
2. Otherwise, scans **active components in that stage only** for a `RecoveryDevice` with `isDrogue() == true` and a non-`NEVER` deploy event → dual-deployment path
3. If neither → single-deployment path

The scan is scoped to the deploying stage so a drogue in stage 1 of a multi-stage rocket does not affect stage 2's warning thresholds.

---

## Configuration

### Per-device: "Is Drogue" checkbox (`ParachuteConfig`, `StreamerConfig`)

Marks a recovery device as the drogue for its stage. Blocked if:
- The stage is already marked drogueless
- Another device in the same stage is already marked as a drogue

### Per-stage: "Drogueless main deployment" checkbox (`AxialStageConfig` → Recovery tab)

Marks a stage as using main-only deployment (no drogue). Blocked if:
- Any recovery device in the stage is marked as a drogue

The Recovery tab also shows a status line — either the name of the detected drogue device or "No drogue device found in this stage."

---

## Simulation Options Panel

The dual deployment tab shows two always-visible threshold fields (no radio buttons or conditional sections):

- **Main low-speed warning** — fires `LowSpeedMainDeployment` if main deploys below this speed in a dual-deployment stage
- **Main high-speed warning** — fires `HighSpeedMainDeployment` if main deploys above this speed in a dual-deployment or drogueless stage

---

## Warning Thresholds

Configured in simulation options (defaults):

| Threshold | Default | Used for |
|---|---|---|
| `RecoverySpeedWarning` | 20 m/s | Single-deployment high-speed deploy |
| `RecoveryDrogueMainHighSpeedWarning` | 30.48 m/s (100 fps) | Main deploy too fast (dual or drogueless) |
| `RecoveryDrogueMainLowSpeedWarning` | 15.24 m/s (50 fps) | Main deploy too slow (dual only) |
