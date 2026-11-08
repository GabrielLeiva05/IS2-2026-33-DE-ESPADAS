@echo off
REM Levanta todas las aplicaciones de Dulce Mayor.
REM Uso: levantar.cmd [-LimpiarBase] [-ConDatosDemo] [-SinMoviles] [-AbrirNavegador] [-Detener] [-Verificar]
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0levantar.ps1" %*