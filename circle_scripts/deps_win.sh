#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

cd "$_HOME_"

export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/

mkdir -p $_SRC_
mkdir -p $_INST_

export LD_LIBRARY_PATH=$_INST_/lib/
export PKG_CONFIG_PATH=$_INST_/lib/pkgconfig


# ----------- config ------------
ORIGPATH=$PATH
export ORIGPATH
NEWPATH=/usr/x86_64-w64-mingw32/bin:$PATH
export NEWPATH
export PATH=$NEWPATH

MAKEFLAGS=j$(nproc)
export MAKEFLAGS

WGET_OPTIONS="--timeout=10"
export WGET_OPTIONS

ARCH="x86_64"
export ARCH
# ----------- config ------------



# ---------- ffmpeg ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

FFMPEG_VERSION=4.1.3
FFMPEG_FILENAME="ffmpeg-$FFMPEG_VERSION.tar.xz"
rm -f ffmpeg-*.tar.xz
wget $WGET_OPTIONS "https://www.ffmpeg.org/releases/$FFMPEG_FILENAME" -O "$FFMPEG_FILENAME"
tar -xf "$FFMPEG_FILENAME"
cd ffmpeg-4.1.3/

  ./configure --arch="$ARCH" \
              --enable-gpl \
              --prefix="$_INST_" \
              --target-os="mingw32" \
              --cross-prefix="$ARCH-w64-mingw32-" \
              --pkg-config="pkg-config" \
              --extra-cflags="-static -O2 -g0" \
              --extra-ldflags="-lm -static" \
              --pkg-config-flags="--static" \
              --disable-swscale \
              --disable-network \
              --disable-everything \
              --disable-debug \
              --disable-shared \
              --disable-programs \
              --disable-protocols \
              --disable-doc \
              --disable-sdl2 \
              --disable-avfilter \
              --disable-avresample \
              --disable-filters \
              --disable-iconv \
              --disable-network \
              --disable-muxers \
              --disable-postproc \
              --disable-swresample \
              --disable-swscale-alpha \
              --disable-dct \
              --disable-dwt \
              --disable-lsp \
              --disable-lzo \
              --disable-mdct \
              --disable-rdft \
              --disable-fft \
              --disable-faan \
              --disable-vaapi \
              --disable-vdpau \
              --disable-zlib \
              --disable-xlib \
              --disable-bzlib \
              --disable-lzma \
              --disable-encoders \
              --disable-decoders \
              --disable-demuxers \
              --disable-parsers \
              --disable-bsfs \
              --enable-runtime-cpudetect \
              --enable-parser=h264 \
              --enable-decoder=h264 || exit 1

  make -j || exit 1
  make install

cd "$_HOME_"

fi
# ---------- ffmpeg ---------


# ---------- opus ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

OPUS_VERSION=1.3.1
OPUS_FILENAME="opus-$OPUS_VERSION.tar.gz"
rm -f opus-*.tar.gz
wget $WGET_OPTIONS "https://archive.mozilla.org/pub/opus/$OPUS_FILENAME" -O "$OPUS_FILENAME"
tar -xf "$OPUS_FILENAME"
cd opus*/

  CFLAGS="-O2 -g0" ./configure --host="$ARCH-w64-mingw32" \
                               --prefix="$_INST_" \
                               --disable-shared \
                               --enable-static \
                               --disable-soname-versions \
                               --disable-extra-programs \
                               --disable-doc || exit 1
  make || exit 1
  make install

cd "$_HOME_"

fi
# ---------- opus ---------



# ---------- sodium ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

SODIUM_VERSION=1.0.18
SODIUM_FILENAME="libsodium-$SODIUM_VERSION.tar.gz"
rm -f libsodium-*.tar.gz
wget $WGET_OPTIONS "https://download.libsodium.org/libsodium/releases/$SODIUM_FILENAME" -O "$SODIUM_FILENAME"
tar -xf "$SODIUM_FILENAME"
cd libsodium*/

  ./configure --host="$ARCH-w64-mingw32" \
              --prefix="$_INST_" \
              --disable-shared \
              --enable-static \
              --with-pic || exit 1

  make || exit 1
  make install

cd "$_HOME_"

fi
# ---------- sodium ---------


# ---------- vpx ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

VPX_VERSION=v1.8.0
VPX_FILENAME="libvpx-$VPX_VERSION.tar.gz"
rm -f libvpx-*.tar.gz
wget $WGET_OPTIONS "https://github.com/webmproject/libvpx/archive/$VPX_VERSION.tar.gz" -O "$VPX_FILENAME"
tar -xf "$VPX_FILENAME"
cd libvpx*/


  VPX_TARGET="x86_64-win64-gcc"
  CROSS="$ARCH-w64-mingw32-" ./configure --target="$VPX_TARGET" \
                                         --prefix="$_INST_" \
                                         --disable-shared \
                                         --size-limit=16384x16384 \
                                         --enable-onthefly-bitpacking \
                                         --enable-runtime-cpu-detect \
                                         --enable-realtime-only \
                                         --enable-multi-res-encoding \
                                         --enable-temporal-denoising \
                                         --enable-static \
                                         --disable-examples \
                                         --disable-tools \
                                         --disable-docs \
                                         --disable-unit-tests || exit 1

  make || exit 1
  make install

cd "$_HOME_"

fi
# ---------- vpx ---------


# --- NASM ---
if [ 1 == 1 ]; then

cd "$_SRC_"

    export PATH=$ORIGPATH

    rm -Rf nasm
    git clone http://repo.or.cz/nasm.git
    cd nasm/
    git checkout nasm-2.13.03

    ./autogen.sh
    ./configure --prefix=/

    make || exit 1

    # seems man pages are not always built. but who needs those
    touch nasm.1
    touch ndisasm.1
    make install

    type -a nasm

    nasm --version || exit 1
    
    export PATH=$NEWPATH
cd "$_HOME_"

fi
# --- NASM ---


# ---------- x264 ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

git clone https://code.videolan.org/videolan/x264.git
_X264_VERSION_="1771b556ee45207f8711744ccbd5d42a3949b14c"
cd x264/

  git checkout "$_X264_VERSION_"
       export CC=x86_64-w64-mingw32-gcc-win32
  export WINDRES=x86_64-w64-mingw32-windres
  CROSS="$ARCH-w64-mingw32-" ./configure --host="$ARCH-w64-mingw32" \
                                         --prefix="$_INST_" \
                                         --disable-opencl \
                                         --enable-static \
                                         --disable-avs \
                                         --disable-cli \
                                         --enable-pic || exit 
  export CC=""
  export WINDRES=""

  make || exit 1
  make install

cd "$_HOME_"

fi
# ---------- x264 ---------


# ---------- c-toxcore ---------

cd "$_SRC_"

git clone https://github.com/zoff99/c-toxcore c-toxcore
cd c-toxcore/
git checkout "zoff99/zoxcore_local_fork"

# ------ set c-toxcore git commit hash ------
git_hash_for_toxcore=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_toxcore"":YY"
cat toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
cd toxcore/ ; sed -i -e 's;^.*TOX_GIT_COMMIT_HASH.*$;#define TOX_GIT_COMMIT_HASH "'$git_hash_for_toxcore'";' tox.h
cd ../
cat toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
# ------ set c-toxcore git commit hash ------


# C_COMPILER=$ARCH-w64-mingw32-gcc
# CXX_COMPILER=$ARCH-w64-mingw32-g++
# RC_COMPILER=$ARCH-w64-mingw32-windres


autoreconf -fi
./configure \
    CFLAGS=" -O2 -g " \
    --prefix="$_INST_" \
    --disable-soname-versions \
    --host="$ARCH-w64-mingw32" \
    --disable-shared \
    --disable-testing \
    --disable-rt || exit 1

    export V=1 VERBOSE=1;make || exit 1
    make install

cd "$_HOME_"

# ---------- c-toxcore ---------


