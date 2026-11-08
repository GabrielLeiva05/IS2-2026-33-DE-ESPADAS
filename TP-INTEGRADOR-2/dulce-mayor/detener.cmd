@echo off
REM Detiene los servicios levantados por levantar.ps1 (y la base con -IncluirBase).
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0levantar.ps1" -Detener %*