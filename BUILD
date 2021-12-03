load("@io_bazel_rules_docker//docker/util:run.bzl", "container_run_and_commit_layer")

container_run_and_commit_layer(
    name = "layer_with_stamp_vars",
    env = {
        "BUILD_SCM_REVISION": "{BUILD_SCM_REVISION}",
        "BUILD_USER": "{BUILD_USER}",
    },    
    commands = [
        "env > /env.txt"
    ],
    image = "@alpine_3_15_0//image",
)
