@echo off
setlocal

if "%1"=="" (
    echo Usage: jansansad [query | populate]
    cmd /k
    exit /b 1
)

if "%1"=="query" (
    echo Running sbt with runMain query...
    sbt "runMain com.jansansad.query"
    cmd /k
    exit /b
)

if "%1"=="download" (
    echo Running sbt with runMain query...
    sbt "runMain com.jansansad.download"
    cmd /k
    exit /b
)

if "%1"=="populate" (
    echo Running sbt with runMain populate...
    sbt "runMain com.jansansad.populate"
    cmd /k
    exit /b
)

echo Invalid option: %1
echo Usage: jansansad.bat [query | populate]
cmd /k
exit /b 1
