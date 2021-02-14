#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

_CTC_SRC_DIR_="/root/work/c-toxcore"
export _CTC_SRC_DIR_

export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/


export CF2=" -O3 -g"
export CF3=" "
export VV1=" " # VERBOSE=1 V=1 "


mkdir -p $_SRC_
mkdir -p $_INST_

export LD_LIBRARY_PATH=$_INST_/lib/
export PKG_CONFIG_PATH=$_INST_/lib/pkgconfig

cd /root/work/
git clone https://github.com/zoff99/c-toxcore "$_CTC_SRC_DIR_"/

cd "$_CTC_SRC_DIR_"/
pwd
ls -al

git checkout zoff99/zoxcore_local_fork

./autogen.sh
make clean
export CFLAGS_=" $CF2 -D_GNU_SOURCE -I$_INST_/include/ -O3 -g -fstack-protector-all "
export CFLAGS="$CFLAGS_"
# export CFLAGS=" $CFLAGS -Werror=div-by-zero -Werror=format=2 -Werror=implicit-function-declaration "
export LDFLAGS="-L$_INST_/lib"

./configure \
--prefix=$_INST_ \
--disable-soname-versions --disable-testing --disable-shared
make -j$(nproc) || exit 1
make install

export CFLAGS=" $CFLAGS_ -fPIC "
export CXXFLAGS=" $CFLAGS_ -fPIC "
export LDFLAGS=" $LDFLAGS_ -fPIC "
# timeout -k 242 240 make V=1 -j20 check || exit 0 # tests fail too often on CI -> don't error out on test failures



# -------------- now compile the JNI lib ----------------------

cd /root/work/
pwd
ls -al

cd src/
pwd
ls -al

echo "--------------"
ls -al $_INST_/lib/libtoxcore.a
echo "--------------"


add_config_flag() { CONFIG_FLAGS="$CONFIG_FLAGS $@";    }
add_c_flag()      { C_FLAGS="$C_FLAGS $@";              }
add_cxx_flag()    { CXX_FLAGS="$CXX_FLAGS $@";          }
add_ld_flag()     { LD_FLAGS="$LD_FLAGS $@";            }
add_flag()        { add_c_flag "$@"; add_cxx_flag "$@"; }

# Our own flags which we can insert in the correct place. We don't use CFLAGS
# and friends here (we unset them below), because they influence config tests
# such as ./configure and cmake tests. Our warning flags break those tests, so
# we can't add them globally here.
CONFIG_FLAGS=""
C_FLAGS=""
CXX_FLAGS=""
LD_FLAGS=""

unset CFLAGS
unset CXXFLAGS
unset CPPFLAGS
unset LDFLAGS

# Optimisation flags.
add_flag -O2 -march=native

# Warn on non-ISO C.
add_c_flag -pedantic
add_c_flag -std=c99

add_flag -g3
add_flag -ftrapv


# Add all warning flags we can.
add_flag -Wall
add_flag -Wextra
add_flag -Weverything

# Disable specific warning flags for both C and C++.

# TODO(iphydf): Clean these up. Probably all of these are actual bugs.
add_flag -Wno-cast-align
# Very verbose, not very useful. This warns about things like int -> uint
# conversions that change sign without a cast and narrowing conversions.
add_flag -Wno-conversion
# TODO(iphydf): Check enum values when received from the user, then assume
# correctness and remove this suppression.
add_flag -Wno-covered-switch-default
# Due to clang's tolower() macro being recursive
# https://github.com/TokTok/c-toxcore/pull/481
add_flag -Wno-disabled-macro-expansion
# We don't put __attribute__ on the public API.
add_flag -Wno-documentation-deprecated-sync
# Bootstrap daemon does this.
add_flag -Wno-format-nonliteral
# struct Foo foo = {0}; is a common idiom.
add_flag -Wno-missing-field-initializers
# Useful sometimes, but we accept padding in structs for clarity.
# Reordering fields to avoid padding will reduce readability.
add_flag -Wno-padded
# This warns on things like _XOPEN_SOURCE, which we currently need (we
# probably won't need these in the future).
add_flag -Wno-reserved-id-macro
# TODO(iphydf): Clean these up. They are likely not bugs, but still
# potential issues and probably confusing.
add_flag -Wno-sign-compare
# Our use of mutexes results in a false positive, see 1bbe446.
add_flag -Wno-thread-safety-analysis
# File transfer code has this.
add_flag -Wno-type-limits
# Callbacks often don't use all their parameters.
add_flag -Wno-unused-parameter
# libvpx uses __attribute__((unused)) for "potentially unused" static
# functions to avoid unused static function warnings.
add_flag -Wno-used-but-marked-unused
# We use variable length arrays a lot.
add_flag -Wno-vla

# Disable specific warning flags for C++.

# Downgrade to warning so we still see it.
# add_flag -Wno-error=documentation-unknown-command
add_flag -Wno-documentation-unknown-command

add_flag -Wno-error=unreachable-code
add_flag -Wno-error=unused-variable


# added by Zoff
# add_flag -Wno-error=double-promotion
add_flag -Wno-double-promotion

# add_flag -Wno-error=missing-variable-declarations
add_flag -Wno-missing-variable-declarations

# add_flag -Wno-error=missing-prototypes
add_flag -Wno-missing-prototypes

add_flag -Wno-error=incompatible-pointer-types-discards-qualifiers
add_flag -Wno-error=deprecated-declarations

# add_flag -Wno-error=unused-macros
add_flag -Wno-unused-macros

#add_flag -Wno-error=bad-function-cast
add_flag -Wno-bad-function-cast

#add_flag -Wno-error=float-equal
add_flag -Wno-float-equal

#add_flag -Wno-error=cast-qual
add_flag -Wno-cast-qual

#add_flag -Wno-error=strict-prototypes
add_flag -Wno-strict-prototypes

#add_flag -Wno-error=gnu-statement-expression
add_flag -Wno-gnu-statement-expression

#add_flag -Wno-error=documentation
add_flag -Wno-documentation

# reactivate this later! ------------
# add_flag -Wno-error=pointer-sign
add_flag -Wno-pointer-sign
# add_flag -Wno-error=extra-semi-stmt
# add_flag -Wno-error=undef
# reactivate this later! ------------


add_flag -Werror
add_flag -fdiagnostics-color=always






## ---------------------------
cd /root/work/
git clone https://github.com/zoff99/ToxAndroidRefImpl
cd /root/work/ToxAndroidRefImpl/jni-c-toxcore/
pwd
ls -al
## ---------------------------

dirname $(find /usr -name 'jni.h' 2>/dev/null|head -1) > /tmp/xx1
dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
export JAVADIR1=$(cat /tmp/xx1)
export JAVADIR2=$(cat /tmp/xx2)
echo "JAVADIR1:""$JAVADIR1"
echo "JAVADIR2:""$JAVADIR2"


export CFLAGS=" -fPIC -std=gnu99 -I$_INST_/include/ -L$_INST_/lib -O3 -g -fstack-protector-all "

set -x

clang-10 $CFLAGS \
$C_FLAGS $CXX_FLAGS $LD_FLAGS \
-D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
-I$JAVADIR1/ \
-I$JAVADIR2/ \
-L/usr/local/lib \
-I/usr/local/include/ \
jni-c-toxcore.c \
-Wl,-whole-archive \
$_INST_/lib/libtoxcore.a \
$_INST_/lib/libtoxav.a \
$_INST_/lib/libtoxencryptsave.a \
$_INST_/lib/libopus.a \
$_INST_/lib/libvpx.a \
$_INST_/lib/libx264.a \
$_INST_/lib/libavcodec.a \
$_INST_/lib/libavutil.a \
$_INST_/lib/libsodium.a \
-Wl,-no-whole-archive
-lpthread -shared \
-lm \
-ldl \
-lcurl \
-Wl,-soname,libjni-c-toxcore.so -o libjni-c-toxcore.so

set +x

ls -al libjni-c-toxcore.so

# -------------- now compile the JNI lib ----------------------


#    - cd jni-c-toxcore ; rm -fv libjni-c-toxcore.so
#    - dirname $(find /usr -name 'jni.h' 2>/dev/null|head -1) > /tmp/xx1
#    - dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
#    - cd jni-c-toxcore ; export JAVADIR1=$(cat /tmp/xx1); export JAVADIR2=$(cat /tmp/xx2); gcc -fPIC -O2 -g
#      -funwind-tables -Wl,-soname,libjni-c-toxcore.so -o libjni-c-toxcore.so -std=gnu99
#      -D_FILE_OFFSET_BITS=64 -D__USE_GNU=1
#      -I$JAVADIR1/
#      -I$JAVADIR2/
#      -L/usr/local/lib
#      -I/usr/local/include/
#      jni-c-toxcore.c
#      -Wl,-whole-archive
#      ../libsodium/src/libsodium/.libs/libsodium.a
#      /usr/local/lib/libtoxcore.a
#      /usr/local/lib/libtoxav.a
#      -Wl,-no-whole-archive
#      -lpthread -shared
#    - cd jni-c-toxcore ; ls -al libjni-c-toxcore.so ; exit 0
#    - cd jni-c-toxcore ; file libjni-c-toxcore.so ; exit 0
#    - cd jni-c-toxcore ; ldd libjni-c-toxcore.so ; exit 0
#    - cd jni-c-toxcore ; cp -av libjni-c-toxcore.so ../java_ref_client/app/src/main/java/
# --------- compile java example ---------
#    - cd java_ref_client/app/src/main/java/ ; javac com/zoffcc/applications/trifa/ToxVars.java
#    - cd java_ref_client/app/src/main/java/ ; javac com/zoffcc/applications/trifa/TRIFAGlobals.java
#    - cd java_ref_client/app/src/main/java/ ; javac com/zoffcc/applications/trifa/MainActivity.java
#    - cd java_ref_client/app/src/main/java/ ; javac com/zoffcc/applications/trifa/TrifaToxService.java
# --------- package java example ---------
#    - cd java_ref_client/app/src/main/java/ ; tar -cvf $CIRCLE_ARTIFACTS/ubuntu_14_04_binaries/install_linux.tar com *.sh *.so
# --------- run test java application ---------
#    - cd java_ref_client/app/src/main/java/ ; java -Djava.library.path="." com.zoffcc.applications.trifa.MainActivity > trifa.log 2>&1 :
#        background: true
# --------- run test java application ---------
#    - sleep 10
#    - cd java_ref_client/app/src/main/java/ ; cat ./trifa.log
#    - cd java_ref_client/app/src/main/java/ ; cat ./trifa.log | grep 'MyToxID:' | cut -d':' -f 4
#    - sleep 10
#    - sleep 240
#    - cd java_ref_client/app/src/main/java/ ; cat ./trifa.log
#    - cd java_ref_client/app/src/main/java/ ; cp -v ./trifa.log $CIRCLE_ARTIFACTS/ ; exit 0


