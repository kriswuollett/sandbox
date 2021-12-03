load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

###############################################################################
# SKYLIB
###############################################################################

http_archive(
    name = "bazel_skylib",
    urls = [
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.1.1/bazel-skylib-1.1.1.tar.gz",
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.1.1/bazel-skylib-1.1.1.tar.gz",
    ],
    sha256 = "c6966ec828da198c5d9adbaa94c05e3a1c7f21bd012a0b29ba8ddbccb2c93b0d",
)
load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")
bazel_skylib_workspace()

###############################################################################
# DOCKER
###############################################################################

http_archive(
    name = "io_bazel_rules_docker",
    sha256 = "c27ab432594e793eb864604ec0e4cfd708285218da663b805eefdd479378da93",
    strip_prefix = "rules_docker-2b35b2dd56f0be6cc6b8df957332a31435f6b3ce",
    urls = ["https://github.com/bazelbuild/rules_docker/archive/2b35b2dd56f0be6cc6b8df957332a31435f6b3ce.tar.gz"],
)

load(
    "@io_bazel_rules_docker//repositories:repositories.bzl",
    container_repositories = "repositories",
)

container_repositories()

load("@io_bazel_rules_docker//repositories:deps.bzl", container_deps = "deps")

container_deps()


load(
    "@io_bazel_rules_docker//container:container.bzl",
    "container_pull",
)

container_pull(
    name = "alpine_3_15_0",
    digest = "sha256:e7d88de73db3d3fd9b2d63aa7f447a10fd0220b7cbf39803c803f2af9ba256b3",
    registry = "index.docker.io",
    repository = "library/alpine",
    tag = "3.15.0",
)
