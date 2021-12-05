#!/usr/bin/env bash

&>/dev/null bazel build :node-modules

cat <<EOF
################################################################################
# node_modules/.bin/next is in the managed node_modules directory
################################################################################

EOF
echo "\$ find -L node_modules -path '*/.bin/next'"
find -L node_modules -path '*/.bin/next'

cat <<EOF

################################################################################
# node_modules/.bin/next is in the Bazel sandbox
################################################################################

EOF
echo "\$ ls -la bazel-sandbox/external/npm/node_modules/.bin/next"
find -L bazel-sandbox/external/npm/node_modules -path '*/.bin/next'

cat <<EOF

################################################################################
# node_modules/.bin/next IS NOT in the pkg_tar archive
################################################################################

EOF

echo "\$ tar -tvf bazel-bin/node-modules.tar | grep '\.bin/next'"
tar -tvf bazel-bin/node-modules.tar | grep '\.bin/next'

cat <<EOF

################################################################################
# Confirming not in :node_modules ...
################################################################################

EOF

&>/dev/null bazel build :npm_files

echo "\$ grep '\.bin/next' bazel-bin/app-npm_files.txt"
grep '\.bin/next' bazel-bin/app-npm_files.txt
