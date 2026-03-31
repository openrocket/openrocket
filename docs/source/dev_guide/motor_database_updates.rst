Motor Database Updates (Security)
=================================

OpenRocket can optionally check for a newer motor database at startup and install it into the user's motor library
directory. The update flow is implemented in:

- `core/src/main/java/info/openrocket/core/database/MotorDatabaseRemoteUpdater.java`
- `swing/src/main/java/info/openrocket/swing/startup/MotorDatabaseUpdateChecker.java`

The motor database contents and build pipeline live in the separate repository:

- https://github.com/openrocket/motor-database

For the SQLite schema, see :doc:`motor_database_schema`.

Remote endpoints
----------------

By default, the updater fetches:

- Metadata: `https://openrocket.info/motor-database/metadata.json`
- Database (compressed): `metadata.json` field `download_url` (fallback: `https://openrocket.info/motor-database/motors.db.gz`)

The metadata must include `database_version`, `sha256_gz`, and `sig`.

Metadata fields
---------------

`metadata.json` is expected to contain (at minimum):

- `schema_version`: schema version of `motors.db`
- `database_version`: monotonic version identifier (typically a timestamp)
- `download_url`: HTTPS URL for `motors.db.gz`
- `sha256_gz`: lowercase hex SHA-256 of `motors.db.gz`
- `sig`: base64 Ed25519 signature over the canonical message

Optional fields that may be present:

- `key_id`: identifier for key rotation
- `generated_at`, `motor_count`, `curve_count`, ...

Update flow
-----------

1. Download remote `metadata.json`, parse it, and verify its Ed25519 signature.
2. Compare `database_version` with the local `metadata.json` in `SystemInfo.getOpenRocketMotorLibraryDirectory()`.
3. If remote is newer, prompt the user (Yes/No) to install.
4. If accepted, download `motors.db.gz` from `download_url`, verify SHA-256, decompress to `motors.db`, validate it,
   then atomically replace the local DB and write the remote metadata next to it.

Security properties
-------------------

The updater takes several precautions when downloading and installing data:

- HTTPS-only and redirect constraints

  - Downloads use HTTPS.
  - Redirects are handled manually and refused if they switch to non-HTTPS or to a host outside the updater allowlist
    (`openrocket.info`, `openrocket.github.io`).

- Offline authenticity (Ed25519 signature)

  - `metadata.json` includes a base64 Ed25519 signature (`sig`) that is verified using a public key embedded in
    OpenRocket. This prevents attackers from substituting a malicious metadata+database pair even if the server is
    compromised, as long as the signing key remains secure.

- Integrity verification (SHA-256)

  - The metadata includes `sha256_gz` (64 hex chars), which must match the SHA-256 of the downloaded `motors.db.gz`.

- Schema validation before install

  - The downloaded database is validated via `ThrustCurveMotorSQLiteDatabase.validateDatabase()` before replacing the
    local file. This prevents installing a non-SQLite file or a database missing required tables/columns.

- Resource limits

  - Hard byte limits are enforced for:

    - remote metadata size
    - downloaded compressed size
    - decompressed database size

  - This reduces risk from oversized downloads and decompression bombs.

Signature format
----------------

The signer produces an Ed25519 signature over the following canonical message (UTF-8):

`openrocket-motordb-v1\n{database_version}\n{sha256_gz}\n`

Where:

- `database_version` is the integer from `metadata.json`
- `sha256_gz` is the lowercase hex SHA-256 of `motors.db.gz`

The signature is stored as base64 in `metadata.json` under `sig`.

Limitations and threat model
----------------------------

- Rollback / freeze attacks

  - An attacker who can intercept traffic can prevent updates by serving older (but correctly signed) metadata.
  - OpenRocket never installs a database with a lower `database_version`, but it also cannot force an update without
    reaching the authentic endpoint.

- Parsing vulnerabilities

  - The update mechanism installs data files only, and does not execute code from the downloaded artifacts.
  - However, a malicious SQLite file could still attempt to exploit vulnerabilities in the SQLite/JDBC stack. Keeping
    dependencies up to date and adding signature verification are the strongest mitigations.

Testing
-------

- `core/src/test/java/info/openrocket/core/database/MotorDatabaseRemoteUpdaterTest.java` verifies SHA handling (compressed
  vs decompressed) and that mismatches are rejected.
- `core/src/test/java/info/openrocket/core/database/motor/InitialMotorsDatabaseFormatTest.java` ensures the bundled
  `initial_motors.db` can be validated and loaded.
