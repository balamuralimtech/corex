# Database Installer

Use the installer from the repository root to apply the shared CoreX schema or an individual application schema.

For a desktop installer with UI prompts for database name, username, password, target selection, progress, and execution logs, build and run [corex-installer/README.md](/Users/balamurali/Documents/GitHub/corex/corex-installer/README.md).

## Targets

- `corex`: shared platform tables only
- `shipx`: shared CoreX tables and ShipX tables
- `carex`: shared CoreX tables and CareX tables
- `payrollx`: shared CoreX tables and PayrollX tables
- `all`: shared CoreX tables and every application schema

## Commands

### macOS / Linux / Git Bash

```bash
sh scripts/install-db.sh --target corex --database corex_dev --user root
sh scripts/install-db.sh --target shipx --database shipx_dev --user root
sh scripts/install-db.sh --target carex --database carex_dev --user root --app-only
sh scripts/install-db.sh --target all --database corex_suite --user root
```

### Windows PowerShell

```powershell
.\scripts\install-db.ps1 -Target corex -Database corex_dev -User root
.\scripts\install-db.ps1 -Target shipx -Database shipx_dev -User root
.\scripts\install-db.ps1 -Target carex -Database carex_dev -User root -AppOnly
.\scripts\install-db.ps1 -Target all -Database corex_suite -User root
```

Pass `--password` in shell or `-Password` in PowerShell only when you need explicit credentials. Otherwise `mysql` uses its normal prompt or local configuration.

## OS Notes

- macOS and Linux: use `sh scripts/install-db.sh ...`
- Windows with PowerShell: use `.\scripts\install-db.ps1 ...`
- Windows with Git Bash: the shell script also works
- Windows `cmd.exe` is not a target directly; use PowerShell instead

The install order is controlled by these manifest files:

- `corex-db/install-order.txt`
- `applications/shipx/shipx-db/install-order.txt`
- `applications/carex/carex-db/install-order.txt`
- `applications/payrollx/payrollx-db/install-order.txt`
