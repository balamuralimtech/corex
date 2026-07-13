# CoreX Standalone Installer

This module builds a standalone Java installer with a desktop UI for creating a MySQL database and applying the shared CoreX schema plus any selected application schema.

## What it asks for

- host
- port
- database name
- username
- password
- install target

## Build

```bash
mvn -pl corex-installer -am package
```

## Run

```bash
java -jar corex-installer/target/corex-installer-jar-with-dependencies.jar
```

Or use the launcher scripts from the repository root:

```bash
sh scripts/run-installer.sh
```

```powershell
.\scripts\run-installer.ps1
```

The packaged jar includes the SQL manifests and SQL files from:

- `corex-db`
- `applications/shipx/shipx-db`
- `applications/carex/carex-db`
- `applications/payrollx/payrollx-db`
