#!/bin/sh
# Compila todo a out/ (y copia los sprites)
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find src test -name '*.java') && mkdir -p out/rpg/vista && cp -r src/rpg/vista/recursos out/rpg/vista/ && echo "Compilado en out/"
