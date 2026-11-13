#!/bin/sh
cd "$(dirname "$0")"
[ -d out ] || ./compilar.sh
java -cp out rpg.Main
