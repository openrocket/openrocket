# Recovery Warning System — Per-Stage Dual/Single Detection

## Overview

Recovery deployment speed warnings are evaluated **per stage**, independently, using the rocket's component configuration.

---

## Warning Paths

Each stage gets one of two warning paths when a recovery device deploys:

| Stage condition | Warning(s) fired |
|---|---|
| Has a drogue device (auto-detected) | `HighSpeedMainDeployment` + `LowSpeedMainDeployment` for the main chute |
| No drogue device detected for the deploying stage | `RecoveryHighSpeedDeployment` (current single deploy) |

---

## How Detection Works

At every recovery device deployment event, the engine:

1. Scans **active components in the deploying stage only** for a `RecoveryDevice` with `isDrogue() == true` and a non-`NEVER` deploy event → dual-deployment path
2. If none is found → single-deployment path

The scan is scoped to the deploying stage so a drogue in stage 1 of a multi-stage rocket does not affect stage 2's warning thresholds.

---

## Configuration

### Per-device: "Is Drogue" checkbox (`ParachuteConfig`, `StreamerConfig`)

This checkbox is read-only in component dialogs; drogue selection is managed from the stage Recovery tab.

### Per-stage Recovery tab (`AxialStageConfig`)

The stage Recovery tab provides:

- `Single Deployment` radio (clears drogue selection for the stage)
- `Dual Deployment` radio (enables drogue selection)
- `Drogue device` dropdown (selects which recovery device in the stage is the drogue)

If no recovery devices exist in the stage, `Dual Deployment` is disabled.

---

## Simulation Options Panel

The dual deployment tab shows two always-visible threshold fields (no radio buttons or conditional sections):

- **Main low-speed warning** — fires `LowSpeedMainDeployment` if main deploys below this speed in a dual-deployment stage
- **Main high-speed warning** — fires `HighSpeedMainDeployment` if main deploys above this speed in a dual-deployment stage

---

## Warning Thresholds

Configured in simulation options (defaults):

| Threshold | Default | Used for |
|---|---|---|
| `RecoverySpeedWarning` | 20 m/s | Single-deployment high-speed deploy |
| `RecoveryDrogueMainHighSpeedWarning` | 30.48 m/s (100 fps) | Main deploy too fast (dual) |
| `RecoveryDrogueMainLowSpeedWarning` | 15.24 m/s (50 fps) | Main deploy too slow (dual only) |
