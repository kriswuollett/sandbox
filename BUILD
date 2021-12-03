load("@io_bazel_rules_docker//container:container.bzl", "container_layer")
load("@rules_pkg//:pkg.bzl", "pkg_tar")

container_layer(
    name = "layer_with_structure",
    data_path = "/",
    files = glob(["doc/**/*"]),
)

container_layer(
    name = "layer_without_structure",
    files = glob(["doc/**/*"]),
)

pkg_tar(
    name = "tar_with_structure",
    srcs = glob(["doc/**/*"]),
    strip_prefix = ".",
)

pkg_tar(
    name = "tar_without_structure",
    srcs = glob(["doc/**/*"]),
)
