@echo off
echo Building plugin...
call gradlew build

echo Copying plugin to server plugins folder...
copy /Y .\build\libs\*.jar "C:\mc-server-1.21-TEST\plugins\"

pause