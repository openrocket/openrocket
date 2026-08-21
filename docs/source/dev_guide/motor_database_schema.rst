Motor Database Schema (SQLite)
==============================

OpenRocket stores its built-in motor database as a SQLite file `motors.db` in the user motor library directory
(`SystemInfo.getOpenRocketMotorLibraryDirectory()`).

The canonical source (generator + build pipeline) for this database is the separate repository:

- https://github.com/openrocket/motor-database

This page documents the *expected* schema OpenRocket can validate and read. The authoritative validation logic lives in
`core/src/main/java/info/openrocket/core/database/motor/ThrustCurveMotorSQLiteDatabase.java`.

Overview
--------

- The schema is **normalized** to allow multiple thrust curves per motor (e.g., certification vs manufacturer vs user).
- Schema versioning is stored inside the DB in `meta` (`schema_version`), while distribution metadata (like
  `database_version` and signatures) lives next to the DB in `metadata.json` (see :doc:`motor_database_updates`).

Tables
------

`meta`
^^^^^^

Key/value metadata about the database.

- `key` (TEXT, PK)
- `value` (TEXT, NOT NULL)

OpenRocket requires at least `schema_version` to be present.

`manufacturers`
^^^^^^^^^^^^^^^

- `id` (INTEGER, PK, autoincrement)
- `name` (TEXT, NOT NULL, unique)
- `abbrev` (TEXT)

`motors`
^^^^^^^^

Motor specifications and identifiers.

Required columns (OpenRocket validates these for schema v2+):

- `id` (INTEGER, PK, autoincrement)
- `manufacturer_id` (INTEGER, FK → `manufacturers.id`)
- `tc_motor_id` (TEXT)
- `designation` (TEXT, NOT NULL)
- `common_name` (TEXT)
- `impulse_class` (TEXT)
- `diameter` (REAL)
- `length` (REAL)
- `total_impulse` (REAL)
- `avg_thrust` (REAL)
- `max_thrust` (REAL)
- `burn_time` (REAL)
- `propellant_weight` (REAL)
- `total_weight` (REAL)
- `type` (TEXT)
- `delays` (TEXT)
- `case_info` (TEXT)
- `prop_info` (TEXT)
- `sparky` (INTEGER)
- `info_url` (TEXT)
- `data_files` (INTEGER)
- `updated_on` (TEXT)

Optional columns (OpenRocket will read these when present):

- `description` (TEXT)
- `source` (TEXT)

`thrust_curves`
^^^^^^^^^^^^^^^

Each motor can have multiple curves from different sources.

- `id` (INTEGER, PK, autoincrement)
- `motor_id` (INTEGER, FK → `motors.id`, cascade delete)
- `tc_simfile_id` (TEXT)
- `source` (TEXT)
- `format` (TEXT)
- `license` (TEXT)
- `info_url` (TEXT)
- `data_url` (TEXT)
- `total_impulse` (REAL)
- `avg_thrust` (REAL)
- `max_thrust` (REAL)
- `burn_time` (REAL)

`thrust_data`
^^^^^^^^^^^^^

Time/thrust data points for each curve.

- `id` (INTEGER, PK, autoincrement)
- `curve_id` (INTEGER, FK → `thrust_curves.id`, cascade delete)
- `time_seconds` (REAL, NOT NULL)
- `force_newtons` (REAL, NOT NULL)

Indices
-------

OpenRocket expects the following indices to exist (or be creatable) for performance:

- `idx_motor_mfr` on `motors(manufacturer_id)`
- `idx_motor_diameter` on `motors(diameter)`
- `idx_motor_impulse` on `motors(total_impulse)`
- `idx_motor_impulse_class` on `motors(impulse_class)`
- `idx_motor_tc_id` on `motors(tc_motor_id)`
- `idx_curve_motor` on `thrust_curves(motor_id)`
- `idx_curve_simfile` on `thrust_curves(tc_simfile_id)`
- `idx_thrust_curve` on `thrust_data(curve_id)`

