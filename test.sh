#!/usr/bin/env bash

set -eu
set -o pipefail

echo "################################################################################"
echo "pkg_tar: Use strip_prefix to keep structure"
echo "################################################################################"
echo
bazel build :tar_with_structure && tar -tvf bazel-bin/tar_with_structure.tar

echo
echo "################################################################################"
echo "pkg_tar: Without strip_prefix all files mixed in root directory"
echo "################################################################################"
echo
bazel build :tar_without_structure && tar -tvf bazel-bin/tar_without_structure.tar

echo
echo "################################################################################"
echo "container_layer: Use data_path to keep structure"
echo "################################################################################"
bazel build :layer_with_structure && tar -tvf bazel-bin/layer_with_structure-layer.tar
echo

echo "################################################################################"
echo "container_layer: Without data_path all files mixed in root directory"
echo "################################################################################"
bazel build :layer_without_structure && tar -tvf bazel-bin/layer_without_structure-layer.tar
