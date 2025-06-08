@echo off
setlocal

echo [1/3] Компиляция Java-файлов...
javac -cp "libs/*" -d out src/*.java src/uimodels/*.java
if %errorlevel% neq 0 (
    echo ❌ Ошибка компиляции
    exit /b %errorlevel%
)

echo [2/3] Создание JAR-файла...
jar cfe myapp.jar Main -C out .
if %errorlevel% neq 0 (
    echo ❌ Ошибка упаковки JAR
    exit /b %errorlevel%
)

echo [3/3] Запуск приложения...
java -cp "myapp.jar;libs/*" Main -gui
if %errorlevel% neq 0 (
    echo ❌ Ошибка выполнения
    exit /b %errorlevel%
)

echo ✅ Готово.
endlocal
pause
