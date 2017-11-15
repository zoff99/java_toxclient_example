#! /bin/bash

cd $(dirname "$0")
export _HOME_=$(pwd)
echo $_HOME_


export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/

if [ "$1""x" == "fx" ]; then
	rm -Rf $_SRC_
	rm -Rf $_INST_
fi

mkdir -p $_SRC_
mkdir -p $_INST_

export LD_LIBRARY_PATH=$_INST_/lib/



if [ "$1""x" != "ux" ]; then


cd $_SRC_
git clone --depth=1 --branch=1.0.13 https://github.com/jedisct1/libsodium.git
cd libsodium
./autogen.sh
export CFLAGS=" -static --static -fPIC "
export LDFLAGS=" -static --static "
./configure --prefix=$_INST_ --disable-shared --disable-soname-versions # --enable-minimal
make -j 4
make install

cd $_SRC_
git clone --depth=1 --branch=v1.6.1 https://github.com/webmproject/libvpx.git
cd libvpx
export CFLAGS=" -fPIC "
./configure --prefix=$_INST_ --disable-examples --disable-unit-tests --enable-shared
make -j 4
make install

cd $_SRC_
git clone --depth=1 --branch=v1.2.1 https://github.com/xiph/opus.git
cd opus
./autogen.sh
export CFLAGS=" -fPIC "
./configure --prefix=$_INST_ --disable-shared
make -j 4
make install

cd $_SRC_
# git clone https://github.com/TokTok/c-toxcore
git clone https://github.com/zoff99/c-toxcore
cd c-toxcore

git checkout 6c88bd0811a499ee72229e28796121e501a25245

./autogen.sh

export CFLAGS=" -I$_INST_/include/ -fPIC "
export LDFLAGS=-L$_INST_/lib
./configure \
--prefix=$_INST_ \
--enable-logging \
--disable-soname-versions --disable-testing --disable-shared
make -j 4
make install

fi

cd $_HOME_/java_toxclient_example/jni-c-toxcore/

dirname $(find /usr -name 'jni.h' 2>/dev/null|head -1) > /tmp/xx1
dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
export JAVADIR1=$(cat /tmp/xx1)
export JAVADIR2=$(cat /tmp/xx2)

gcc -fPIC -O2 -g \
      -funwind-tables -Wl,-soname,libjni-c-toxcore.so \
      -o libjni-c-toxcore.so -std=gnu99 \
      -D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
      -I$JAVADIR1/ \
      -I$JAVADIR2/ \
      -L$_INST_/lib \
      -I$_INST_/include \
      jni-c-toxcore.c \
      -Wl,-whole-archive \
      $_INST_/lib/libsodium.a \
      $_INST_/lib/libtoxcore.a \
      $_INST_/lib/libtoxav.a \
      -Wl,-no-whole-archive \
      -lpthread -shared

#gcc -g -O3 -fPIC -export-dynamic -I$_INST_/include \
#-o toxblinkenwall -lm toxblinkenwall.c \
#-std=gnu99 \
#-L$_INST_/lib \
#$_INST_/lib/libtoxcore.a \
#$_INST_/lib/libtoxav.a \
#-lrt \
#$_INST_/lib/libopus.a \
#$_INST_/lib/libvpx.a \
#-lm \
#$_INST_/lib/libsodium.a


cd $_HOME_



