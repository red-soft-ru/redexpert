#-------------------------------------------------
#
# Project created by QtCreator 2018-07-13T09:59:37
#
#-------------------------------------------------

QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TEMPLATE = app

# The following define makes your compiler emit warnings if you use
# any feature of Qt which has been marked as deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

# You can also make your code fail to compile if you use deprecated APIs.
# In order to do so, uncomment the following line.
# You can also select to disable deprecated APIs only up to a certain version of Qt.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0

QTPLUGIN += core

win32: {
    contains(QT_ARCH, i386) {
        message("select 32-bit arch")
        TARGET = bin/RedExpertNativeLauncher
        LIBS += -L"$$(JAVA_HOME)/lib/" -ljvm
        INCLUDEPATH += "$$(JAVA_HOME)/include/"
        INCLUDEPATH += "$$(JAVA_HOME)/include/win32/"
        DEPENDPATH += "$$(JAVA_HOME)/include/"
        DEPENDPATH += "$$(JAVA_HOME)/include/win32/"
    } else {
        message("select 64-bit arch")
        TARGET = bin/RedExpertNativeLauncher64
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

    RC_ICONS += red_expert.ico
}
else:unix: {
    contains(QT_ARCH, i386) {
        message("select 32-bit arch")
        TARGET = bin/RedExpertNativeLauncher
#        LIBS += -L/usr/lib/jvm/java-8-oracle/jre/lib/i386/server/ -ljvm
    } else {
        message("select 64-bit arch")
        TARGET = bin/RedExpertNativeLauncher64
#        LIBS += -L/usr/lib/jvm/java-8-oracle/jre/lib/amd64/server/ -ljvm
    }

    INCLUDEPATH += $$(JAVA_HOME)/include
    INCLUDEPATH += $$(JAVA_HOME)/include/linux

    # QMAKE_LFLAGS_RPATH=
    # add your own with quoting gyrations to make sure $ORIGIN gets to the command line unexpanded
    QMAKE_LFLAGS += "-Wl,-rpath,\'\$$ORIGIN\'"

    LIBS += -ldl
}

SOURCES += \
        main.cpp

HEADERS +=

FORMS +=
