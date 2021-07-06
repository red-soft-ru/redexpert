@echo off

if exist "%SYSTEMDRIVE%\Program Files (x86)\" (
	cd bin
	call RedExpert64.exe -Xms1024m
) else (
	cd bin
	call RedExpert.exe -Xms1024m
)