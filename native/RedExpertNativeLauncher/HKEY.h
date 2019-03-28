#ifndef HKEY_H_included
#define HKEY_H_included

#include <iostream>

#ifdef _WIN32
#include <windows.h>
#else
struct HKey;
typedef HKey* HKEY;
static HKEY HKEY_CURRENT_USER;
static HKEY HKEY_LOCAL_MACHINE;
#endif

/**
 * It prints the name of the hive
 */
inline std::ostream& operator<<(std::ostream& os, HKEY hive)
{
    if (hive == HKEY_CURRENT_USER)
    {
        return os << "HKEY_CURRENT_USER";
    }
    if (hive == HKEY_LOCAL_MACHINE)
    {
        return os << "HKEY_LOCAL_MACHINE";
    }
    void* pointer = hive;
    return os << "(unrecognized HKEY)" << pointer;
}

#endif
