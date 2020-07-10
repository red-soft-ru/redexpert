group red_expert

artifact bin VERSION
file dist/unified/RedExpert-VERSION.tar.gz tar.gz bin
file dist/unified/RedExpert-VERSION.zip zip bin
end

artifact installer VERSION
file dist/unified/RedExpert-VERSION-installer-windows-amd64.exe  windows-x64.exe installer
file dist/unified/RedExpert-VERSION-installer-windows-x86.exe  windows-x86.exe installer
file dist/unified/RedExpert-VERSION-installer-linux-x86_64.bin  linux-x64.bin installer
file dist/unified/RedExpert-VERSION-installer-linux-x86.bin  linux-x86.bin installer
end

artifact src VERSION
file dist-src/RedExpert-VERSION-src.tar.gz tar.gz src
file dist-src/RedExpert-VERSION-src.zip zip src
end
