# rules_docker_no_stamping

To test run `./test.sh`. Currently the output looks like:

```
BUILD_USER={BUILD_USER}
BUILD_SCM_REVISION={BUILD_SCM_REVISION}
```

But the values should look like those found in `bazel-out/stable-status.txt` and `bazel-out/volatile-status.txt`.
