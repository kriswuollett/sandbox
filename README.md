# rules_pkg_dot_dir

Example code for figuring out why `.bin/next` was missing: [bazelbuild/rules_nodejs#3117](https://github.com/bazelbuild/rules_nodejs/issues/3117).

## Example

Run `./test.sh`. Example output

```
$ ./test.sh
################################################################################
# node_modules/.bin/next is in the managed node_modules directory
################################################################################

$ find -L node_modules -path '*/.bin/next'
node_modules/next-router-mock/node_modules/.bin/next
node_modules/.bin/next
node_modules/eslint-config-next/node_modules/.bin/next

################################################################################
# node_modules/.bin/next is in the Bazel sandbox
################################################################################

$ ls -la bazel-sandbox/external/npm/node_modules/.bin/next
bazel-sandbox/external/npm/node_modules/next-router-mock/node_modules/.bin/next
bazel-sandbox/external/npm/node_modules/.bin/next
bazel-sandbox/external/npm/node_modules/eslint-config-next/node_modules/.bin/next

################################################################################
# node_modules/.bin/next IS NOT in the pkg_tar archive
################################################################################

$ tar -tvf bazel-bin/node-modules.tar | grep '\.bin/next'
-r-xr-xr-x 0/0            4629 1999-12-31 19:00 ./node_modules/eslint-config-next/node_modules/.bin/next
-r-xr-xr-x 0/0            4629 1999-12-31 19:00 ./node_modules/next-router-mock/node_modules/.bin/next

################################################################################
# Confirming not in :node_modules ...
################################################################################

$ grep '\.bin/next' bazel-bin/app-npm_files.txt
external/npm/node_modules/eslint-config-next/node_modules/.bin/next
external/npm/node_modules/next-router-mock/node_modules/.bin/next
```
