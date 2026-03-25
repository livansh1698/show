$ErrorActionPreference = "Stop"
Write-Host "============================================"
Write-Host "   Show Pure PowerShell Builder"
Write-Host "============================================"

# 禁止交互式提示并杀死可能占用的进程
$Host.UI.RawUI.WindowTitle = "Show Build"
Stop-Process -Name "Show" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "Show" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

$env:JAVA_HOME = 'D:\env\jdk-17.0.17'
$env:MAVEN_HOME = 'D:\env\apache-maven-3.9.12'
$env:NODE_HOME = 'C:\nvm4w\nodejs'
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:NODE_HOME;$env:PATH"

Write-Host "`n[1/3] Building Frontend..."
Set-Location frontend
npm install
npm run build
Set-Location ..

Write-Host "`n[2/3] Building Backend..."
mvn clean package -DskipTests

Write-Host "`n[3/3] Packaging App (jpackage)..."
if (Test-Path dist\Show) {
    Remove-Item -Recurse -Force dist\Show -ErrorAction SilentlyContinue
}
if (Test-Path target\jpackage-input) { Remove-Item -Recurse -Force target\jpackage-input }
New-Item -ItemType Directory -Force -Path target\jpackage-input | Out-Null
Copy-Item target\ddmo-1.0.0.jar target\jpackage-input\
Copy-Item target\lib\*.jar target\jpackage-input\

& "$env:JAVA_HOME\bin\jpackage.exe" --type app-image --input target\jpackage-input --main-jar ddmo-1.0.0.jar --main-class com.ddmo.app.DdmoLauncher --name Show --dest dist --java-options "-Xmx512m" --icon logo.ico

Write-Host "`n[4/4] Compiling Single EXE..."
if (Test-Path Show.zip) { Remove-Item Show.zip }
Set-Location dist
tar.exe -a -c -f ..\Show.zip Show
Set-Location ..

$CSC_PATH = "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
& $CSC_PATH /nologo /win32icon:logo.ico /target:winexe /out:Show.exe /res:Show.zip Packer.cs /r:System.IO.Compression.FileSystem.dll /r:System.Windows.Forms.dll

Write-Host "`n============================================"
Write-Host "[OK] Build Complete!"
Write-Host "============================================"
