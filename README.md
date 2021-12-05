# rules_docker_no_stamping

Example code for stamp variable does not expand in `container_run_and_commit_layer` rule [bazelbuild/rules_docker#1973](https://github.com/bazelbuild/rules_docker/issues/1973).

## Example

To test run `./test.sh`. Currently the output looks like:

```
BUILD_USER={BUILD_USER}
BUILD_SCM_REVISION={BUILD_SCM_REVISION}
```

But the values should look like those found in `bazel-out/stable-status.txt` and `bazel-out/volatile-status.txt`.
