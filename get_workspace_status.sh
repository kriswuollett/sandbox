#!/bin/bash

# https://raw.githubusercontent.com/bazelbuild/bazel/09c621e4cf5b968f4c6cdf905ab142d5961f9ddc/tools/buildstamp/get_workspace_status

BUILD_SCM_REVISION=$(git rev-parse HEAD)
if [[ $? != 0 ]];
then
    exit 1
fi
echo "BUILD_SCM_REVISION ${BUILD_SCM_REVISION}"
