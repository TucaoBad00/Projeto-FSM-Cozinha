@echo off
REM Compila e abre a simulacao visual (duplo clique neste arquivo)
cd /d "%~dp0"
javac -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
  echo Erro ao compilar.
  pause
  exit /b 1
)
java -cp out SimulacaoVisual
