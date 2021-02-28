#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

echo $_HOME_
cd $_HOME_


build_for='
alpine:3.12.0
ubuntu:16.04
ubuntu:18.04
ubuntu:20.04
'

# debian:9
# debian:10
# archlinux/archlinux:base-20210118.0.13862


for system_to_build_for in $build_for ; do

    system_to_build_for_orig="$system_to_build_for"
    system_to_build_for=$(echo "$system_to_build_for_orig" 2>/dev/null|tr ':' '_' 2>/dev/null)

    cd $_HOME_/
    mkdir -p $_HOME_/"$system_to_build_for"/

    # rm -Rf $_HOME_/"$system_to_build_for"/script 2>/dev/null
    # rm -Rf $_HOME_/"$system_to_build_for"/workspace 2>/dev/null

    mkdir -p $_HOME_/"$system_to_build_for"/artefacts
    mkdir -p $_HOME_/"$system_to_build_for"/script
    mkdir -p $_HOME_/"$system_to_build_for"/workspace

    ls -al $_HOME_/"$system_to_build_for"/

    rsync -a ../ --exclude=.localrun $_HOME_/"$system_to_build_for"/workspace/data
    chmod a+rwx -R $_HOME_/"$system_to_build_for"/workspace/data

    echo '#! /bin/bash


pkgs_Ubuntu_18_04="
    :u:
    ca-certificates
    libconfig-dev
    wget
    unzip
    zip
    automake
    autotools-dev
    build-essential
    check
    checkinstall
    libtool
    pkg-config
    rsync
    git
    ffmpeg
    libavcodec-dev
    libavdevice-dev
    libsodium-dev
    libvpx-dev
    libopus-dev
    libx264-dev
    openjdk-11-jdk
    clang-10
    gdb
    nano
"


pkgs_Ubuntu_16_04="
    :u:
    software-properties-common
    :c:add-apt-repository\sppa:jonathonf/ffmpeg-4\s-y
    :u:
    libfuse-dev
    libfuse2
    ca-certificates
    libconfig-dev
    cmake
    wget
    unzip
    zip
    passwd
    ffmpeg
    automake
    autotools-dev
    build-essential
    check
    checkinstall
    libtool
    pkg-config
    rsync
    git
    libavcodec-dev
    libavdevice-dev
    libsodium-dev
    libvpx-dev
    libopus-dev
    libx264-dev
    openjdk-9-jdk-headless
    clang-8
    gdb
    nano
"

    pkgs_AlpineLinux_3_12_0="
        :c:apk\supdate
        :c:apk\sadd\sshadow
        :c:apk\sadd\sgit
        :c:apk\sadd\sunzip
        :c:apk\sadd\szip
        :c:apk\sadd\smake
        :c:apk\sadd\scmake
        :c:apk\sadd\sgcc
        :c:apk\sadd\slinux-headers
        :c:apk\sadd\smusl-dev
        :c:apk\sadd\sautomake
        :c:apk\sadd\sautoconf
        :c:apk\sadd\scheck
        :c:apk\sadd\slibtool
        :c:apk\sadd\srsync
        :c:apk\sadd\sgit
        :c:apk\sadd\slibx11-dev
        :c:apk\sadd\slibxext-dev
        :c:apk\sadd\sfreetype-deb
        :c:apk\sadd\sfontconfig-dev
        :c:apk\sadd\sopenal-soft-dev
        :c:apk\sadd\slibxrender-dev
        :c:apk\sadd\sffmpeg
        :c:apk\sadd\sffmpeg-dev
        :c:apk\sadd\salsa-lib
        :c:apk\sadd\salsa-lib-dev
        :c:apk\sadd\sv4l-utils
        :c:apk\sadd\sv4l-utils-dev
        :c:apk\sadd\slibjpeg
        :c:apk\sadd\slibsodium
        :c:apk\sadd\slibsodium-dev
        :c:apk\sadd\slibsodium-static
        :c:apk\sadd\slibvpx
        :c:apk\sadd\slibvpx-dev
        :c:apk\sadd\sopus
        :c:apk\sadd\sopus-dev
        :c:apk\sadd\sx264
        :c:apk\sadd\sx264-dev
        :c:apk\sadd\sclang
        :c:apk\sadd\sfile
        :c:apk\sadd\sjava-common
        :c:apk\sadd\sopenjdk11-jdk
        :c:apk\sadd\sopenjdk11-jre
"

    pkgs_ArchLinux_="
        :c:pacman\s-Sy
        :c:pacman\s-S\s--noconfirm\score/make
        :c:pacman\s-S\s--noconfirm\scmake
        :c:pacman\s-S\s--noconfirm\sopenal
        :c:pacman\s-S\s--noconfirm\sffmpeg
        :c:pacman\s-S\s--noconfirm\slibsodium
        :c:pacman\s-S\s--noconfirm\sv4l-utils
        :c:pacman\s-S\s--noconfirm\sautomake\s--ignore=perl,gcc-libs
        :c:pacman\s-S\s--noconfirm\slibx11
        :c:pacman\s-S\s--noconfirm\slibxext
        :c:pacman\s-S\s--noconfirm\slibxrender
        :c:pacman\s-S\s--noconfirm\sextra/check
        :c:pacman\s-S\s--noconfirm\sautoconf\s--ignore=perl,gcc-libs
        :c:pacman\s-S\s--noconfirm\slibtool\s--ignore=perl,gcc-libs
        :c:pacman\s-S\s--noconfirm\sgit
        :c:pacman\s-S\s--noconfirm\sjdk11-openjdk
        :c:pacman\s-S\s--noconfirm\sjre11-openjdk
        :c:pacman\s-S\s--noconfirm\sgcc\s--ignore=perl,gcc-libs
        :c:pacman\s-S\s--noconfirm\sclang\s--ignore=perl,gcc-libs
        :c:pacman\s-S\s--noconfirm\sbase-devel\s--ignore=perl,gcc-libs
        :c:pacman\s-S\s--noconfirm\sglibc\s--ignore=perl,gcc-libs
"


pkgs_Ubuntu_20_04="$pkgs_Ubuntu_18_04"
pkgs_DebianGNU_Linux_9="$pkgs_Ubuntu_18_04"
pkgs_DebianGNU_Linux_10="$pkgs_Ubuntu_18_04"

export DEBIAN_FRONTEND=noninteractive


os_release=$(cat /etc/os-release 2>/dev/null|grep "PRETTY_NAME=" 2>/dev/null|cut -d"=" -f2)
echo "using /etc/os-release"
system__=$(cat /etc/os-release 2>/dev/null|grep "^NAME=" 2>/dev/null|cut -d"=" -f2|tr -d "\""|sed -e "s#\s##g")
version__=$(cat /etc/os-release 2>/dev/null|grep "^VERSION_ID=" 2>/dev/null|cut -d"=" -f2|tr -d "\""|sed -e "s#\s##g")

echo "compiling on: $system__ $version__"

pkgs_name="pkgs_"$(echo "$system__"|tr "." "_"|tr "/" "_")"_"$(echo $version__|tr "." "_"|tr "/" "_")
echo "PKG:-->""$pkgs_name""<--"

for i in ${!pkgs_name} ; do
    if [[ ${i:0:3} == ":u:" ]]; then
        echo "apt-get update"
        apt-get update > /dev/null 2>&1
    elif [[ ${i:0:3} == ":c:" ]]; then
        cmd=$(echo "${i:3}"|sed -e "s#\\\s# #g")
        echo "$cmd"
        $cmd > /dev/null 2>&1
    else
        echo "apt-get install -y --force-yes ""$i"
        apt-get install -qq -y --force-yes $i > /dev/null 2>&1
    fi
done

#------------------------

if [ "$pkgs_name""x" == "pkgs_AlpineLinux_3_12_0""x" ]; then
    ls -al /usr/lib/jvm/java-11-openjdk/bin/
    JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
    export JAVA_HOME
    PATH="$JAVA_HOME/bin:${PATH}"
    export PATH
    echo $PATH
    javac -version
fi

pwd
ls -al
id -a

mkdir -p /workspace/data/
cd /workspace/data/jni-c-toxcore/ || exit 1

ls -al

../circle_scripts/deps.sh || exit 1
bash ../circle_scripts/java_jni_lib.sh "../" || exit 1


#------------------------


' > $_HOME_/"$system_to_build_for"/script/run.sh

    docker run -ti --rm \
      -v $_HOME_/"$system_to_build_for"/artefacts:/artefacts \
      -v $_HOME_/"$system_to_build_for"/script:/script \
      -v $_HOME_/"$system_to_build_for"/workspace:/workspace \
      --net=host \
     "$system_to_build_for_orig" \
     /bin/sh -c "apk add bash >/dev/null 2>/dev/null; /bin/bash /script/run.sh"
     if [ $? -ne 0 ]; then
        echo "** ERROR **:$system_to_build_for_orig"
        exit 1
     else
        echo "--SUCCESS--:$system_to_build_for_orig"
     fi

done

