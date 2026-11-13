@echo off
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java test\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
del fuentes.txt
xcopy /s /y /i /q src\rpg\vista\recursos out\rpg\vista\recursos >nul
echo Compilado en out\
