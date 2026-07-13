# Application Module Layout

Each product lives under `applications/<product>` and contains its own:

- `<product>-common`
- `<product>-persist`
- `<product>-module`
- `<product>-web`
- `<product>-db`

These product modules depend on the shared `corex-*` modules and are intended for
product-specific extensions only.

Database installation is handled centrally by `scripts/install-db.sh`, with each
product DB module contributing its own `install-order.txt` manifest.
