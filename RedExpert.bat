@echo off

if exist "%SYSTEMDRIVE%\Program Files (x86)\" (
	cd bin
	call RedXpert64.exe -Xms1024m
) else (
	cd bin
	call RedXpert.exe -Xms1024m
)