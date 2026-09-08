@echo off
chcp 65001 >nul
setlocal
title Instalar Gestor Driver

set "APK=%~dp0GestorDriver.apk"
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo.
echo  Gestor Driver — instalacao pelo cabo
echo  ------------------------------------
echo.

if not exist "%APK%" (
  echo Nao achei o arquivo:
  echo   %APK%
  echo Deixe GestorDriver.apk na mesma pasta deste instalar.bat
  goto :fim
)

if not exist "%ADB%" (
  echo Nao achei o ADB em:
  echo   %ADB%
  echo Instale o Android SDK Platform-Tools ou abra o projeto no Android Studio uma vez.
  goto :fim
)

echo Celular conectado:
"%ADB%" devices
echo.

"%ADB%" wait-for-device
echo Instalando... isso pode levar um minuto.
echo.
"%ADB%" install -r "%APK%"
if errorlevel 1 (
  echo.
  echo Falhou. Confira:
  echo  1. Cabo de dados ^(nao so carga^)
  echo  2. No celular: USB para transferencia de arquivos
  echo  3. Depuracao USB ligada ^(Opcoes do desenvolvedor^)
  echo  4. Aceite o aviso "Permitir depuracao USB" na tela do celular
  goto :fim
)

echo.
echo Pronto. Abra o Gestor Driver na lista de apps.

:fim
echo.
pause
endlocal
