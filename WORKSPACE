workspace(
    name = "kw",
    managed_directories = {
        "@npm": ["node_modules"],
    },
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

###############################################################################
# PACKAGING
###############################################################################

http_archive(
    name = "rules_pkg",
    sha256 = "a89e203d3cf264e564fcb96b6e06dd70bc0557356eb48400ce4b5d97c2c3720d",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/0.5.1/rules_pkg-0.5.1.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.5.1/rules_pkg-0.5.1.tar.gz",
    ],
)

###############################################################################
# NODEJS
###############################################################################


http_archive(
    name = "build_bazel_rules_nodejs",
    sha256 = "cfc289523cf1594598215901154a6c2515e8bf3671fd708264a6f6aefe02bf39",
    urls = ["https://github.com/bazelbuild/rules_nodejs/releases/download/4.4.6/rules_nodejs-4.4.6.tar.gz"],
)

load("@build_bazel_rules_nodejs//:index.bzl", "node_repositories", "yarn_install")

node_repositories(
    node_version = "16.13.1",
    node_repositories = {
        "16.13.1-darwin_amd64": ("node-v16.13.1-darwin-x64.tar.gz", "node-v16.13.1-darwin-x64", "00b7a8426e076e9bf9d12ba2d571312e833fe962c70afafd10ad3682fdeeaa5e"),
        "16.13.1-linux_amd64": ("node-v16.13.1-linux-x64.tar.xz", "node-v16.13.1-linux-x64", "686d2c7b7698097e67bcd68edc3d6b5d28d81f62436c7cf9e7779d134ec262a9"),
        "16.13.1-windows_amd64": ("node-v16.13.1-win-x64.zip", "node-v16.13.1-win-x64", "70c46e6451798be9d052b700ce5dadccb75cf917f6bf0d6ed54344c856830cfb"),
    },
    yarn_version = "1.22.11",
yarn_repositories = {
        "1.22.11": ("yarn-v1.22.11.tar.gz", "yarn-v1.22.11", "09bea8f4ec41e9079fa03093d3b2db7ac5c5331852236d63815f8df42b3ba88d"),
    },    
)

# The set of node_modules needed to build kriswuollett.
yarn_install(
    name = "npm",
    package_json = "//:package.json",
    yarn_lock = "//:yarn.lock",
)
