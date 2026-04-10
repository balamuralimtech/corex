# Translation Packs

This workflow splits the main core translation CSV into one file per language for native review, then merges the reviewed files back.

## Create Per-Language Packs

From the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\split-core-messages-translation-packs.ps1
```

Output folder:

```text
translation-packs/coreMessages/<locale>/coreMessages_<locale>.csv
```

Each pack contains:

- `key`
- `base`
- `target`
- `review_status`
- `reviewer_notes`

Translators should update only `target`, and optionally `review_status` / `reviewer_notes`.

## Merge Reviewed Packs

After review is complete:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\merge-core-messages-translation-packs.ps1
```

This regenerates:

```text
coreMessages.translation.csv
```

## Import Back Into Bundles

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\import-core-messages.ps1 -CsvFile .\coreMessages.translation.csv
```

## Verify

```powershell
mvn -pl corex-web -am -DskipTests compile
```
