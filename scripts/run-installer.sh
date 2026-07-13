#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "${SCRIPT_DIR}/.." && pwd)
INSTALLER_JAR="${REPO_ROOT}/corex-installer/target/corex-installer-jar-with-dependencies.jar"

if [ ! -f "${INSTALLER_JAR}" ]; then
  echo "Installer jar not found. Building corex-installer first..."
  mvn -pl corex-installer -am package
fi

exec java -jar "${INSTALLER_JAR}"
