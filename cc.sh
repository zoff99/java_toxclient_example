#! /bin/bash

a=abcdefghijklmnopqrstuvwxyz
b=ABCDEFGHIJKLMNOPQRSTUVWXYZ
r="$1"
cat "$2"|sed "y/$a$b/${a:$r}${a::$r}${b:$r}${b::$r}/"
