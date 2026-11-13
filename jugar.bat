@echo off
cd /d "%~dp0"
if not exist out call compilar.bat
java -cp out rpg.Main
