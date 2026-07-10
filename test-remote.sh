#!/bin/bash
set -o pipefail
rsync -a --delete --exclude .git --exclude local.properties \
  "$HOME/memory-match/" tycho:builds/memory-match/ || { echo "RSYNC FAILED"; exit 2; }
ssh tycho 'cd ~/builds/memory-match && ~/gradle-8.9/bin/gradle test 2>&1'
