# a Tox Client in Java for Linux [Minimal working Example]

Build Status
=
**CircleCI:** [![CircleCI](https://circleci.com/gh/zoff99/java_toxclient_example/tree/master.png?style=badge)](https://circleci.com/gh/zoff99/java_toxclient_example/tree/master)

Installation (Linux) method 1
=
```
git clone https://github.com/zoff99/java_toxclient_example
cd java_toxclient_example
cd java_ref_client/app/src/main/java/
chmod u+rx *.sh
./do_run.sh
```

Installation (Linux) method 2
=
```
wget 'https://circleci.com/api/v1/project/zoff99/java_toxclient_example/latest/artifacts/0/$CIRCLE_ARTIFACTS/ubuntu_14_04_binaries/install_linux.tar?filter=successful&branch=master' -O install_linux.tar
tar -xf install_linux.tar
chmod u+rx *.sh
./do_run.sh
```
**HINT:** if you get this error **Unsupported major.minor version 52.0** you need to install java8 runtime to run the example client

Development Snapshot Version (Linux)
=
the latest Development Snapshot can be downloaded from CircleCI, [here](https://circleci.com/api/v1/project/zoff99/java_toxclient_example/latest/artifacts/0/$CIRCLE_ARTIFACTS/ubuntu_14_04_binaries/install_linux.tar?filter=successful&branch=master)

