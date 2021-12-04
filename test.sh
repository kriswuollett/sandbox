#!/usr/bin/env bash

set -eu
set -o pipefail



echo "################################################################################"
echo "# Stamp variables are present in cc_binary"
echo "################################################################################"
echo

bazel run //:hello_world

echo
echo "################################################################################"
echo "# Stamp variables are not expanded in container_run_and_commit_layer"
echo "################################################################################"
echo

bazel build :layer_with_stamp_vars && tar -O -xf bazel-bin/layer_with_stamp_vars-layer.tar env.txt | grep BUILD
