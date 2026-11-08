@echo off
REM Reinicia la base de datos desde cero y levanta todo con los datos de demostración.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0levantar.ps1" -LimpiarBase -ConDatosDemo -AbrirNavegador %*