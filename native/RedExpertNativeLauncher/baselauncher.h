#include <map>
#include <deque>
#include <string>
#include <vector>
#include <thread>
#include <fstream>
#include <sstream>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <iostream>
#include <algorithm>
#include <stdexcept>

#include "utils.h"
#include "JniError.h"
#include "JniString.h"
#include "PortableJni.h"
#include "reportFatalErrorViaGui.h"

static std::string djvm;
static std::string dpath;
static std::string locale;
static std::string bin_dir;
static std::string app_dir;
static std::string user_dir;
static std::string djava_home;
static std::string app_exe_path;
static std::string path_to_java_paths;

static int result_dialog;
typedef void *SharedLibraryHandle;
typedef std::deque<std::string> NativeArguments;

std::string runnable_command();
std::string file_separator();
std::string other_file_separator();
std::string extension_exe_file();

std::wstring wfile_separator();
std::wstring wother_file_separator();

int showDialog();
int invokeExecuteQuery(const NativeArguments &l_args);
int executeCmdEx(const char *cmd, std::string &result);

static void deferToHotSpotExceptionHandler();
bool startsWith(const std::string &st, const std::string &prefix);

SharedLibraryHandle checkParameters(bool from_file);
SharedLibraryHandle openJvmLibrary(bool isClient, bool isServer);
SharedLibraryHandle openSharedLibrary(const std::string &sl_file, std::string path, bool from_file_java_paths);

std::vector<std::string> get_search_suffixes();
std::vector<std::string> get_potential_libjvm_paths();
std::vector<std::string> get_potential_libjvm_paths_from_path(std::string path_parameter);

struct UsageError : std::runtime_error
{
    explicit UsageError(const std::string &description) : std::runtime_error(description)
    {
    }
};

struct Properties : public std::map<std::string, std::string>
{
    void parse(const NativeArguments &arguments)
    {
        for (NativeArguments::const_iterator it = arguments.begin(), en = arguments.end(); it != en; ++it)
        {
            std::string option = *it;
            if ((startsWith(option, "-D") == false) && (startsWith(option, "eq.") == false))
                continue;

            size_t offset = option.find('=');
            if (offset == std::string::npos)
                continue;

            std::string name;
            if (startsWith(option, "-D"))
                name = option.substr(2, offset - 2);
            else
                name = option.substr(0, offset);

            std::string value = option.substr(offset + 1);
            (*this)[name] = value;
        }
    }
};

template <class ExtraInfo>
struct JvmOption : JavaVMOption
{
    JvmOption(const char *ostr_arg, ExtraInfo ei_arg)
    {
        optionString = const_cast<char *>(ostr_arg);
        extraInfo = const_cast<void *>(reinterpret_cast<const void *>(reinterpret_cast<uintptr_t>(ei_arg)));
    }
};

struct ErrorReporter
{
public:
    int typeError;
    std::string ARGV0;
    std::string support_address;
    NativeArguments launcher_args;
    std::ostringstream progress_os;

private:
    std::string getUsage() const;
    void generateReport(const std::exception &ex, const std::string &usage) const;

public:
    ErrorReporter() : ARGV0("<unknown>")
    {
        typeError = 0;
    }

    void reportUsageError(const UsageError &ex) const
    {
        generateReport(ex, getUsage());
    }

    void reportFatalException(const std::exception &ex) const
    {
        generateReport(ex, "");
    }

    void abortJvm() const
    {
        std::runtime_error ex("JVM aborted");
        reportFatalException(ex);

        exit(EXIT_FAILURE);
    }
};
static ErrorReporter err_rep;

template <class ExtraInfo>
JvmOption<ExtraInfo> makeJvmOption(const char *ostr_arg, ExtraInfo ei_arg)
{
    return JvmOption<ExtraInfo>(ostr_arg, ei_arg);
}

JvmOption<void *> makeJvmOption(const char *ostr_arg)
{
    void *ei_arg = 0;
    return JvmOption<void *>(ostr_arg, ei_arg);
}

std::string replaceFirstOccurrence(
    std::string &s,
    const std::string &toReplace,
    const std::string &replaceWith)
{
    std::size_t pos = s.find(toReplace);
    if (pos == std::string::npos)
        return s;

    return s.replace(pos, toReplace.length(), replaceWith);
}

std::wstring replaceFirstOccurrenceW(
    std::wstring &s,
    const std::wstring &toReplace,
    const std::wstring &replaceWith)
{
    std::size_t pos = s.find(toReplace);
    if (pos == std::string::npos)
        return s;

    return s.replace(pos, toReplace.length(), replaceWith);
}

SharedLibraryHandle openSharedLibrary(const std::string &sl_file, bool from_file_java_paths)
{
    std::string sl_f = sl_file;
    std::string java_exe = "java" + extension_exe_file();
    std::string server_jvm = "server" + file_separator() + "jvm.dll";
    std::string client_jvm = "client" + file_separator() + "jvm.dll";
    std::string path_to_java = replaceFirstOccurrence(sl_f, server_jvm, "");
    path_to_java = replaceFirstOccurrence(path_to_java, client_jvm, "");

    return openSharedLibrary(sl_file, path_to_java, from_file_java_paths);
}

static std::string readFile(const std::string &path)
{
    std::ifstream is(path.c_str());
    if (is.good() == false)
        throw std::runtime_error("Couldn't open \"" + path + "\".");

    std::ostringstream contents;
    contents << is.rdbuf();

    return contents.str();
}

SharedLibraryHandle checkInputDialog()
{
    if (showDialog() == 0)
        return 0;

    SharedLibraryHandle handle = 0;
    handle = checkParameters(false);
    if (handle != 0)
        return handle;

    return checkInputDialog();
}

bool isUnreasonableVersion(std::ostream &os, const std::string &version)
{
    if (version.empty() || isdigit(version[0]) == false)
    {
        os << "\"" << version << "\" is not a number" << std::endl;
        return true;
    }

    if (version < "1.8")
    {
        os << version << " is too old" << std::endl;
        return true;
    }

    return false;
}

static int runJvm(const NativeArguments &l_args)
{
    try
    {
        return invokeExecuteQuery(l_args);
    }
    catch (std::string &ex)
    {
        std::cout << ex << std::endl;
        int res = ex.compare("CANCEL");
        if (res == 0)
            return 1;

        err_rep.reportUsageError(UsageError(ex));
    }
    catch (std::exception &ex)
    {
        // clear cached java path
        djvm = "";

        // offer to download java
        checkInputDialog();

        const char *command = runnable_command().c_str();
        std::cout << std::endl
                  << "Restarting aplication..." << std::endl
                  << "Command: " << command << std::endl;

        // restart native launcher
        system(command);
        std::exit(EXIT_SUCCESS);
    }
}

bool startsWith(const std::string &st, const std::string &prefix)
{
    return st.substr(0, prefix.size()) == prefix;
}

static void abortJvm()
{
    err_rep.abortJvm();
}
