#pragma comment(lib, "user32.lib")
#pragma comment(lib, "Urlmon.lib")
#pragma comment(lib, "WinINet.lib")
#pragma comment(lib, "shell32.lib")

#include <regex>
#include <thread>
#include <cctype>
#include <tchar.h>
#include <direct.h>
#include <windows.h>
#include <Wininet.h>
#include <ShellAPI.h>
#include <commctrl.h>
#include <WinReg.hpp>

#include "HKEY.h"
#include "unzip.h"
#include "resource.h"
#include "baselauncher.h"

#if INTPTR_MAX == INT64_MAX

static std::string arch = "amd64";

#elif INTPTR_MAX == INT32_MAX

static std::string arch = "x86";

#else
#error "Environment not 32 or 64-bit."
#endif

static HWND h_progress;
static HWND h_dialog_download;

HRESULT error_code;
static std::thread th;
static int showError = 0;

static std::wstring archive_dir;
static std::wstring archive_path;
static std::wstring archive_name = L"java.zip";

std::wstring get_download_url();
static std::string readRegistryFile(const HKEY hive, const std::string &path);
std::string get_property_from_regex(std::string reg_property, std::string source);

extern "C"
{
    typedef jint(_stdcall *CreateJavaVM)(JavaVM **, void **, void *);
    typedef jint(_stdcall *CreateJvmFuncPtr)(JavaVM **, void **, JavaVMInitArgs *);
}

class DownloadStatus : public IBindStatusCallback
{
private:
    int progress, filesize;
    int AbortDownload;

public:
    STDMETHOD(OnStartBinding)
    (
        /* [in] */ DWORD dwReserved,
        /* [in] */ IBinding __RPC_FAR *pib)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(GetPriority)
    (
        /* [out] */ LONG __RPC_FAR *pnPriority)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(OnLowResource)
    (
        /* [in] */ DWORD reserved)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(OnProgress)
    (
        /* [in] */ ULONG ulProgress,
        /* [in] */ ULONG ulProgressMax,
        /* [in] */ ULONG ulStatusCode,
        /* [in] */ LPCWSTR wszStatusText);

    STDMETHOD(OnStopBinding)
    (
        /* [in] */ HRESULT hresult,
        /* [unique][in] */ LPCWSTR szError)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(GetBindInfo)
    (
        /* [out] */ DWORD __RPC_FAR *grfBINDF,
        /* [unique][out][in] */ BINDINFO __RPC_FAR *pbindinfo)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(OnDataAvailable)
    (
        /* [in] */ DWORD grfBSCF,
        /* [in] */ DWORD dwSize,
        /* [in] */ FORMATETC __RPC_FAR *pformatetc,
        /* [in] */ STGMEDIUM __RPC_FAR *pstgmed)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(OnObjectAvailable)
    (
        /* [in] */ REFIID riid,
        /* [iid_is][in] */ IUnknown __RPC_FAR *punk)
    {
        return E_NOTIMPL;
    }

    // IUnknown methods.  Note that IE never calls any of these methods, since
    // the caller owns the IBindStatusCallback interface, so the methods all
    // return zero/E_NOTIMPL.

    STDMETHOD_(ULONG, AddRef)
    ()
    {
        return 0;
    }

    STDMETHOD_(ULONG, Release)
    ()
    {
        return 0;
    }

    STDMETHOD(QueryInterface)
    (
        /* [in] */ REFIID riid,
        /* [iid_is][out] */ void __RPC_FAR *__RPC_FAR *ppvObject)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(OnStartBinding)
    ()
    {
        AbortDownload = 0;
        progress = 0;
        filesize = 0;
        return E_NOTIMPL;
    }

    STDMETHOD(GetProgress)
    ()
    {
        return progress;
    }

    STDMETHOD(GetFileSize)
    ()
    {
        return filesize;
    }
    STDMETHOD(AbortDownl)
    ()
    {
        AbortDownload = 1;
        return E_NOTIMPL;
    }
    STDMETHOD(GetAbortDownl)
    ()
    {
        return AbortDownload;
    }
};

class LauncherArgumentParser
{
private:
    bool isClient;
    bool isServer;
    std::string className;
    Properties properties;
    NativeArguments jvmArguments;
    NativeArguments mainArguments;

public:
    explicit LauncherArgumentParser(const NativeArguments &launcherArguments)
    {
        properties.parse(launcherArguments);

        djvm = properties["jvm"];
        dpath = properties["path"];
        djava_home = properties["java_home"];
        user_dir = properties["eq.user.home.dir"];
        err_rep.support_address = properties["supportAddress"];

        std::wstring wuser_dir = utils::convertUtf8ToUtf16(user_dir);
        wuser_dir = replaceFirstOccurrenceW(wuser_dir, L"$HOME", _wgetenv(L"USERPROFILE"));
        wuser_dir = replaceFirstOccurrenceW(wuser_dir, wother_file_separator(), wfile_separator());
        user_dir = utils::convertUtf16ToUtf8(wuser_dir);

        if (!CreateDirectoryW(wuser_dir.c_str(), NULL))
        {
            if (GetLastError() == ERROR_PATH_NOT_FOUND)
            {
                std::wstring error_message = L"error creating directory \"" + wuser_dir + L"\"";
                MessageBox(GetActiveWindow(), error_message.c_str(), L"Error", MB_OK);

                exit(EXIT_FAILURE);
            }
        }

        archive_dir = utils::convertUtf8ToUtf16(user_dir + file_separator() + "java");
        path_to_java_paths = user_dir + file_separator() + properties["eq.java_paths.filename"];

#if INTPTR_MAX == INT64_MAX
        archive_dir += L"64";
        path_to_java_paths += "64";
#endif

        if (!CreateDirectoryW(archive_dir.c_str(), NULL))
        {
            if (GetLastError() == ERROR_PATH_NOT_FOUND)
            {
                std::wstring error_message = L"error creating directory \"" + archive_dir + L"\"";
                MessageBox(GetActiveWindow(), error_message.c_str(), L"Error", MB_OK);

                exit(EXIT_FAILURE);
            }
        };
        archive_path = archive_dir + wfile_separator() + archive_name;

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
                    throw UsageError(option + " requires an argument.");

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
            else if (!startsWith(option, "eq."))
            {
                mainArguments.push_back(option);
            }
        }

        if (it == end)
            throw UsageError("No class specified.");

        className = *it++;
        while (it != end)
            mainArguments.push_back(*it++);
    }

    SharedLibraryHandle openJvmLibrary() const
    {
        return ::openJvmLibrary(isClient, isServer);
    }

    NativeArguments getJvmArguments() const
    {
        return jvmArguments;
    }

    std::string getMainClassName() const
    {
        return className;
    }

    NativeArguments getMainArguments() const
    {
        return mainArguments;
    }
};

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
        jvm = reinterpret_cast<CreateJavaVM>(reinterpret_cast<uintptr_t>(GetProcAddress(reinterpret_cast<HMODULE>(sl_handle), "JNI_CreateJavaVM")));
        if (jvm == 0)
        {
            std::ostringstream os;

            os << "dlsym(" << sl_handle << ", JNI_CreateJavaVM) failed with " << GetLastError() << ".\n";
            if (sl_handle == 0)
                os << "A Java Runtime Environment (JRE) or Java Development Kit (JDK) must be available in order to run Red Expert. "
                   << "No Java virtual machine was found.\n"
                   << "If the required Java Runtime Environment is not installed, you can download it from from the website:\n"
                   << "http://java.com/\n"
                   << "then try again.";

            throw std::runtime_error(os.str());
        }

        return jvm;
    }

    jclass findClass(const std::string &c_name)
    {
        std::string can_name(c_name);
        for (std::string::iterator it = can_name.begin(), end = can_name.end(); it != end; ++it)
            if (*it == '.')
                *it = '/';

        jclass j_class = env->FindClass(can_name.c_str());
        if (j_class == 0)
        {
            std::ostringstream os;
            reportAnyJavaException(os);
            os << "FindClass(\"" << can_name << "\") failed ";
            os << "(FindClass loads and initializes the class as well as finding it).";

            throw std::runtime_error(os.str());
        }

        return j_class;
    }

    jmethodID findMainMethod(jclass cl_main)
    {
        jmethodID method = env->GetStaticMethodID(cl_main, "main", "([Ljava/lang/String;)V");
        if (method == 0)
            throw std::runtime_error("GetStaticMethodID(\"main\") failed.");

        return method;
    }

    jstring makeJavaString(const wchar_t *n_string)
    {
        jstring j_string = env->NewString((const jchar *)n_string, wcslen(n_string));
        return j_string;
    }

    jobjectArray convertArguments(const NativeArguments &n_args)
    {
        jclass str_cl = findClass("java/lang/String");
        jstring def_args = makeJavaString(L"");
        jobjectArray j_args = env->NewObjectArray(n_args.size(), str_cl, def_args);

        if (j_args == 0)
        {
            std::ostringstream os;
            os << "NewObjectArray(" << n_args.size() << ") failed.";

            throw std::runtime_error(os.str());
        }

        for (size_t index = 0; index != n_args.size(); ++index)
        {
            std::string n_arg = n_args[index];
            jstring j_arg = makeJavaString(utils::convertUtf8ToUtf16(n_arg).c_str());

            env->SetObjectArrayElement(j_args, index, j_arg);
        }

        return j_args;
    }

public:
    explicit JavaInvocation(LauncherArgumentParser &l_argsA) : l_args(l_argsA)
    {
        typedef std::vector<JavaVMOption> JavaVMOptions; // Required to be contiguous.
        const NativeArguments &jvm_args = l_args.getJvmArguments();

        JavaVMOptions j_vm_opts;
        for (size_t i = 0; i != jvm_args.size(); ++i)
            j_vm_opts.push_back(makeJvmOption(jvm_args[i].c_str()));
        j_vm_opts.push_back(makeJvmOption("abort", &abortJvm));

        JavaVMInitArgs j_vm_init_args;
        j_vm_init_args.version = JNI_VERSION_1_8;
        j_vm_init_args.options = &j_vm_opts[0];
        j_vm_init_args.nOptions = j_vm_opts.size();
        j_vm_init_args.ignoreUnrecognized = false;

        CreateJavaVM cr_java_vm = findCreateJavaVM();
        int result = cr_java_vm(&vm, reinterpret_cast<void **>(&env), &j_vm_init_args);
        if (result < 0)
        {
            std::ostringstream os;

            os << "JNI_CreateJavaVM(options=[";
            for (size_t i = 0; i < j_vm_opts.size(); ++i)
                os << (i > 0 ? ", " : "") << '"' << j_vm_opts[i].optionString << '"';
            os << "]) failed with " << JniError(result) << ".";

            throw std::runtime_error(os.str());
        }
    }

    void reportAnyJavaException(std::ostream &os)
    {
        jthrowable j_exc = env->ExceptionOccurred();
        if (j_exc == 0)
            return;

        env->ExceptionDescribe();
        os << "A Java exception occurred." << std::endl;

        jclass str_utl = env->FindClass("org/apache/commons/lang/exception/ExceptionUtils");
        if (str_utl == 0)
        {
            os << "FindClass(\"org.apache.commons.lang.exception.ExceptionUtils\") failed." << std::endl;
            return;
        }

        jmethodID throw_stack = env->GetStaticMethodID(str_utl, "getStackTrace", "(Ljava/lang/Throwable;)Ljava/lang/String;");
        if (throw_stack == 0)
        {
            os << "GetStaticMethodID(org.apache.commons.lang.exception.ExceptionUtils, \"getStackTrace(Throwable)\") failed." << std::endl;
            return;
        }

        jstring report = static_cast<jstring>(env->CallStaticObjectMethod(str_utl, throw_stack, j_exc));
        os << JniString(env, report);
    }

    ~JavaInvocation()
    {
        // If you attempt to destroy the VM with a pending JNI exception,
        // the VM crashes with an "internal error" and good luck to you finding
        // any reference to it on the web.
        if (env->ExceptionCheck())
            env->ExceptionDescribe();

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
        jobjectArray j_args = convertArguments(l_args.getMainArguments());

        env->CallStaticVoidMethod(j_class, j_method, j_args);
        if (env->ExceptionCheck() == false)
            return 0;

        std::ostringstream os;
        reportAnyJavaException(os);
        os << l_args.getMainClassName() << ".main(...) failed.";

        throw std::runtime_error(os.str());
    }
};

HRESULT DownloadStatus::OnProgress(ULONG ulProgress, ULONG ulProgressMax, ULONG ulStatusCode, LPCWSTR wszStatusText)
{
    progress = ulProgress;
    filesize = ulProgressMax;

    if (AbortDownload)
        return E_ABORT;

    return S_OK;
}
static DownloadStatus ds;

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
    JvmRegistryKey(
        HKEY &hive,
        const std::string &reg_root,
        const std::string &a_ver,
        const std::string &a_jre_path)
        : m_hive(hive),
          ver(a_ver),
          reg_path(reg_root + "\\" + ver),
          jre_path(a_jre_path)
    {
    }

    std::string readJreBin() const
    {
        std::string java_home = readRegistryFile(m_hive, reg_path);
        std::string jre_bin = java_home + jre_path + file_separator() + "bin";

        return jre_bin;
    }

    bool operator<(const self &rhs) const
    {
        return ver < rhs.ver;
    }

    void dumpTo(std::ostream &os) const
    {
        os << reg_path;
    }

    friend std::ostream &operator<<(std::ostream &os, const self &rhs)
    {
        rhs.dumpTo(os);
        return os;
    }
};
typedef std::vector<JvmRegistryKey> JvmRegistryKeys;

struct JavaVendorRegistryLocation
{
    const char *java_vendor;
    const char *jdk_name;
    const char *jre_name;

    JavaVendorRegistryLocation(
        const char *a_java_vendor,
        const char *a_jdk_name,
        const char *a_jre_name)
        : java_vendor(a_java_vendor),
          jdk_name(a_jdk_name),
          jre_name(a_jre_name)
    {
    }
};

std::string ErrorReporter::getUsage() const
{
    std::ostringstream os;
    os << "Usage: " << ARGV0 << " [options] class [args...]" << std::endl;
    os << "where options are:" << std::endl;
    os << "  -client - use client VM" << std::endl;
    os << "  -server - use server VM" << std::endl;
    os << "  -cp <path> | -classpath <path> - set the class search path" << std::endl;
    os << "  (As the JVM is a native Windows program, the class path must use Windows directory names separated by semicolons.)" << std::endl;
    os << "  -D<name>=<value> - set a system property" << std::endl;
    os << "  -verbose[:class|gc|jni] - enable verbose output" << std::endl;
    os << "or any option implemented internally by the chosen JVM." << std::endl;
    os << std::endl;

    return os.str();
}

void ErrorReporter::generateReport(const std::exception &ex, const std::string &usage) const
{
    std::ostringstream os;
    os << "If you don't have Java installed, download it from http://java.com/, then try again." << std::endl;
    os << "Error: " << ex.what() << std::endl;
    os << "JVM selection:" << std::endl;
    os << progress_os.str() << std::endl;
    os << usage << std::endl;
    os << "Command line was: " << ARGV0 << " " << utils::join(" ", launcher_args) << std::endl;

    reportFatalErrorViaGui("Red Expert", os.str(), support_address, typeError, locale);
}

int executeCmdEx(const char *cmd, std::string &result)
{
    char buffer[128];
    int ret_code = -1; // -1 if error ocurs.
    std::string command(cmd);
    command.append(" 2>&1"); // also redirect stderr to stdout

    result = "";
    FILE *pipe;

    pipe = _popen(command.c_str(), "r");

    if (pipe != NULL)
    {
        try
        {
            while (!feof(pipe))
                if (fgets(buffer, 128, pipe) != NULL)
                    result += buffer;
        }
        catch (...)
        {
            ret_code = _pclose(pipe);
            throw;
        }

        ret_code = _pclose(pipe);
    }

    return ret_code;
}

SharedLibraryHandle openSharedLibrary(const std::string &sl_file, std::string path, bool from_file_java_paths)
{
    std::ostringstream os;

    std::string out;
    std::string sl_f = sl_file;

    std::string java_exe = "java" + extension_exe_file();
    std::string path_to_java = path + file_separator() + java_exe;
    std::string cmd = "\"" + path_to_java + "\"" + " -XshowSettings:properties -version";

    executeCmdEx(cmd.c_str(), out);
    if (out.find("Property settings") == std::string::npos)
    {
        std::string err = "Error 02: File " + sl_f + " not_found";
        throw err;
    }

    std::string str = get_property_from_regex("os\\.arch", out);
    if (str != arch)
    {
        std::string err = "Error 01: File " + sl_f + " not support arch this application! this application need in java with arch: " + arch;
        throw err;
    }

    str = get_property_from_regex("java\\.version", out);
    if (!str.empty() && isUnreasonableVersion(err_rep.progress_os, str))
    {
        std::string err = "Error 03: File " + sl_f + " not support version this application! this application need in java with version >= 1.8";
        err_rep.typeError = NOT_SUPPORTED_VERSION;

        throw err;
    }

    SetEnvironmentVariableA("PATH", path.c_str());

    void *sl_handle = LoadLibraryA(sl_file.c_str());
    if (sl_handle == 0)
    {
        DWORD l_err = GetLastError();

        os << std::endl;
        os << "If you can't otherwise explain why this call failed, ";
        os << "consider whether all of the shared libraries ";
        os << "used by this shared library can be found." << std::endl;
        os << "This command's output may help:" << std::endl;
        os << "objdump -p \"" << sl_file << "\" | grep DLL" << std::endl;
        os << "LoadLibrary(\"" << sl_file << "\")";

        throw utils::WindowsError(os.str(), l_err);
    }

    if (!from_file_java_paths)
    {
        std::ofstream file_java_paths;                    // поток для записи
        file_java_paths.open(path_to_java_paths.c_str()); // окрываем файл для записи

        if (file_java_paths.is_open())
        {
            file_java_paths << "jvm=" << sl_file << std::endl;
            file_java_paths << "path=" << path << std::endl;
        }
    }

    return sl_handle;
}

static std::string readRegistryFile(const HKEY hive, const std::string &path)
{
    winreg::RegKey key;
    key.Open(hive, std::wstring(path.begin(), path.end()));

    std::wstring w_version = key.GetStringValue(L"JavaHome");
    std::string contents = std::string(w_version.begin(), w_version.end());

    // MSDN's article on RegQueryValueEx explains that the value may or may not include the null terminator.
    if (contents.empty() == false && contents[contents.size() - 1] == '\0')
        return contents.substr(0, contents.size() - 1);

    return contents;
}

void findVersionsInRegistry(
    std::ostream &os,
    HKEY &hive,
    JvmRegistryKeys &jvm_reg_keys,
    const std::string &reg_ath,
    const char *jre_path)
{
    std::string s_hive = utils::toString(hive);

    os << "Looking for registered JVMs under \"" << s_hive << reg_ath << "\" [" << std::endl;
    try
    {
        winreg::RegKey key;
        key.Open(hive, std::wstring(reg_ath.begin(), reg_ath.end()));

        std::wstring w_version = key.GetStringValue(L"CurrentVersion");
        std::string version = std::string(w_version.begin(), w_version.end());

        if (isUnreasonableVersion(os, version))
            return;

        JvmRegistryKey jvmRegistryKey(hive, reg_ath, version, jre_path);
        jvm_reg_keys.push_back(jvmRegistryKey);
    }
    catch (const std::exception &ex)
    {
        os << ex.what() << std::endl;
    }

    os << "]" << std::endl;
}

SharedLibraryHandle checkParameters(bool from_file)
{
    std::ostream &os = err_rep.progress_os;

    if (!djvm.empty() && !dpath.empty())
    {
        os << "Trying potential path \"" << djvm << "\" [" << std::endl;
        try
        {
            return openSharedLibrary(djvm, dpath, from_file);
        }
        catch (std::string &ex)
        {
            if (ex.find("Error 01") == 0)
                err_rep.typeError = NOT_SUPPORTED_ARCH;

            os << ex << std::endl;
        }
        catch (const std::exception &ex)
        {
            os << ex.what() << std::endl;
        }

        os << "]" << std::endl;
    }

    if (!djava_home.empty())
    {
        std::vector<std::string> paths = get_potential_libjvm_paths_from_path(djava_home);
        for (std::vector<std::string>::const_iterator it = paths.begin(), en = paths.end(); it != en; ++it)
        {
            std::string p_jvm = *it;
            os << "Trying potential path \"" << p_jvm << "\" [" << std::endl;

            try
            {
                return openSharedLibrary(p_jvm, false);
            }
            catch (std::string &ex)
            {
                if (ex.find("Error 01") == 0)
                    err_rep.typeError = NOT_SUPPORTED_ARCH;

                os << ex << std::endl;
            }
            catch (const std::exception &ex)
            {
                os << ex.what() << std::endl;
            }

            os << "]" << std::endl;
        }
    }

    return 0;
}

SharedLibraryHandle tryPaths()
{
    std::ostream &os = err_rep.progress_os;

    SharedLibraryHandle handle = checkParameters(false);
    if (handle != 0)
        return handle;

    std::ifstream in(path_to_java_paths);
    bool f_open = in.is_open();
    if (f_open)
    {
        std::string line;
        while (getline(in, line))
        {
            size_t offset = line.find('=');
            if (startsWith(line, "java_home"))
                djava_home = line.substr(offset + 1);

            if (startsWith(line, "jvm"))
                djvm = line.substr(offset + 1);

            if (startsWith(line, "path"))
                dpath = line.substr(offset + 1);
        }
    }
    in.close();

    handle = checkParameters(true);
    if (handle != 0)
        return handle;

    std::vector<std::string> p_paths = get_potential_libjvm_paths();
    for (std::vector<std::string>::const_iterator it = p_paths.begin(), en = p_paths.end(); it != en; ++it)
    {
        std::string p_jvm = *it;

        os << "Trying potential path \"" << p_jvm << "\" [" << std::endl;
        try
        {
            return openSharedLibrary(p_jvm, false);
        }
        catch (std::string &ex)
        {
            if (ex.find("Error 01") == 0)
                err_rep.typeError = NOT_SUPPORTED_ARCH;

            os << ex << std::endl;
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what() << std::endl;
        }

        os << "]" << std::endl;
    }

    return 0;
}

SharedLibraryHandle tryVersions(
    const char *jvm_dir,
    HKEY hive,
    const char *java_vendor,
    const char *jdk_name,
    const char *jre_name)
{
    JvmRegistryKeys jvm_reg_keys;
    std::ostream &os = err_rep.progress_os;

    const std::string reg_prefix = utils::toString("SOFTWARE\\") + java_vendor + "\\";
    const std::string jre_reg_path = reg_prefix + jre_name;
    const std::string jdk_reg_path = reg_prefix + jdk_name;

    findVersionsInRegistry(os, hive, jvm_reg_keys, jre_reg_path, "");
    findVersionsInRegistry(os, hive, jvm_reg_keys, jdk_reg_path, "");
    findVersionsInRegistry(os, hive, jvm_reg_keys, jdk_reg_path, "\\jre");

    std::sort(jvm_reg_keys.begin(), jvm_reg_keys.end());
    while (jvm_reg_keys.empty() == false)
    {
        JvmRegistryKey jvm_reg_key = jvm_reg_keys.back();
        jvm_reg_keys.pop_back();

        os << "Trying \"" << jvm_reg_key << "\" [" << std::endl;
        try
        {
            std::string jre_bin = jvm_reg_key.readJreBin();
            std::string jvm_location = jre_bin + "\\" + jvm_dir + "\\jvm.dll";

            return openSharedLibrary(jvm_location, false);
        }
        catch (std::string &ex)
        {
            if (ex.find("Error 01") == 0)
                err_rep.typeError = NOT_SUPPORTED_ARCH;

            os << ex << std::endl;
        }
        catch (const std::exception &ex)
        {
            os << ex.what() << std::endl;
        }

        os << "]" << std::endl;
    }

    std::ostringstream err_os;
    err_os << "tryVersions(\"" << jvm_dir << "\", ";
    err_os << hive << ", ";
    err_os << java_vendor << ", ";
    err_os << jdk_name << ", ";
    err_os << jre_name << ") failed";

    throw std::runtime_error(err_os.str());
}

SharedLibraryHandle tryHives(
    const char *jvm_dir,
    const char *java_vendor,
    const char *jdk_name,
    const char *jre_name)
{
    typedef std::deque<HKEY> Hives;

    Hives hives;
    hives.push_back(HKEY_CURRENT_USER);
    hives.push_back(HKEY_LOCAL_MACHINE);

    for (Hives::const_iterator it = hives.begin(), en = hives.end(); it != en; ++it)
    {
        HKEY hive = *it;
        try
        {
            return tryVersions(jvm_dir, hive, java_vendor, jdk_name, jre_name);
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what() << std::endl;
        }
    }

    std::ostringstream os;
    os << "tryHives(\"" << jvm_dir << "\", ";
    os << java_vendor << ", ";
    os << jdk_name << ", ";
    os << jre_name << ") failed";

    throw std::runtime_error(os.str());
}

std::vector<std::string> get_search_suffixes()
{
    std::vector<std::string> search_suffixes;

    search_suffixes.push_back("");
    search_suffixes.push_back("" + file_separator() + "jre" + file_separator() + "bin" + file_separator() + "server");
    search_suffixes.push_back("" + file_separator() + "jre" + file_separator() + "bin" + file_separator() + "client");
    search_suffixes.push_back("" + file_separator() + "bin" + file_separator() + "server");
    search_suffixes.push_back("" + file_separator() + "bin" + file_separator() + "client");

    return search_suffixes;
}

std::vector<std::string> get_potential_libjvm_paths_from_path(std::string path_parameter)
{
    std::vector<std::string> libjvm_potential_paths;
    std::vector<std::string> search_suffixes = get_search_suffixes();

    std::string out;
    std::string path = path_parameter;
    std::string file_name = "jvm.dll";

    std::string cmd = path_parameter + " -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);

    cmd = path_parameter + file_separator() + "java -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);

    for (std::vector<std::string>::iterator it_s = search_suffixes.begin(), en_s = search_suffixes.end(); it_s != en_s; ++it_s)
    {
        std::string suffix = *it_s;
        std::string res_path = path + suffix + file_separator() + file_name;
        libjvm_potential_paths.push_back(res_path);
    }

    return libjvm_potential_paths;
}

std::vector<std::string> get_potential_libjvm_paths()
{
    std::vector<std::string> libjvm_potential_paths;

    std::string file_name = "jvm.dll";
    std::vector<std::string> search_prefixes = {""};
    std::vector<std::string> search_suffixes = {
        file_separator() + "jre" + file_separator() + "bin" + file_separator() + "server",
        file_separator() + "bin" + file_separator() + "server",
        file_separator() + "bin" + file_separator() + "client"};

    // From direct environment variable
    char *env_value = NULL;
    if ((env_value = getenv("JAVA_HOME")) != NULL)
    {
        search_prefixes.insert(search_prefixes.begin(), env_value);
    }
    else
    {
        std::string out;
        executeCmdEx("java -XshowSettings:properties -version", out);

        std::ostream &os = err_rep.progress_os;
        os << out;

        std::string str = get_property_from_regex("java\\.home", out);
        if (!str.empty())
        {
            os << "java.home = " << str;

            std::string str86 = replaceFirstOccurrence(str, "Program Files\\", "Program Files (x86)\\");
            std::string str64 = replaceFirstOccurrence(str, "Program Files (x86)\\", "Program Files\\");

            search_prefixes.insert(search_prefixes.begin(), _strdup(str86.c_str()));
            search_prefixes.insert(search_prefixes.begin(), _strdup(str64.c_str()));
            search_prefixes.insert(search_prefixes.begin(), _strdup(str.c_str()));
        }

        HANDLE dir;
        WIN32_FIND_DATA file_data;

        if ((dir = FindFirstFile(L"C:\\Program Files\\Java\\*", &file_data)) != INVALID_HANDLE_VALUE)
        {
            do
            {
                char ch[260];
                char DefChar = ' ';
                WideCharToMultiByte(CP_ACP, 0, file_data.cFileName, -1, ch, 260, &DefChar, NULL);

                std::string fileName(ch);
                std::string full_file_name = "C:\\Program Files\\Java\\" + fileName;

                if (fileName[0] == '.')
                    continue;

                search_prefixes.push_back(full_file_name);

            } while (FindNextFile(dir, &file_data));
        }
        FindClose(dir);

        if ((dir = FindFirstFile(L"C:\\Program Files (x86)\\Java\\*", &file_data)) != INVALID_HANDLE_VALUE)
        {
            do
            {
                char ch[260];
                char DefChar = ' ';
                WideCharToMultiByte(CP_ACP, 0, file_data.cFileName, -1, ch, 260, &DefChar, NULL);

                std::string fileName(ch);
                std::string full_file_name = "C:\\Program Files (x86)\\Java\\" + fileName;

                if (fileName[0] == '.')
                    continue;

                search_prefixes.push_back(full_file_name);

            } while (FindNextFile(dir, &file_data));
        }
        FindClose(dir);

        if (djava_home != "")
            search_prefixes.insert(search_prefixes.begin(), djava_home);
    }

    for (std::vector<std::string>::const_iterator it = search_prefixes.begin(), en = search_prefixes.end(); it != en; ++it)
    {
        for (std::vector<std::string>::iterator it_s = search_suffixes.begin(), en_s = search_suffixes.end(); it_s != en_s; ++it_s)
        {
            std::string prefix = *it;
            std::string suffix = *it_s;
            std::string path = prefix + file_separator() + suffix + file_separator() + file_name;

            libjvm_potential_paths.push_back(path.c_str());
        }
    }

    return libjvm_potential_paths;
}

SharedLibraryHandle tryVendors(const char *jvm_dir)
{
    typedef std::deque<JavaVendorRegistryLocation> JavaVendorRegistryLocations;

    JavaVendorRegistryLocations vendor_reg_locations;
    vendor_reg_locations.push_back(JavaVendorRegistryLocation("JavaSoft", "Java Development Kit", "Java Runtime Environment"));
    vendor_reg_locations.push_back(JavaVendorRegistryLocation("JavaSoft", "JDK", "Java Runtime Environment"));
    vendor_reg_locations.push_back(JavaVendorRegistryLocation("IBM", "Java Development Kit", "Java2 Runtime Environment"));

    for (JavaVendorRegistryLocations::const_iterator it = vendor_reg_locations.begin(); it != vendor_reg_locations.end(); ++it)
    {
        try
        {
            return tryHives(jvm_dir, it->java_vendor, it->jdk_name, it->jre_name);
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what() << std::endl;
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
        jvmDirectories.push_back("client");

    if (isServer)
        jvmDirectories.push_back("server");

    for (JvmDirectories::const_iterator it = jvmDirectories.begin(), en = jvmDirectories.end(); it != en; ++it)
    {
        const char *jvmDirectory = *it;
        try
        {
            return tryVendors(jvmDirectory);
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what() << std::endl;
        }
    }

    std::ostringstream os;
    os << "tryDirectories(" << isClient << ", " << isServer << ") failed";

    return 0;
}

SharedLibraryHandle openJvmLibrary(bool isClient, bool isServer)
{
    std::ostream &os = err_rep.progress_os;
    os << "Trying to find " << sizeof(void *) * 8 << " bit jvm.dll - we need a 1.8 or newer JRE or JDK." << std::endl;
    os << "Error messages were:" << std::endl;

    SharedLibraryHandle handle = tryPaths();
    if (handle != 0)
        return handle;

    handle = tryDirectories(isClient, isServer);
    if (handle != 0)
        return handle;

    std::string log_file = user_dir + "\\launcher.log";
    printErrorToLogFile(log_file.c_str(), err_rep.progress_os.str());

    if (checkInputDialog() == 0)
        throw std::string("CANCEL");

    return checkParameters(false);
}

VOID CALLBACK TimerProc(
    HWND hwnd,
    UINT uMsg,
    UINT_PTR idEvent,
    DWORD dwTime)
{
    if (idEvent == 1001)
    {
        long filesize = ds.GetFileSize();
        long progress = ds.GetProgress();

        if (filesize != 0)
        {
            float procent = ((float)progress) / ((float)filesize);
            procent *= 100;
            int pb_pos = (int)procent;

            SendMessage(h_progress, PBM_SETPOS, pb_pos, 0);
            if (progress == filesize)
            {
                EndDialog(h_dialog_download, 0);
                KillTimer(h_dialog_download, idEvent);
            }
        }
    }
}

INT_PTR CALLBACK DlgDownloadProc(HWND hw, UINT msg, WPARAM wp, LPARAM lp)
{
    switch (msg)
    {

    case WM_INITDIALOG:
    {
        h_dialog_download = hw;
        h_progress = CreateWindowEx(
            0,
            PROGRESS_CLASS,
            L"ProgressBar",                    // тип окна
            WS_CHILD | WS_BORDER | WS_VISIBLE, // стиль окна
            10, 40,                            // расположение
            270, 20,
            hw, // родительское окно
            NULL,
            GetModuleHandle(NULL),
            NULL);

        SendMessage(h_progress, PBM_SETRANGE, 0, (LPARAM)MAKELONG(0, 100));
        SendMessage(h_progress, PBM_SETSTEP, (WPARAM)1, 0);
        ds.OnStartBinding();

        showError = 0;
        th = std::thread([]
                         {
                            HRESULT res = URLDownloadToFile(0, get_download_url(), archive_path.c_str(), 0, &ds);
                            EndDialog(h_dialog_download, 0);
                            KillTimer(h_dialog_download, 1001);
                            if (res != S_OK && res != E_ABORT)
                            {
                                showError = 1;
                                error_code = res;
                            } });
        SetTimer(hw, 1001, 1000, TimerProc);

        return TRUE;
    }

    case WM_COMMAND:
        if (LOWORD(wp) == 3)
        {
            ds.AbortDownl();
            EndDialog(hw, 0);
        }
    }

    return FALSE;
}

INT_PTR CALLBACK DlgProc(HWND hw, UINT msg, WPARAM wp, LPARAM lp)
{
    std::string m_mes;
    std::string arch;

#if INTPTR_MAX == INT32_MAX
    arch = "x86";
#elif INTPTR_MAX == INT64_MAX
    arch = "amd64";
#endif

    switch (msg)
    {
    case WM_INITDIALOG: // сообщение о создании диалога

        SendMessage(GetDlgItem(hw, CHOOSE_FILE), BM_SETCHECK, BST_CHECKED, 0);
        if (locale.find("ru") != -1)
        {
            if (err_rep.typeError == NOT_SUPPORTED_ARCH)
                m_mes.append("Найдена Java с недопустимой архитектурой. ");
            else if (err_rep.typeError == NOT_SUPPORTED_ARCH)
                m_mes.append("Найдена Java с недопустимой версией. ");
            else
                m_mes.append("Java не найдена. ");

            m_mes.append("Вы можете вручную указать путь к JVM\n");
            m_mes.append("или скачать Java автоматически с ");
            m_mes.append("<A HREF=\"");
            m_mes.append(url_manual);
            m_mes.append("\">");
            m_mes.append(url_manual);
            m_mes.append("</A>");
            m_mes.append("\nПримечание: приложение работает с Java 1.8 или выше с архитектурой ");
            m_mes.append(arch);
        }
        else
        {
            if (err_rep.typeError == NOT_SUPPORTED_ARCH)
                m_mes.append("Java with invalid architecture found. ");
            else if (err_rep.typeError == NOT_SUPPORTED_ARCH)
                m_mes.append("Java with invalid version found. ");
            else
                m_mes.append("Java not found. ");

            m_mes.append("You can specify the path to JVM manually\n");
            m_mes.append("or download Java automatically or manually from ");
            m_mes.append("<A HREF=\"");
            m_mes.append(url_manual);
            m_mes.append("\">");
            m_mes.append(url_manual);
            m_mes.append("</A>");
            m_mes.append("\nNote that you need Java 1.8 or higher with ");
            m_mes.append(arch);
            m_mes.append(" architecture. ");
        }

        std::wstring wideString = L"";
        wideString.assign(m_mes.begin(), m_mes.end());
        SetDlgItemText(hw, 1, wideString.c_str());

        return TRUE;

    case WM_COMMAND: // сообщение от управляющих элементов
        if (LOWORD(wp) == 6)
        {
            if (IsDlgButtonChecked(hw, DOWNLOAD))
            {

                HINSTANCE h = GetModuleHandle(NULL);
                EnableWindow(hw, FALSE);

                if (locale.find("ru") != -1)
                    DialogBox(h, MAKEINTRESOURCEW(P_BAR_DIALOG_RU), NULL, DlgDownloadProc);
                else
                    DialogBox(h, MAKEINTRESOURCEW(P_BAR_DIALOG_EN), NULL, DlgDownloadProc);

                th.join();
                EnableWindow(hw, TRUE);
                DeleteUrlCacheEntry(get_download_url());

                if (showError == 1)
                {
                    LPTSTR errorText = NULL;

                    std::wstring mes = L"Error code: ";
                    mes.append(std::to_wstring(error_code));
                    if (locale.find("ru") != -1)
                    {
                        mes.append(L"\nПроверьте интернет соединение.");
                        MessageBox(hw, mes.c_str(), TEXT("Ошибка скачивания"), MB_OK);
                    }
                    else
                    {
                        mes.append(L"\nCheck internet connection.");
                        MessageBox(hw, mes.c_str(), TEXT("Error download"), MB_OK);
                    }

                    // release memory allocated by FormatMessage()
                    LocalFree(errorText);
                    errorText = NULL;

                    return TRUE;
                }

                if (ds.GetAbortDownl())
                    return TRUE;

                HZIP hz = OpenZip(archive_path.c_str(), 0);
                ZIPENTRY ze;
                SetUnzipBaseDir(hz, archive_dir.c_str());
                GetZipItem(hz, -1, &ze);
                int numitems = ze.index;

                // -1 gives overall information about the zipfile
                for (int zi = 0; zi < numitems; zi++)
                {
                    ZIPENTRY ze;
                    GetZipItem(hz, zi, &ze);    // fetch individual details
                    UnzipItem(hz, zi, ze.name); // e.g. the item's name.
                }

                CloseZip(hz);
                _wremove(archive_path.c_str());

                HANDLE dir;
                WIN32_FIND_DATA file_data;

                std::wstring str = archive_dir + wfile_separator() + L"*";
                if ((dir = FindFirstFile(str.c_str(), &file_data)) != INVALID_HANDLE_VALUE)
                {
                    do
                    {
                        std::string fileName = utils::convertUtf16ToUtf8(file_data.cFileName);
                        std::string full_file_name = utils::convertUtf16ToUtf8(archive_dir) + file_separator() + fileName;

                        if (fileName[0] == '.')
                            continue;

                        djava_home = full_file_name;

                    } while (FindNextFile(dir, &file_data));
                }

                FindClose(dir);
                result_dialog = DOWNLOAD;
                EndDialog(hw, 0);
            }

            if (IsDlgButtonChecked(hw, CHOOSE_FILE))
            {
                std::wstring ws = basicOpenFolder();
                if (ws != L"")
                {
                    std::wcout << L"ws=" << ws << std::endl;
                    djava_home = utils::convertUtf16ToUtf8(ws);
                    std::cout << "djava_home=" << djava_home << std::endl;
                    result_dialog = CHOOSE_FILE;
                    EndDialog(hw, 0);
                }
            }
        }

        if (LOWORD(wp) == 7)
        {
            result_dialog = CANCEL;
            EndDialog(hw, 0);
        }

        return TRUE;

    case WM_NOTIFY:

        if (LOWORD(wp) == 1)
        {
            switch (((LPNMHDR)lp)->code)
            {
            case NM_CLICK:
            case NM_RETURN:
            {
                PNMLINK pNMLink = (PNMLINK)lp;
                LITEM item = pNMLink->item;

                if (item.iLink == 0)
                    ShellExecute(NULL, L"open", item.szUrl, NULL, NULL, SW_SHOW);

                break;
            }
            }
        }
    }

    return FALSE;
}

int showDialog()
{
    int res;
    HINSTANCE h = GetModuleHandle(NULL);

    if (locale.find("ru") != -1)
        DialogBox(h, MAKEINTRESOURCEW(DIALOG_X_RU), NULL, DlgProc);
    else
        DialogBox(h, MAKEINTRESOURCEW(DIALOG_X_EN), NULL, DlgProc);

    if (result_dialog == DOWNLOAD || result_dialog == CHOOSE_FILE)
        return 1;

    return 0;
}

LONG CALLBACK handleVectoredException(PEXCEPTION_POINTERS)
{
    return EXCEPTION_CONTINUE_SEARCH;
}

static void deferToHotSpotExceptionHandler()
{
    ULONG first_handler = 1;
    PVOID handle = AddVectoredContinueHandler(first_handler, &handleVectoredException);

    if (handle == 0)
        throw std::runtime_error("AddVectoredContinueHandler failed");

    // The handle of the next handler seems to be at our handle.
    // The previous handle follows.
    // The rest of the structure remained opaque to me, though I didn't try
    // RtlDecodePointer.

    void *w_handle = *reinterpret_cast<void **>(handle);
    ULONG rc = RemoveVectoredContinueHandler(w_handle);
    rc = RemoveVectoredContinueHandler(handle);

    if (rc == 0)
        throw std::runtime_error("RemoveVectoredContinueHandler(handle) failed");
}

std::string get_property_from_regex(std::string reg_property, std::string source)
{
    std::regex regex(reg_property + "\\s\\=\\s(([^\\n])+)\\n");
    std::smatch match;

    if (std::regex_search(source, match, regex))
        return match[1].str();

    return "";
}

std::string file_separator()
{
    return "\\";
}

std::string other_file_separator()
{
    return "/";
}

std::wstring wfile_separator()
{
    return L"\\";
}

std::wstring wother_file_separator()
{
    return L"/";
}

std::string extension_exe_file()
{
    return ".exe";
}

std::string runnable_command()
{
    HMODULE h_module = GetModuleHandleW(NULL);
    WCHAR path[MAX_PATH];
    GetModuleFileNameW(h_module, path, MAX_PATH);
    std::wstring ws(path);
    std::string command = utils::convertUtf16ToUtf8(ws);

    return command;
}

std::wstring get_download_url()
{
    std::wstring download_url_w = L"";
    download_url_w.assign(download_url.begin(), download_url.end());

    return download_url_w;
}

int invokeExecuteQuery(const NativeArguments &l_args)
{
    LauncherArgumentParser parser(l_args);
    deferToHotSpotExceptionHandler();
    JavaInvocation j_invocation(parser);
    return j_invocation.invokeMain();
}

int runLauncher(int argc, char *argv[])
{
    // get current OS locale
    locale = std::locale("").name();
    std::transform(locale.begin(), locale.end(), locale.begin(), [](unsigned char c)
                   { return std::tolower(c); });

    std::string separator = ";";
    std::string paths("-Djava.class.path=");

    // hide console window
    ::ShowWindow(::GetConsoleWindow(), SW_HIDE);

    HMODULE h_module = GetModuleHandleW(NULL);
    WCHAR path[MAX_PATH];
    GetModuleFileNameW(h_module, path, MAX_PATH);

    std::wstring ws(path);
    std::string str = utils::convertUtf16ToUtf8(ws);
    app_exe_path = str;

    char buf[256];
    GetCurrentDirectoryA(256, buf);
    int app_pid = GetCurrentProcessId();

    bin_dir = app_exe_path.substr(0, app_exe_path.find_last_of(file_separator()));
    app_dir = bin_dir + file_separator() + "..";

    paths.append(app_dir + file_separator() + "RedExpert.jar");
    paths.append(separator);

    std::string lib_dir(app_dir + file_separator() + "lib");
    std::wstring wlib_dir(lib_dir.begin(), lib_dir.end());
    wlib_dir.append(L"\\*");

    WIN32_FIND_DATA data;
    HANDLE h_find = FindFirstFile(wlib_dir.c_str(), &data);
    if (h_find != INVALID_HANDLE_VALUE)
    {
        do
        {
            char buffer[1024];
            char def_char = '\0';
            WideCharToMultiByte(CP_ACP, 0, data.cFileName, -1, buffer, 260, &def_char, NULL);

            std::string conv_file(buffer);
            if (conv_file.rfind("fbplugin-impl", 0) == 0 || conv_file.rfind("fbclient", 0) == 0 || conv_file.rfind("jaybird", 0) == 0)
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

    int sep_idx = paths.find_last_of(separator);
    paths = paths.substr(0, sep_idx);

    // add to java class path Red Expert jar
    std::string stdString = paths;
    std::string fileForOpenPath;
    char *class_path = (char *)stdString.c_str();

    NativeArguments launcher_args;

    err_rep.ARGV0 = *argv++;
    if (*argv != 0)
        fileForOpenPath = *argv++;

    while (*argv != 0)
        launcher_args.push_back(*argv++);

    err_rep.launcher_args = launcher_args;
    launcher_args.push_back(class_path);
    launcher_args.push_back("org/executequery/ExecuteQuery");
    launcher_args.push_back("-exe_path=" + app_exe_path);
    launcher_args.push_back("-exe_pid=" + utils::toString(app_pid));

    std::string path_config;
    path_config = replaceFirstOccurrence(app_exe_path, "bin" + file_separator() + "RedExpert64" + extension_exe_file(), "config" + file_separator() + "redexpert_config.ini");
    path_config = replaceFirstOccurrence(path_config, "bin" + file_separator() + "RedExpert" + extension_exe_file(), "config" + file_separator() + "redexpert_config.ini");

    std::ifstream in(path_config); // открываем файл для чтения
    bool f_open = in.is_open();
    std::string line;
    if (f_open)
        while (getline(in, line))
            if (startsWith(line, "eq."))
                launcher_args.push_back(line);

    if (!fileForOpenPath.empty())
        launcher_args.push_back("FILE_FOR_OPEN=" + fileForOpenPath);
    in.close();

    return runJvm(launcher_args);
}