#ifdef _WIN32
#include "winlauncher.h"

#elif __linux__
#include "linlauncher.h"

#endif

int main(int argc, char *argv[])
{
    runLauncher(argc, argv);
}