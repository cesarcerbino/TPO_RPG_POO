@echo off
cd /d "%~dp0"
call compilar.bat
java -cp out rpg.SimulacionTest
