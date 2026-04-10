# Core Messages Translation Workflow

Use this workflow when you want reviewed native translations for the full `coreMessages` bundle family.

## Export

Run from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\export-core-messages.ps1
```

This produces:

```text
coreMessages.translation.csv
```

CSV format:

```text
key,base,as,bn,de,es,fr,gu,hi,id,it,ja,kn,ko,ml,mr,ms,nl,or,pa,pl,pt,ru,si,ta,te,th,tr,uk,ur,vi,zh
```

Rules for translation:

- Do not change the `key` column.
- Keep placeholders, punctuation, and product-specific terms intact.
- Translate only the text values.
- Review each locale with a native-language source before import.

## Import

After translation review, import the CSV back into the bundle files:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\import-core-messages.ps1 -CsvFile .\coreMessages.translation.csv
```

This updates:

- `corex-web/src/main/resources/coreMessages.properties`
- `corex-web/src/main/resources/coreMessages_*.properties`

Behavior:

- empty locale cells keep the existing locale value if present
- if no existing locale value exists, the importer falls back to the base value

## Verify

Compile after import:

```powershell
mvn -pl corex-web -am -DskipTests compile
```
