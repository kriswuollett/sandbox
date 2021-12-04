cat <<EOF
################################################################################
# doc1.tar
################################################################################

EOF
&>/dev/null bazel build :doc1 && tar -tvf bazel-bin/doc1.tar

cat <<EOF

################################################################################
# doc2.tar
################################################################################

EOF
&>/dev/null bazel build :doc2 && tar -tvf bazel-bin/doc2.tar

cat <<EOF

################################################################################
# Expected output
################################################################################

/a
└── b
    ├── .doc1
        └── README.md
    └── doc2
        └── README.md

3 directories 2 files
EOF

cat <<EOF

################################################################################
# Actual output
################################################################################

EOF

&>/dev/null bazel run :image -- --norun  && docker run -it  bazel:image tree -a /a
