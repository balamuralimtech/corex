#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "${SCRIPT_DIR}/.." && pwd)

MYSQL_HOST="127.0.0.1"
MYSQL_PORT="3306"
MYSQL_USER=""
MYSQL_PASSWORD=""
MYSQL_DATABASE=""
TARGET=""
APP_ONLY=0

usage() {
  cat <<'EOF'
Usage:
  sh scripts/install-db.sh --target <corex|shipx|carex|payrollx|all> --database <name> --user <user> [options]

Options:
  --host <host>           MySQL host. Default: 127.0.0.1
  --port <port>           MySQL port. Default: 3306
  --user <user>           MySQL user
  --password <password>   MySQL password. If omitted, mysql will prompt when needed.
  --database <name>       Target database name
  --target <name>         Install shared CoreX schema, an app schema, or all app schemas
  --app-only              For app targets, skip shared CoreX schema
  --help                  Show this help

Examples:
  sh scripts/install-db.sh --target corex --database corex_dev --user root
  sh scripts/install-db.sh --target shipx --database shipx_dev --user root
  sh scripts/install-db.sh --target carex --database carex_dev --user root --app-only
  sh scripts/install-db.sh --target all --database corex_suite --user root
EOF
}

fail() {
  echo "Error: $*" >&2
  exit 1
}

require_value() {
  [ $# -ge 2 ] || fail "Missing value for $1"
  [ -n "$2" ] || fail "Missing value for $1"
}

trim_line() {
  printf '%s' "$1" | sed 's/^[[:space:]]*//; s/[[:space:]]*$//'
}

run_sql() {
  sql_path=$1

  if [ -n "${MYSQL_PASSWORD}" ]; then
    mysql --host="${MYSQL_HOST}" --port="${MYSQL_PORT}" --user="${MYSQL_USER}" --password="${MYSQL_PASSWORD}" --database="${MYSQL_DATABASE}" < "${sql_path}"
  else
    mysql --host="${MYSQL_HOST}" --port="${MYSQL_PORT}" --user="${MYSQL_USER}" --database="${MYSQL_DATABASE}" < "${sql_path}"
  fi
}

while [ $# -gt 0 ]; do
  case "$1" in
    --host)
      require_value "$1" "${2-}"
      MYSQL_HOST=$2
      shift 2
      ;;
    --port)
      require_value "$1" "${2-}"
      MYSQL_PORT=$2
      shift 2
      ;;
    --user)
      require_value "$1" "${2-}"
      MYSQL_USER=$2
      shift 2
      ;;
    --password)
      require_value "$1" "${2-}"
      MYSQL_PASSWORD=$2
      shift 2
      ;;
    --database)
      require_value "$1" "${2-}"
      MYSQL_DATABASE=$2
      shift 2
      ;;
    --target)
      require_value "$1" "${2-}"
      TARGET=$2
      shift 2
      ;;
    --app-only)
      APP_ONLY=1
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      fail "Unknown argument: $1"
      ;;
  esac
done

[ -n "${MYSQL_USER}" ] || fail "--user is required"
[ -n "${MYSQL_DATABASE}" ] || fail "--database is required"
[ -n "${TARGET}" ] || fail "--target is required"

if ! command -v mysql >/dev/null 2>&1; then
  fail "mysql client is required but was not found in PATH"
fi

MANIFEST_COREX="${REPO_ROOT}/corex-db/install-order.txt"
MANIFEST_SHIPX="${REPO_ROOT}/applications/shipx/shipx-db/install-order.txt"
MANIFEST_CAREX="${REPO_ROOT}/applications/carex/carex-db/install-order.txt"
MANIFEST_PAYROLLX="${REPO_ROOT}/applications/payrollx/payrollx-db/install-order.txt"

run_manifest() {
  manifest_path=$1
  base_dir=$2
  label=$3
  executed=0

  [ -f "${manifest_path}" ] || fail "Manifest not found: ${manifest_path}"

  echo "Installing ${label} schema from ${manifest_path}"

  while IFS= read -r raw_line || [ -n "${raw_line}" ]; do
    line=$(trim_line "${raw_line}")

    [ -n "${line}" ] || continue
    case "${line}" in
      \#*)
        continue
        ;;
    esac

    sql_path="${base_dir}/${line}"
    [ -f "${sql_path}" ] || fail "SQL file listed in ${manifest_path} does not exist: ${line}"

    echo "  -> ${line}"
    run_sql "${sql_path}"
    executed=1
  done < "${manifest_path}"

  if [ "${executed}" -eq 0 ]; then
    echo "  -> no SQL files listed; skipped"
  fi
}

install_corex() {
  run_manifest "${MANIFEST_COREX}" "${REPO_ROOT}/corex-db" "CoreX"
}

install_shipx() {
  run_manifest "${MANIFEST_SHIPX}" "${REPO_ROOT}/applications/shipx/shipx-db" "ShipX"
}

install_carex() {
  run_manifest "${MANIFEST_CAREX}" "${REPO_ROOT}/applications/carex/carex-db" "CareX"
}

install_payrollx() {
  run_manifest "${MANIFEST_PAYROLLX}" "${REPO_ROOT}/applications/payrollx/payrollx-db" "PayrollX"
}

case "${TARGET}" in
  corex)
    install_corex
    ;;
  shipx)
    [ "${APP_ONLY}" -eq 1 ] || install_corex
    install_shipx
    ;;
  carex)
    [ "${APP_ONLY}" -eq 1 ] || install_corex
    install_carex
    ;;
  payrollx)
    [ "${APP_ONLY}" -eq 1 ] || install_corex
    install_payrollx
    ;;
  all)
    install_corex
    install_shipx
    install_carex
    install_payrollx
    ;;
  *)
    fail "Unsupported target: ${TARGET}"
    ;;
esac

echo "Database installation complete for target: ${TARGET}"
