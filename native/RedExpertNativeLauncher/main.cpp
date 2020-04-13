#ifdef __linux__
#include <dirent.h>
#include <dlfcn.h>
#include <libgen.h>
#include <unistd.h>
#include <string.h>
#include <system_error>
#else
#pragma comment(lib, "user32.lib")
#include <windows.h>
#include <ShellAPI.h>
#include <regex>
#include <WinReg.hpp>
#include "HKEY.h"
#include <cstdint>
#endif

#include "utils.h"
#include "JniError.h"
#include "JniString.h"
#include "PortableJni.h"
#include "reportFatalErrorViaGui.h"

#include <algorithm>
#include <deque>
#include <fstream>
#include <iostream>
#include <map>
#include <sstream>
#include <stdexcept>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <vector>

std::vector<std::string> get_potential_libjvm_paths();

struct UsageError : std::runtime_error
{
    explicit UsageError(const std::string &description)
        : std::runtime_error(description) {}
};

typedef std::deque<std::string> NativeArguments;

extern "C"
{
#ifdef __linux__
typedef jint (*CreateJavaVM)(JavaVM **, void **, void *);
typedef jint (*CreateJvmFuncPtr) (JavaVM**, void**, JavaVMInitArgs*);
#else
typedef jint (_stdcall *CreateJavaVM)(JavaVM **, void **, void *);
typedef jint (_stdcall *CreateJvmFuncPtr) (JavaVM**, void**, JavaVMInitArgs*);
#endif
}

struct ErrorReporter
{
public:
    std::string ARGV0;
    NativeArguments launcher_args;
    std::string support_address;
    std::ostringstream progress_os;

private:
    std::string getUsage() const;
    void generateReport(const std::exception &ex, const std::string &usage) const;

public:
    ErrorReporter() : ARGV0("<unknown>") {}

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

static void abortJvm() { err_rep.abortJvm(); }

#ifdef __linux__
std::string getSelfPath()
{
    char buff[PATH_MAX];
    ssize_t len = ::readlink("/proc/self/exe", buff, sizeof(buff) - 1);
    if (len != -1)
    {
        buff[len] = '\0';
        return std::string(buff);
    }
}
#endif

int executeCmdEx(const char* cmd, std::string &result)
{
    char buffer[128];
    int ret_code = -1; // -1 if error ocurs.
    std::string command(cmd);
    command.append(" 2>&1"); // also redirect stderr to stdout

    result = "";
    FILE* pipe;
#ifdef __linux__
    pipe = popen(command.c_str(), "r");
#else
    pipe = _popen(command.c_str(), "r");
#endif
    if (pipe != NULL) {
        try {
            while (!feof(pipe)) {
                if (fgets(buffer, 128, pipe) != NULL)
                    result += buffer;
            }
        }
        catch (...) {
#ifdef __linux__
            ret_code = pclose(pipe);
#else
            ret_code = _pclose(pipe);
#endif
            throw;
        }
#ifdef __linux__
        ret_code = pclose(pipe);
#else
        ret_code = _pclose(pipe);
#endif
    }
    return ret_code;
}


std::string
replaceFirstOccurrence(
    std::string& s,
    const std::string& toReplace,
    const std::string& replaceWith)
{
    std::size_t pos = s.find(toReplace);
    if (pos == std::string::npos) return s;
    return s.replace(pos, toReplace.length(), replaceWith);
}

typedef void *SharedLibraryHandle;

SharedLibraryHandle
openSharedLibrary(const std::string &sl_file)
{
    std::ostringstream os;
#ifdef _WIN32
    std::string arch;
	#if INTPTR_MAX == INT32_MAX
    arch = "x86";
	#elif INTPTR_MAX == INT64_MAX
    arch = "amd64";
	#else
	#error "Environment not 32 or 64-bit."
	#endif
    std::string out;
    std::string server_jvm = "server/jvm.dll";
    std::string client_jvm = "client/jvm.dll";
    std::string java_exe = "java.exe";
    std::string sl_f = sl_file;
    std::string path_to_java = replaceFirstOccurrence(sl_f,server_jvm,java_exe);
    path_to_java = replaceFirstOccurrence(path_to_java,client_jvm,java_exe);
    std::string cmd = "\"" +path_to_java +"\"" + " -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);
    if(out.find("Property settings")==std::string :: npos)
    {
        std::string err = "File "+sl_f+" not_found";
        throw err;
    }
    std::regex jarch_regex("os\\.arch\\s\\=\\s(([\\w+\\s\\\\\\-:\\.])+)\\n");
    std::smatch match;
    bool support = false;
    if(std::regex_search(out, match, jarch_regex))
    {
        std::string str = match[1].str();
        support = str==arch;
    }
    if(!support)
    {

        std::string err = "File "+ sl_f+" not support arch this application! this application need in java with arch: " +arch;
        throw err;
    }
    void *sl_handle = LoadLibraryA(sl_file.c_str());
    if (sl_handle == 0)
    {
        DWORD l_err = GetLastError();
#else
    void *sl_handle = dlopen(sl_file.c_str(), RTLD_LAZY);
    if (sl_handle == 0)
    {
        os << "dlopen(\"" << sl_handle << "\") failed with "
           << dlerror() << ".";
#endif
        os << std::endl;
        os << "If you can't otherwise explain why this call failed, consider "
              "whether all of the shared libraries";
        os << " used by this shared library can be found.";
        os << std::endl;
#ifdef _WIN32
        os << "This command's output may help:";
        os << std::endl;
        os << "objdump -p \"" << sl_file << "\" | grep DLL";
        os << std::endl;
        os << "LoadLibrary(\"" << sl_file << "\")";
        throw utils::WindowsError(os.str(), l_err);
#else
        throw std::runtime_error(os.str());
#endif
    }
    return sl_handle;
}

static std::string readFile(const std::string &path)
{
    std::ifstream is(path.c_str());
    if (is.good() == false)
    {
        throw std::runtime_error("Couldn't open \"" + path + "\".");
    }
    std::ostringstream contents;
    contents << is.rdbuf();
    return contents.str();
}

#ifdef _WIN32

static std::string readRegistryFile(const HKEY hive, const std::string &path)
{
    winreg::RegKey key;
    key.Open(hive, std::wstring(path.begin(), path.end()));
    std::wstring w_version = key.GetStringValue(L"JavaHome");
    std::string contents = std::string(w_version.begin(), w_version.end());
    // MSDN's article on RegQueryValueEx explains that the value may or may not
    // include the null terminator.
    if (contents.empty() == false && contents[contents.size() - 1] == '\0')
    {
        return contents.substr(0, contents.size() - 1);
    }
    return contents;
}

struct JvmRegistryKey
{
public:
    typedef JvmRegistryKey self;

private:
    HKEY m_hive;
    std::string ver;
    std::string reg_path;
    std::string jre_path;

public:
    JvmRegistryKey(HKEY &hive, const std::string &reg_root, const std::string &a_ver,
                   const std::string &a_jre_path)
        : m_hive(hive), ver(a_ver),
          reg_path(reg_root + "\\" + ver),
          jre_path(a_jre_path) {}

    std::string readJreBin() const
    {
        std::string java_home = readRegistryFile(m_hive, reg_path);
        std::string jre_bin = java_home + jre_path + "\\bin";
        return jre_bin;
    }

    bool operator<(const self &rhs) const { return ver < rhs.ver; }

    void dumpTo(std::ostream &os) const { os << reg_path; }

    friend std::ostream &operator<<(std::ostream &os, const self &rhs)
    {
        rhs.dumpTo(os);
        return os;
    }
};

typedef std::vector<JvmRegistryKey> JvmRegistryKeys;

#endif

bool isUnreasonableVersion(std::ostream &os, const std::string &version)
{
    if (version.empty() || isdigit(version[0]) == false)
    {
        os << "\"";
        os << version;
        os << "\" is not a number";
        os << std::endl;
        return true;
    }
    if (version < "1.8")
    {
        os << version;
        os << " is too old";
        os << std::endl;
        return true;
    }
    return false;
}

#ifdef _WIN32

void findVersionsInRegistry(std::ostream &os, HKEY &hive, JvmRegistryKeys &jvm_reg_keys,
                            const std::string &reg_ath,
                            const char *jre_path)
{
    std::string s_hive = utils::toString(hive);
    os << "Looking for registered JVMs under \"";
    os << s_hive;
    os << reg_ath;
    os << "\" [";
    os << std::endl;
    try
    {
        winreg::RegKey key;
        key.Open(hive, std::wstring(reg_ath.begin(), reg_ath.end()));
        std::wstring w_version = key.GetStringValue(L"CurrentVersion");
        std::string version = std::string(w_version.begin(), w_version.end());
        if (isUnreasonableVersion(os, version))
        {
            return;
        }
        JvmRegistryKey jvmRegistryKey(hive, reg_ath, version,
                                      jre_path);
        jvm_reg_keys.push_back(jvmRegistryKey);
    }
    catch (const std::exception &ex)
    {
        os << ex.what();
        os << std::endl;
    }
    os << "]";
    os << std::endl;
}

SharedLibraryHandle tryVersions(const char *jvm_dir, HKEY hive,
                                const char *java_vendor, const char *jdk_name,
                                const char *jre_name)
{
    const std::string reg_prefix = utils::toString("SOFTWARE\\") + java_vendor + "\\";
    const std::string jre_reg_path = reg_prefix + jre_name;
    const std::string jdk_reg_path = reg_prefix + jdk_name;
    std::ostream &os = err_rep.progress_os;
    JvmRegistryKeys jvm_reg_keys;
    findVersionsInRegistry(os, hive, jvm_reg_keys, jre_reg_path, "");
    // Sun JDK key:
    // "JavaHome"="C:\\Program Files\\Java\\jdk1.5.0_06"
    // IBM JDK has an appended "jre" component:
    // "JavaHome"="C:\\Program Files\\IBM\\Java50\\jre"
    findVersionsInRegistry(os, hive, jvm_reg_keys, jdk_reg_path, "\\jre");
    std::sort(jvm_reg_keys.begin(), jvm_reg_keys.end());
    while (jvm_reg_keys.empty() == false)
    {
        JvmRegistryKey jvm_reg_key = jvm_reg_keys.back();
        jvm_reg_keys.pop_back();
        os << "Trying \"";
        os << jvm_reg_key;
        os << "\" [";
        os << std::endl;
        try
        {
            std::string jre_bin = jvm_reg_key.readJreBin();
            std::string jvm_location = jre_bin + "\\" + jvm_dir + "\\jvm.dll";
            return openSharedLibrary(jvm_location);
        }
        catch (std::string  &ex)
        {
            os << ex;
            os << std::endl;
        }
        catch (const std::exception &ex)
        {
            os << ex.what();
            os << std::endl;
        }

        os << "]";
        os << std::endl;
    }

    std::vector<std::string> p_paths = get_potential_libjvm_paths();

    for (std::vector<std::string>::const_iterator it = p_paths.begin(), en = p_paths.end(); it != en;
         ++it)
    {
        std::string p_jvm = *it;
        os << "Trying potential path \"";
        os << p_jvm;
        os << "\" [";
        os << std::endl;
        try
        {
            return openSharedLibrary(p_jvm);
        }
        catch (std::string &ex)
        {
            os << ex;
            os << std::endl;
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
        os << "]";
        os << std::endl;
    }

    std::ostringstream err_os;
    err_os << "tryVersions(\"" << jvm_dir << "\", " << hive << ", "
                 << java_vendor << ", " << jdk_name << ", " << jre_name
                 << ") failed";
    throw std::runtime_error(err_os.str());
}

SharedLibraryHandle tryHives(const char *jvm_dir, const char *java_vendor,
                             const char *jdk_name, const char *jre_name)
{
    typedef std::deque<HKEY> Hives;
    Hives hives;
    hives.push_back(HKEY_CURRENT_USER);
    hives.push_back(HKEY_LOCAL_MACHINE);
    for (Hives::const_iterator it = hives.begin(), en = hives.end(); it != en;
         ++it)
    {
        HKEY hive = *it;
        try
        {
            return tryVersions(jvm_dir, hive, java_vendor, jdk_name, jre_name);
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
    }
    std::ostringstream os;
    os << "tryHives(\"" << jvm_dir << "\", " << java_vendor << ", " << jdk_name
       << ", " << jre_name << ") failed";
    throw std::runtime_error(os.str());
}

#endif


std::vector<std::string> get_potential_libjvm_paths()
{
    std::vector<std::string> libjvm_potential_paths;

    std::vector<std::string> search_prefixes;
    std::vector<std::string> search_suffixes;
    std::string file_name;

    // From heuristics
#ifdef _WIN32
    search_prefixes = {""};
    search_suffixes = {"/jre/bin/server", "/bin/server", "/bin/client" };
    file_name = "jvm.dll";
#else
    search_prefixes.push_back("/usr/lib/jvm/default-java");       // ubuntu / debian distros
    search_prefixes.push_back("/usr/lib/jvm/java");               // rhel6
    search_prefixes.push_back("/usr/lib/jvm");                    // centos6
    search_prefixes.push_back("/usr/lib64/jvm");                  // opensuse 13
    search_prefixes.push_back("/usr/local/lib/jvm/default-java"); // alt ubuntu / debian distros
    search_prefixes.push_back("/usr/local/lib/jvm/java");         // alt rhel6
    search_prefixes.push_back("/usr/local/lib/jvm");              // alt centos6
    search_prefixes.push_back("/usr/local/lib64/jvm");            // alt opensuse 13

    search_prefixes.push_back("/usr/lib/jvm/java-8-openjdk-amd64");
    search_prefixes.push_back("/usr/local/lib/jvm/java-8-openjdk-amd64");

    search_prefixes.push_back("/usr/local/lib/jvm/java-9-openjdk-amd64"); // alt ubuntu / debian distros
    search_prefixes.push_back("/usr/lib/jvm/java-9-openjdk-amd64");       // alt ubuntu / debian distros

    search_prefixes.push_back("/usr/lib/jvm/java-10-openjdk-amd64");       // alt ubuntu / debian distros
    search_prefixes.push_back("/usr/local/lib/jvm/java-10-openjdk-amd64"); // alt ubuntu / debian distros

    search_prefixes.push_back("/usr/lib/jvm/java-11-openjdk-amd64");       // alt ubuntu / debian distros
    search_prefixes.push_back("/usr/local/lib/jvm/java-11-openjdk-amd64"); // alt ubuntu / debian distros

    search_prefixes.push_back("/usr/lib/jvm/java-12-openjdk-amd64");       // alt ubuntu / debian distros
    search_prefixes.push_back("/usr/local/lib/jvm/java-12-openjdk-amd64"); // alt ubuntu / debian distros

    search_prefixes.push_back("/usr/lib/jvm/java-8-oracle");  // alt ubuntu
    search_prefixes.push_back("/usr/lib/jvm/java-9-oracle");  // alt ubuntu
    search_prefixes.push_back("/usr/lib/jvm/java-10-oracle"); // alt ubuntu
    search_prefixes.push_back("/usr/lib/jvm/java-11-oracle"); // alt ubuntu
    search_prefixes.push_back("/usr/lib/jvm/java-12-oracle"); // alt ubuntu

    search_prefixes.push_back("/usr/local/lib/jvm/java-8-oracle");  // alt ubuntu
    search_prefixes.push_back("/usr/local/lib/jvm/java-9-oracle");  // alt ubuntu
    search_prefixes.push_back("/usr/local/lib/jvm/java-10-oracle"); // alt ubuntu
    search_prefixes.push_back("/usr/local/lib/jvm/java-11-oracle"); // alt ubuntu
    search_prefixes.push_back("/usr/local/lib/jvm/java-12-oracle"); // alt ubuntu
    search_prefixes.push_back("/usr/lib/jvm/default");              // alt centos
    search_prefixes.push_back("/usr/java/latest");                  // alt centos

    search_suffixes.push_back("");
    search_suffixes.push_back("/lib/amd64/server");
    search_suffixes.push_back("/jre/lib/amd64/server");
    search_suffixes.push_back("/lib/amd64/client");
    search_suffixes.push_back("/jre/lib/amd64/client");
    search_suffixes.push_back("jre/lib/amd64");
    search_suffixes.push_back("/lib/server");
    search_suffixes.push_back("/lib/client");


    file_name = "libjvm.so";
#endif
    // From direct environment variable
    char *env_value = NULL;
    if ((env_value = getenv("JAVA_HOME")) != NULL)
    {
        search_prefixes.insert(search_prefixes.begin(), env_value);
    }
    else
    {
        // If the environment variable is not set,
        // then there may be java is in global path?
        std::string out;
        executeCmdEx("java -XshowSettings:properties -version", out);
#ifdef _WIN32
        std::ostream &os = err_rep.progress_os;
        os<<out;
        std::regex jhome_regex("java\\.home\\s\\=\\s(([\\w+\\s\\\\\\-:\\.\\(\\)])+)\\n");
        std::smatch match;
        if(std::regex_search(out, match, jhome_regex))
        {
            std::string str = match[1].str();
            os<<"java.home = "<<str;
            std::string str86 = replaceFirstOccurrence(str,"Program Files\\","Program Files (x86)\\");
            std::string str64 = replaceFirstOccurrence(str,"Program Files (x86)\\","Program Files\\");
            search_prefixes.insert(search_prefixes.begin(), strdup(str86.c_str()));
            search_prefixes.insert(search_prefixes.begin(), strdup(str64.c_str()));
            search_prefixes.insert(search_prefixes.begin(), strdup(str.c_str()));
        }
#elif __linux__
        std::string jhome_pat = "java.home = ";
        int jhome_pos = out.find(jhome_pat.c_str()) +  jhome_pat.length();
        int end_pos = out.find("\n", jhome_pos);
        std::string java_env = strdup(out.substr(jhome_pos, end_pos - jhome_pos).c_str());
        search_prefixes.insert(search_prefixes.begin(), java_env);
#endif

    }

    // Generate cross product between search_prefixes, search_suffixes, and
    // file_name
    for (std::vector<std::string>::const_iterator it = search_prefixes.begin(), en = search_prefixes.end(); it != en;
         ++it)
    {
        for (std::vector<std::string>::iterator it_s = search_suffixes.begin(), en_s = search_suffixes.end(); it_s != en_s;
             ++it_s)
        {
            std::string prefix = *it;
            std::string suffix = *it_s;
            std::string path = prefix + "/" + suffix + "/" + file_name;
            libjvm_potential_paths.push_back(path);
        }
    }

    return libjvm_potential_paths;
}

#ifdef _WIN32

struct JavaVendorRegistryLocation
{
    const char *java_vendor;
    const char *jdk_name;
    const char *jre_name;

    JavaVendorRegistryLocation(const char *a_java_vendor, const char *a_jdk_name,
                               const char *a_jre_name)
        : java_vendor(a_java_vendor), jdk_name(a_jdk_name), jre_name(a_jre_name) {}
};

SharedLibraryHandle tryVendors(const char *jvm_dir)
{
    typedef std::deque<JavaVendorRegistryLocation> JavaVendorRegistryLocations;
    JavaVendorRegistryLocations vendor_reg_locations;
    vendor_reg_locations.push_back(JavaVendorRegistryLocation(
                                              "JavaSoft", "Java Development Kit", "Java Runtime Environment"));
    vendor_reg_locations.push_back(JavaVendorRegistryLocation(
                                              "IBM", "Java Development Kit", "Java2 Runtime Environment"));
    for (JavaVendorRegistryLocations::const_iterator it =
         vendor_reg_locations.begin();
         it != vendor_reg_locations.end(); ++it)
    {
        try
        {
            return tryHives(jvm_dir, it->java_vendor, it->jdk_name, it->jre_name);
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
    }
    std::ostringstream os;
    os << "tryVendors(\"" << jvm_dir << "\") failed";
    throw std::runtime_error(os.str());
}

SharedLibraryHandle tryDirectories(bool isClient, bool isServer)
{
    typedef std::deque<const char *> JvmDirectories;
    JvmDirectories jvmDirectories;
    if (isClient == false && isServer == false)
    {
        isClient = true;
        isServer = true;
    }
    if (isClient)
    {
        jvmDirectories.push_back("client");
    }
    if (isServer)
    {
        jvmDirectories.push_back("server");
    }
    for (JvmDirectories::const_iterator it = jvmDirectories.begin(),
         en = jvmDirectories.end();
         it != en; ++it)
    {
        const char *jvmDirectory = *it;
        try
        {
            return tryVendors(jvmDirectory);
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
    }
    std::ostringstream os;
    os << "tryDirectories(" << isClient << ", " << isServer << ") failed";
    throw std::runtime_error(os.str());
}

// Once we've successfully opened a shared library, I think we're committed to
// trying to use it or else who knows what its DLL entry point has done. Until
// we've successfully opened it, though, we can keep trying alternatives.
SharedLibraryHandle openWindowsJvmLibrary(bool isClient, bool isServer)
{
    std::ostream &os = err_rep.progress_os;
    os << "Trying to find ";
    os << sizeof(void *) * 8;
    os << " bit jvm.dll - we need a 1.8 or newer JRE or JDK.";
    os << std::endl;
    os << "Error messages were:";
    os << std::endl;
    return tryDirectories(isClient, isServer);
}

#endif

#ifdef __linux__
int try_dlopen(std::vector<std::string> potential_paths, void *&out_handle)
{
    for (std::vector<std::string>::iterator it = potential_paths.begin(), en = potential_paths.end(); it != en;
         ++it)
    {
        std::string p_path = *it;
        out_handle = dlopen(p_path.c_str(), RTLD_NOW | RTLD_LOCAL);

        if (out_handle != 0)
        {
            break;
        }
    }

    if (out_handle == 0)
    {
        return 0;
    }
    return 1;
}
#endif

SharedLibraryHandle openJvmLibrary(bool isClient, bool isServer)
{
#ifdef _WIN32
    return openWindowsJvmLibrary(isClient, isServer);
#else
    (void)isClient;
    (void)isServer;
    std::vector<std::string> paths = get_potential_libjvm_paths();
    void *handler = 0;
    if (try_dlopen(paths, handler))
    {
        return static_cast<SharedLibraryHandle>(handler);
    } else
    {
        std::ostringstream os;
        os << "dlopen failed with "
           << dlerror() << ".";
        return 0;
    }
#endif
}

bool startsWith(const std::string &st, const std::string &prefix)
{
    return st.substr(0, prefix.size()) == prefix;
}

struct Properties : public std::map<std::string, std::string>
{
    void parse(const NativeArguments &arguments)
    {
        for (NativeArguments::const_iterator it = arguments.begin(),
             en = arguments.end();
             it != en; ++it)
        {
            std::string option = *it;
            if (startsWith(option, "-D") == false)
            {
                continue;
            }
            size_t offset = option.find('=');
            if (offset == std::string::npos)
            {
                continue;
            }
            std::string name = option.substr(2, offset - 2);
            std::string value = option.substr(offset + 1);
            (*this)[name] = value;
        }
    }
};

class LauncherArgumentParser
{
private:
    Properties properties;
    bool isClient;
    bool isServer;
    NativeArguments jvmArguments;
    std::string className;
    NativeArguments mainArguments;

public:
    explicit LauncherArgumentParser(const NativeArguments &launcherArguments)
    {
        properties.parse(launcherArguments);
        // Try to set the mailing list address before reporting errors.
        err_rep.support_address = properties["supportAddress"];
        isClient = false;
        isServer = false;
        NativeArguments::const_iterator it = launcherArguments.begin();
        NativeArguments::const_iterator end = launcherArguments.end();
        while (it != end && startsWith(*it, "-"))
        {
            std::string option = *it++;
            if (option == "-cp" || option == "-classpath")
            {
                if (it == end)
                {
                    throw UsageError(option + " requires an argument.");
                }
                // Translate to a form the JVM understands.
                std::string classPath = *it++;
                jvmArguments.push_back("-Djava.class.path=" + classPath);
            }
            else if (option == "-client")
            {
                isClient = true;
            }
            else if (option == "-server")
            {
                isServer = true;
            }
            else if (startsWith(option, "-D") || startsWith(option, "-X"))
            {
                jvmArguments.push_back(option);
            }
            else
            {
                mainArguments.push_back(option);
            }
        }
        if (it == end)
        {
            throw UsageError("No class specified.");
        }
        className = *it++;
        while (it != end)
        {
            mainArguments.push_back(*it++);
        }
    }

    SharedLibraryHandle openJvmLibrary() const
    {
        return ::openJvmLibrary(isClient, isServer);
    }

    NativeArguments getJvmArguments() const { return jvmArguments; }

    std::string getMainClassName() const { return className; }

    NativeArguments getMainArguments() const { return mainArguments; }
};

template <class ExtraInfo> struct JvmOption : JavaVMOption
{
    JvmOption(const char *ostr_arg, ExtraInfo ei_arg)
    {
        optionString = const_cast<char *>(ostr_arg);
        extraInfo = const_cast<void *>(reinterpret_cast<const void *>(
                                           reinterpret_cast<uintptr_t>(ei_arg)));
    }
};

template <class ExtraInfo>
JvmOption<ExtraInfo> makeJvmOption(const char *ostr_arg,
                                   ExtraInfo ei_arg)
{
    return JvmOption<ExtraInfo>(ostr_arg, ei_arg);
}

JvmOption<void *> makeJvmOption(const char *ostr_arg)
{
    void *ei_arg = 0;
    return JvmOption<void *>(ostr_arg, ei_arg);
}

struct JavaInvocation
{
private:
    JavaVM *vm;
    JNIEnv *env;
    LauncherArgumentParser &l_args;

private:
    CreateJavaVM findCreateJavaVM()
    {
        SharedLibraryHandle sl_handle = 0;
        sl_handle = l_args.openJvmLibrary();

        CreateJavaVM jvm = 0;
#ifdef __linux__
        try
        {
            jvm = reinterpret_cast<CreateJavaVM>(reinterpret_cast<uintptr_t>(
                                                     dlsym(sl_handle, "JNI_CreateJavaVM")));
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
#else
        jvm = reinterpret_cast<CreateJavaVM>(reinterpret_cast<uintptr_t>(
                                                 GetProcAddress(reinterpret_cast<HMODULE>(sl_handle), "JNI_CreateJavaVM")));
#endif
        if (jvm == 0)
        {

            std::ostringstream os;
            // Hopefully our caller will report something more useful than the shared
            // library handle.
#ifdef __linux__
            os << "dlsym(" << sl_handle
               << ", JNI_CreateJavaVM) failed with " << dlerror() << ".\n";
#else
            os << "dlsym(" << sl_handle
               << ", JNI_CreateJavaVM) failed with " << GetLastError() << ".\n";
#endif
            if (sl_handle == 0)
                os << "A Java Runtime Environment (JRE) or Java Development Kit (JDK) must be available in order to run Red Expert. " <<
                      "No Java virtual machine was found.\n" <<
                      "If the required Java Runtime Environment is not installed, you can download it from from the website:\n" <<
                      "http://java.com/\n" <<
                      "then try again.";
            throw std::runtime_error(os.str());
        }
        return jvm;
    }

    jclass findClass(const std::string &c_name)
    {
        // Internally, the JVM tends to use '/'-separated class names.
        // Externally, '.'-separated class names are more common.
        // '.' is never valid in a class name, so we can unambiguously translate.
        std::string can_name(c_name);
        for (std::string::iterator it = can_name.begin(),
             end = can_name.end();
             it != end; ++it)
        {
            if (*it == '.')
            {
                *it = '/';
            }
        }

        jclass j_class = env->FindClass(can_name.c_str());
        if (j_class == 0)
        {
            std::ostringstream os;
            reportAnyJavaException(os);
            os << "FindClass(\"" << can_name
               << "\") failed (FindClass loads and initializes the class as well as "
                  "finding it).";
            throw std::runtime_error(os.str());
        }
        return j_class;
    }

    jmethodID findMainMethod(jclass cl_main)
    {
        jmethodID method =
                env->GetStaticMethodID(cl_main, "main", "([Ljava/lang/String;)V");
        if (method == 0)
        {
            throw std::runtime_error("GetStaticMethodID(\"main\") failed.");
        }
        return method;
    }

    jstring makeJavaString(const char *n_string)
    {
        jstring j_string = env->NewStringUTF(n_string);
        if (j_string == 0)
        {
            std::ostringstream os;
            os << "NewStringUTF(\"" << n_string << "\") failed.";
            throw std::runtime_error(os.str());
        }
        return j_string;
    }

    jobjectArray convertArguments(const NativeArguments &n_args)
    {
        jclass str_cl = findClass("java/lang/String");
        jstring def_args = makeJavaString("");
        jobjectArray j_args = env->NewObjectArray(
                    n_args.size(), str_cl, def_args);
        if (j_args == 0)
        {
            std::ostringstream os;
            os << "NewObjectArray(" << n_args.size() << ") failed.";
            throw std::runtime_error(os.str());
        }
        for (size_t index = 0; index != n_args.size(); ++index)
        {
            std::string n_arg = n_args[index];
            jstring j_arg = makeJavaString(n_arg.c_str());
            env->SetObjectArrayElement(j_args, index, j_arg);
        }
        return j_args;
    }

public:
    explicit JavaInvocation(LauncherArgumentParser &l_argsA)
        : l_args(l_argsA)
    {
        typedef std::vector<JavaVMOption>
                JavaVMOptions; // Required to be contiguous.
        const NativeArguments &jvm_args = l_args.getJvmArguments();
        JavaVMOptions j_vm_opts;
        for (size_t i = 0; i != jvm_args.size(); ++i)
        {
            j_vm_opts.push_back(makeJvmOption(jvm_args[i].c_str()));
        }

        j_vm_opts.push_back(makeJvmOption("abort", &abortJvm));

        JavaVMInitArgs j_vm_init_args;

        j_vm_init_args.version = JNI_VERSION_1_8;
        j_vm_init_args.options = &j_vm_opts[0];
        j_vm_init_args.nOptions = j_vm_opts.size();
        j_vm_init_args.ignoreUnrecognized = false;

        CreateJavaVM cr_java_vm = findCreateJavaVM();
        int result =
                cr_java_vm(&vm, reinterpret_cast<void **>(&env), &j_vm_init_args);
        if (result < 0)
        {
            std::ostringstream os;
            os << "JNI_CreateJavaVM(options=[";
            for (size_t i = 0; i < j_vm_opts.size(); ++i)
            {
                os << (i > 0 ? ", " : "") << '"' << j_vm_opts[i].optionString
                   << '"';
            }
            os << "]) failed with " << JniError(result) << ".";
            throw std::runtime_error(os.str());
        }
    }

    void reportAnyJavaException(std::ostream &os)
    {
        jthrowable j_exc = env->ExceptionOccurred();
        if (j_exc == 0)
        {
            return;
        }
        // Report it via stderr first, in case we fail later and overwrite the
        // pending exception.
        env->ExceptionDescribe();
        os << "A Java exception occurred.";
        os << std::endl;
        jclass str_utl = env->FindClass("org/apache/commons/lang/exception/ExceptionUtils");
        if (str_utl == 0)
        {
            os << "FindClass(\"org.apache.commons.lang.exception.ExceptionUtils\") failed.";
            os << std::endl;
            return;
        }
        jmethodID throw_stack =
                env->GetStaticMethodID(str_utl, "getStackTrace",
                                       "(Ljava/lang/Throwable;)Ljava/lang/String;");
        if (throw_stack == 0)
        {
            os << "GetStaticMethodID(org.apache.commons.lang.exception.ExceptionUtils, "
                  "\"getStackTrace(Throwable)\") failed.";
            os << std::endl;
            return;
        }
        jstring report = static_cast<jstring>(env->CallStaticObjectMethod(
                                                  str_utl, throw_stack, j_exc));
        os << JniString(env, report);
    }

    ~JavaInvocation()
    {
        // If you attempt to destroy the VM with a pending JNI exception,
        // the VM crashes with an "internal error" and good luck to you finding
        // any reference to it on the web.
        if (env->ExceptionCheck())
        {
            env->ExceptionDescribe();
        }

        // The non-obvious thing about DestroyJavaVM is that you have to call this
        // in order to wait for all the Java threads to quit - even if you don't
        // care about "leaking" the VM.
        // Deliberately ignore the error code, as the documentation says we must.
        vm->DestroyJavaVM();
    }

    int invokeMain()
    {
        jclass j_class = findClass(l_args.getMainClassName());
        jmethodID j_method = findMainMethod(j_class);
        jobjectArray j_args =
                convertArguments(l_args.getMainArguments());
        env->CallStaticVoidMethod(j_class, j_method, j_args);
        if (env->ExceptionCheck() == false)
        {
            return 0;
        }
        std::ostringstream os;
        reportAnyJavaException(os);
        os << l_args.getMainClassName() << ".main(...) failed.";
        throw std::runtime_error(os.str());
    }
};

std::string ErrorReporter::getUsage() const
{
    std::ostringstream os;
    os << "Usage: " << ARGV0 << " [options] class [args...]" << std::endl;
    os << "where options are:" << std::endl;
    os << "  -client - use client VM" << std::endl;
    os << "  -server - use server VM" << std::endl;
    os << "  -cp <path> | -classpath <path> - set the class search path"
       << std::endl;
#ifdef _WIN32
    os << "   (As the JVM is a native Windows program, the class path must use "
          "Windows directory names separated by semicolons.)"
       << std::endl;
#endif
    os << "  -D<name>=<value> - set a system property" << std::endl;
    os << "  -verbose[:class|gc|jni] - enable verbose output" << std::endl;
    os << "or any option implemented internally by the chosen JVM." << std::endl;
    os << std::endl;
    return os.str();
}

void ErrorReporter::generateReport(const std::exception &ex,
                                   const std::string &usage) const
{
    std::ostringstream os;
#ifdef _WIN32
    os << "If you don't have Java installed, download it from http://java.com/, "
          "then try again."
       << std::endl;
    os << std::endl;
#endif
    os << "Error: " << ex.what() << std::endl;
    os << std::endl;

    os << "JVM selection:" << std::endl;
    os << progress_os.str();
    os << std::endl;

    os << usage;

    os << "Command line was:";
    os << std::endl;
    os << ARGV0 << " ";
    os << utils::join(" ", launcher_args);
    os << std::endl;

    reportFatalErrorViaGui("Red Expert", os.str(), support_address);
}

#ifdef _WIN32

LONG CALLBACK handleVectoredException(PEXCEPTION_POINTERS)
{
    return EXCEPTION_CONTINUE_SEARCH;
}

static void deferToHotSpotExceptionHandler()
{
    ULONG first_handler = 1;
    PVOID handle =
            AddVectoredContinueHandler(first_handler, &handleVectoredException);
    if (handle == 0)
    {
        // No mention is made of GetLastError() at the MSDN page du jour.
        throw std::runtime_error("AddVectoredContinueHandler failed");
    }
    // The handle of the next handler seems to be at our handle.
    // The previous handle follows.
    // The rest of the structure remained opaque to me, though I didn't try
    // RtlDecodePointer.
    void *w_handle = *reinterpret_cast<void **>(handle);
    ULONG rc = RemoveVectoredContinueHandler(w_handle);
    if (rc == 0)
    {
    }
    rc = RemoveVectoredContinueHandler(handle);
    if (rc == 0)
    {
        throw std::runtime_error("RemoveVectoredContinueHandler(handle) failed");
    }
}

#else

static void deferToHotSpotExceptionHandler() {}

#endif

static int runJvm(const NativeArguments &l_args)
{
    try
    {
        LauncherArgumentParser parser(l_args);
        deferToHotSpotExceptionHandler();
        JavaInvocation j_invocation(parser);
        return j_invocation.invokeMain();
    }
    catch (const UsageError &ex)
    {
        err_rep.reportUsageError(ex);
        return 1;
    }
    catch (const std::exception &ex)
    {
        err_rep.reportFatalException(ex);
        return 1;
    }
}

int main(int argc, char *argv[])
{
    // if linux system is used need to set desktop environment to NONE,
    // otherwise launcher is crashed
    // TODO check it on Centos 7
#ifdef __linux__
    setenv("XDG_CURRENT_DESKTOP", "NONE", 1);
#endif

    std::string separator = ";";
#ifdef __linux__
    separator = ":";
#endif

    std::string paths("-Djava.class.path=");
    std::string app_exe_path;
    std::string bin_dir;
    std::string app_dir;
    int app_pid;
#ifdef __linux__
    app_exe_path = getSelfPath();
    std::string tmp_path = app_exe_path;
    bin_dir = dirname((char *)tmp_path.c_str());
    app_pid = getpid();
#else
    // hide console window
    ::ShowWindow(::GetConsoleWindow(), SW_HIDE);
    HMODULE h_module = GetModuleHandleW(NULL);
    WCHAR path[MAX_PATH];
    GetModuleFileNameW(h_module, path, MAX_PATH);
    std::wstring ws(path);
    std::string str(ws.begin(), ws.end());
    app_exe_path = str;
    char buf[256];
    GetCurrentDirectoryA(256, buf);
    bin_dir = std::string(buf);
    app_pid = GetCurrentProcessId();
#endif

    app_dir = bin_dir + "/..";
    paths.append(app_dir + "/RedExpert.jar");
    paths.append(separator);

#ifdef __linux__
    paths.append(app_dir + "/createDesktopEntry.sh");
    paths.append(separator);
    paths.append(app_dir + "/redexpert.desktop");
    paths.append(separator);
#endif

    std::string lib_dir(app_dir + "/lib");

#ifdef __linux__
    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir(lib_dir.c_str())) != NULL)
    {
        while ((ent = readdir(dir)) != NULL)
        {
            int res = strcmp(ent->d_name, "fbplugin-impl.jar");
            int res2 = strcmp(ent->d_name, "jaybird-full.jar");
            int res3 = strcmp(ent->d_name, "jaybird-cryptoapi.jar");
            if (!res || !res2 || !res3)
                continue;
            paths.append(lib_dir + "/" + ent->d_name);
            paths.append(separator);
        }
        closedir(dir);
    } else
    {
        err_rep.reportFatalException(std::system_error(ENOENT, std::generic_category(), "No library directory found"));
        exit(EXIT_FAILURE);
    }
#else
    WIN32_FIND_DATA data;
    std::wstring wlib_dir(lib_dir.begin(), lib_dir.end());
    wlib_dir.append(L"\\*");
    HANDLE h_find = FindFirstFile(wlib_dir.c_str(), &data);

    if (h_find != INVALID_HANDLE_VALUE)
    {
        do
        {
            // convert from wide char to narrow char array
            char buffer[1024];
            char def_char = '\0';
            WideCharToMultiByte(CP_ACP, 0, data.cFileName, -1, buffer, 260, &def_char,
                                NULL);
            std::string conv_file(buffer);
            int res = strcmp(conv_file.c_str(), "fbplugin-impl.jar");
            int res2 = strcmp(conv_file.c_str(), "jaybird-full.jar");
            int res3 = strcmp(conv_file.c_str(), "jaybird-cryptoapi.jar");
            if (!res || !res2 || !res3)
                continue;
            if (conv_file != "." && conv_file != "..")
            {
                paths.append(lib_dir + "\\");
                paths.append(conv_file);
                paths.append(separator);
            }
        } while (FindNextFile(h_find, &data));
        FindClose(h_find);
        int localLength = paths.length();
        paths.resize(paths.length() - 2);
    }
#endif

    int sep_idx = paths.find_last_of(separator);
    paths = paths.substr(0, sep_idx);

    // add to java class path Red Expert jar
    std::string stdString = paths;
    char *class_path = (char *)stdString.c_str();

    NativeArguments launcher_args;

    err_rep.ARGV0 = *argv++;
    while (*argv != 0)
    {
        launcher_args.push_back(*argv++);
    }
    err_rep.launcher_args = launcher_args;

    launcher_args.push_back(class_path);
    launcher_args.push_back("org/executequery/ExecuteQuery");

    launcher_args.push_back("-exe_path=" + app_exe_path);
    std::string str_pid = utils::toString(app_pid);
    launcher_args.push_back("-exe_pid=" + str_pid);

    return runJvm(launcher_args);
}
