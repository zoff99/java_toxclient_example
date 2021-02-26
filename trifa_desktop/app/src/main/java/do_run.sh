#! /bin/bash

java -Dsun.java2d.uiScale=3 -Djava.library.path="." -classpath ".:sqlite-jdbc-3.32.3.2.jar" com.zoffcc.applications.trifa.MainActivity 2>&1 | tee trifa.log
