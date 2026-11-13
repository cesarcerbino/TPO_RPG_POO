#!/bin/sh
cd "$(dirname "$0")"
./compilar.sh && java -cp out rpg.SimulacionTest
