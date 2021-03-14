#! /bin/bash

ftp -n <<EOF
open ftp.dlptest.com
user dlpuser rNrKYTX9g7z3RgJRmxWuGHbeu
put /Users/runner/ToxAndroidRefImpl/jni-c-toxcore/libjni-c-toxcore.jnilib
EOF

