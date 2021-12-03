#!/usr/bin/env bash

set -eu
set -o pipefail

bazel build :layer_with_stamp_vars && tar -O -xf bazel-bin/layer_with_stamp_vars-layer.tar env.txt | grep BUILD