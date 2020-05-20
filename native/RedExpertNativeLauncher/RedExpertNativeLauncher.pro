#-------------------------------------------------
#
# Project created by QtCreator 2018-07-13T09:59:37
#
#-------------------------------------------------

TEMPLATE = app
CONFIG += console c++11
CONFIG += static
CONFIG -= app_bundle
CONFIG -= qt

win32: {
    contains(QT_ARCH, i386) {
        message("select 32-bit arch")
        TARGET = bin/RedExpert
        LIBS += -L"$$(JAVA_HOME)/lib/" -ljvm
        INCLUDEPATH += "$$(JAVA_HOME)/include/"
        INCLUDEPATH += "$$(JAVA_HOME)/include/win32/"
        DEPENDPATH += "$$(JAVA_HOME)/include/"
        DEPENDPATH += "$$(JAVA_HOME)/include/win32/"
    } else {
        message("select 64-bit arch")
        TARGET = bin/RedExpert64
        LIBS += -L"$$(JAVA_HOME)/lib/" -ljvm
        INCLUDEPATH += "$$(JAVA_HOME)/include/"
        INCLUDEPATH += "$$(JAVA_HOME)/include/win32/"
        DEPENDPATH += "$$(JAVA_HOME)/include/"
        DEPENDPATH += "$$(JAVA_HOME)/include/win32/"
    }
    QMAKE_CXXFLAGS_RELEASE -= -Zc:strictStrings
    QMAKE_CFLAGS_RELEASE -= -Zc:strictStrings
    QMAKE_CFLAGS -= -Zc:strictStrings
    QMAKE_CXXFLAGS -= -Zc:strictStrings
    
    QMAKE_CXXFLAGS_RELEASE = -O2 -MT
    QMAKE_CFLAGS_RELEASE = -O2 -MT
    QMAKE_CFLAGS = -O2 -MT
    QMAKE_CXXFLAGS = -O2 -MT
    LIBS += -lcomdlg32
    RC_FILE += ResourceScript.rc
    HEADERS +=\
           resource.h
    DISTFILES += ResourceScript.rc

}
else:unix: {
    contains(QT_ARCH, i386) {
        message("select 32-bit arch")
        TARGET = bin/RedExpert
        CONFIG += link_pkgconfig
        PKGCONFIG += gtk+-2.0
#        LIBS += -L/usr/lib/jvm/java-8-oracle/jre/lib/i386/server/ -ljvm
    } else {
        message("select 64-bit arch")
        TARGET = bin/RedExpert64
        CONFIG += link_pkgconfig
        PKGCONFIG += gtk+-2.0
#        LIBS += -L/usr/lib/jvm/java-8-oracle/jre/lib/amd64/server/ -ljvm
    }

    INCLUDEPATH += $$(JAVA_HOME)/include
    INCLUDEPATH += $$(JAVA_HOME)/include/linux

    # QMAKE_LFLAGS_RPATH=
    # add your own with quoting gyrations to make sure $ORIGIN gets to the command line unexpanded
    QMAKE_LFLAGS += "-Wl,-rpath,\'\$$ORIGIN\'"

    QMAKE_CXXFLAGS += -std=c++0x

    LIBS += -ldl
}

SOURCES += \
    main.cpp \
    unzip.cpp \
    zip.cpp

HEADERS += \
    JniError.h \
    JniString.h \
    PortableJni.h \
    reportFatalErrorViaGui.h \
    WinReg.hpp \
    HKEY.h \
    unzip.h \
    utils.h \
    zip.h








