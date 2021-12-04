# rules_docker_dot_dir

Run `./test.sh` to see dot directory issue with `container_image` from tar files.

Given a tar file with a `doc2` directory and another with a `.doc1` directory.

```
$ tar -tvf bazel-bin/doc1.tar 
drwxr-xr-x kris/kris         0 2021-12-04 16:30 .doc1/
-rw-r--r-- kris/kris         0 2021-12-04 16:30 .doc1/README.md
$ tar -tvf bazel-bin/doc2.tar 
drwxr-xr-x kris/kris         0 2021-12-04 16:30 doc2/
-rw-r--r-- kris/kris         0 2021-12-04 16:30 doc2/README.md
```

Expected result when adding them to `container` image with the `tars` attribute:

```
/a
└── b
    ├── .doc1
        └── README.md
    └── doc2
        └── README.md
```

Actual:

```
/a
├── b
│   └── doc2
│       └── README.md
└── bdoc1
    └── README.md
```
