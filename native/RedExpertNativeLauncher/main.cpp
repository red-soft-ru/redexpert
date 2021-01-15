#ifdef __linux__
#include <dirent.h>
#include <dlfcn.h>
#include <libgen.h>
#include <unistd.h>
#include <string.h>
#include <system_error>
#include <sys/types.h>
#include <sys/stat.h>
#include <pwd.h>
#include <curl/curl.h>
#include <ctime>
#else
#pragma comment(lib, "user32.lib")
#pragma comment(lib, "Urlmon.lib")
#pragma comment(lib, "WinINet.lib")
#pragma comment(lib, "shell32.lib")
#include <windows.h>
#include <ShellAPI.h>
#include <regex>
#include <WinReg.hpp>
#include "HKEY.h"
#include <thread>
#include <commctrl.h>
#include <Wininet.h>
#include "resource.h"
#include "unzip.h"
#include <tchar.h>
#include <direct.h>

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
#include <thread>

static std::string djava_home;
static std::string djvm;
static std::string dpath;
static std::string path_to_java_paths;
static std::string user_dir;
static std::string app_exe_path;
static std::string bin_dir;
static std::string app_dir;
static int result_dialog;
typedef void* SharedLibraryHandle;
bool startsWith(const std::string& st, const std::string& prefix)
{
    return st.substr(0, prefix.size()) == prefix;
}

std::string file_separator()
{
#ifdef WIN32
    return "\\";
#else
    return "/";
#endif
}

std::string other_file_separator()
{
#ifdef WIN32
    return "/";
#else
    return "\\";
#endif
}

std::wstring wfile_separator()
{
#ifdef WIN32
    return L"\\";
#else
    return L"/";
#endif
}

std::wstring wother_file_separator()
{
#ifdef WIN32
    return L"/";
#else
    return L"\\";
#endif
}

std::string extension_exe_file()
{
#ifdef WIN32
    return ".exe";
#else
    return "";
#endif
}
#ifdef WIN32
#if INTPTR_MAX == INT64_MAX
static TCHAR* url_manual = TEXT("https://www.oracle.com/java/technologies/javase-downloads.html");
static TCHAR* download_url = TEXT("https://download.bell-sw.com/java/14.0.2+13/bellsoft-jre14.0.2+13-windows-amd64.zip");
#elif INTPTR_MAX == INT32_MAX
static TCHAR* url_manual = TEXT("https://www.java.com/ru/download/manual.jsp");
static TCHAR* download_url = TEXT("https://download.bell-sw.com/java/14.0.2+13/bellsoft-jre14.0.2+13-windows-i586.zip");
#else
#error "Environment not 32 or 64-bit."
#endif
static HWND h_progress;
static HWND h_dialog_download;
static std::wstring archive_name = L"java.zip";
static std::wstring archive_path;
static std::wstring archive_dir;
class DownloadStatus : public IBindStatusCallback {
private:
    int progress, filesize;
    int AbortDownload;

public:
    STDMETHOD(OnStartBinding)
    (
        /* [in] */ DWORD dwReserved,
        /* [in] */ IBinding __RPC_FAR* pib)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(GetPriority)
    (
        /* [out] */ LONG __RPC_FAR* pnPriority)
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
        /* [out] */ DWORD __RPC_FAR* grfBINDF,
        /* [unique][out][in] */ BINDINFO __RPC_FAR* pbindinfo)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(OnDataAvailable)
    (
        /* [in] */ DWORD grfBSCF,
        /* [in] */ DWORD dwSize,
        /* [in] */ FORMATETC __RPC_FAR* pformatetc,
        /* [in] */ STGMEDIUM __RPC_FAR* pstgmed)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(OnObjectAvailable)
    (
        /* [in] */ REFIID riid,
        /* [iid_is][in] */ IUnknown __RPC_FAR* punk)
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
        /* [iid_is][out] */ void __RPC_FAR* __RPC_FAR* ppvObject)
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

HRESULT DownloadStatus::OnProgress(ULONG ulProgress, ULONG ulProgressMax, ULONG ulStatusCode, LPCWSTR wszStatusText)
{
    progress = ulProgress;
    filesize = ulProgressMax;
    if (AbortDownload)
        return E_ABORT;
    return S_OK;
}

static DownloadStatus ds;
static std::thread th;
int showDialog();
std::string get_property_from_regex(std::string reg_property, std::string source)
{
    std::regex regex(reg_property + "\\s\\=\\s(([^\\n])+)\\n");
    std::smatch match;
    if (std::regex_search(source, match, regex)) {
        return match[1].str();
    }
    return "";
}
#else
#if INTPTR_MAX == INT64_MAX
static std::string url_manual = "https://www.oracle.com/java/technologies/javase-downloads.html";
static std::string download_url = "https://download.bell-sw.com/java/14+36/bellsoft-jre14+36-linux-amd64.tar.gz";
#elif INTPTR_MAX == INT32_MAX
static std::string url_manual = "https://www.java.com/ru/download/manual.jsp";
static std::string download_url = "https://download.bell-sw.com/java/14+36/bellsoft-jre14+36-linux-i586.tar.gz";
#endif
SharedLibraryHandle checkParameters(bool from_file);
int status_downl;
FILE* outfile;
static int ABORT_DOWNLOAD = -1;
static int DOWNLOADING = 0;
static int FINISHED_DOWNLOADING = 1;
static int ERROR_DOWNLOAD = -2;
static std::string archive_name = "java.tar.gz";
static std::string archive_path;
static std::string archive_dir;
GtkWidget* Bar;
GtkWidget* dialog_dwnl;
GtkLabel * upLabel;
GtkLabel * downLabel;
GThread* downl_thread;
CURL* curl;
CURL* (*curl_easy_init_)(void);
CURLcode (*curl_easy_setopt_)(CURL *, CURLoption , ...);
CURLcode (*curl_easy_perform_)(CURL *);
CURLcode (*curl_global_init_)(long flags);
void (*curl_easy_cleanup_)(CURL *);
void (*curl_easy_reset_)(CURL *);
const char *(*curl_easy_strerror_)(CURLcode);
void (*curl_global_cleanup_)(void);
SharedLibraryHandle curlHandle;
static double d = 0, t = 0;
std::string getUsabilitySize(long countByte) {
        int oneByte = 1024;
        int delimiter = 103;
        long drob = 0;
        if (countByte > 1024) {
            drob = (countByte % oneByte) / delimiter;
            countByte = countByte / oneByte;
            if (countByte > oneByte) {
                drob = (countByte % oneByte) / delimiter;
                countByte = countByte / oneByte;
                if (countByte > oneByte) {
                    drob = (countByte % oneByte) / delimiter;
                    countByte = countByte / oneByte;
                    if (countByte > oneByte) {
                        drob = (countByte % oneByte) / delimiter;
                        countByte = countByte / oneByte;
                        return std::to_string(countByte )+ "," + std::to_string(drob)+ "Tb";
                    } else return std::to_string(countByte )+ "," + std::to_string(drob)+ "Gb";
                } else return std::to_string(countByte )+ "," + std::to_string(drob)+ "Mb";
            } else return std::to_string(countByte )+ "," + std::to_string(drob) + "Kb";
        } else return std::to_string(countByte ) + "b";
    }
void download_java();
extern "C"  void
ok_button_clicked (GtkButton *button,
            gpointer   data)
{


    if (gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON(rb_download)))
    {
        download_java();
        if (status_downl==ABORT_DOWNLOAD||status_downl==ERROR_DOWNLOAD)
            return;
        dialog_result = DOWNLOAD;
    }
    if (gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON(rb_file)))
    {
        char* s = gtkOpenFile();
        if (s == 0)
            return;
        djava_home = s;
        dialog_result = CHOOSE_FILE;
    }
    gtk_widget_destroy(dialog);
    gtk_main_quit();

}
extern "C" void cancel_button_clicked_cb(GtkButton* button,
    gpointer data)
{
    status_downl = ABORT_DOWNLOAD;
    gtk_widget_destroy(dialog_dwnl);
    gtk_main_quit();
}

extern "C" void destroy_dialog(GtkDialog* d,
    gpointer data)
{
    if(status_downl==DOWNLOADING)
    {
    status_downl = ABORT_DOWNLOAD;
    }
}
size_t my_write_func(void* ptr, size_t size, size_t nmemb, FILE* stream)
{
    return fwrite(ptr, size, nmemb, stream);
}
size_t my_read_func(void* ptr, size_t size, size_t nmemb, FILE* stream)
{
    return fread(ptr, size, nmemb, stream);
}
int my_progress_func(GtkWidget* bar,
    double tt, /* dltotal */
    double dd, /* dlnow */
    double ultotal,
    double ulnow)
{

    t = tt;
    d = dd;
    if(status_downl==ABORT_DOWNLOAD||status_downl==ERROR_DOWNLOAD)
        return 1;
    return 0;
}
static std::string http_code;
static bool timeout_error;
static time_t last_time;
static double last_d;
static size_t header_callback(char *buffer, size_t size,
                          size_t nitems, void *userdata)
{
    std::string s=buffer;
    if (s.find("HTTP") != std::string::npos)
        http_code=s;
  /*
   * Эта функция будет вызываться на каждый возвращаемый заголовок.
   */
  return nitems * size;
}
static gboolean updateProgress(gpointer data)
{
    if (status_downl == DOWNLOADING) {
        time_t cur_time= time(NULL);
        if(last_time == 0)
        {
            last_time=cur_time;
        }
        else
        {
            if(d!=last_d)
            {
                last_time=cur_time;
            }
            else if(cur_time-last_time>10)
            {
                status_downl=ERROR_DOWNLOAD;
                timeout_error = true;
            }

        }
        last_d = d;
        if (t != 0) {
            std::string text=getUsabilitySize((long)d)+"/"+getUsabilitySize((long)t);
            gtk_label_set_text(GTK_LABEL(downLabel),text.c_str());
            gtk_progress_bar_set_fraction(GTK_PROGRESS_BAR(Bar), d / t);
        }
        if (d == t && d != 0) {
            status_downl = FINISHED_DOWNLOADING;
            gtk_widget_destroy(dialog_dwnl);
            gtk_main_quit();
            return FALSE;
        }
    } else if(status_downl==ERROR_DOWNLOAD)
    {
        gtk_widget_destroy(dialog_dwnl);
        gtk_main_quit();
        return FALSE;
    }
    return TRUE;
}
void init_curl()
{
    status_downl=DOWNLOADING;
    curlHandle=0;
    curlHandle = dlopen("libcurl.so.4",RTLD_LAZY);
    if(curlHandle==0)
    {
        gtkMessageBox("Error downloading java","libcurl not found. Please install libcurl4 to automatically download java");
        status_downl=ERROR_DOWNLOAD;
    }
    curl_easy_init_=(CURL* (*)(void))dlsym(curlHandle,"curl_easy_init");
    curl_easy_setopt_=(CURLcode (*)(CURL *, CURLoption , ...))dlsym(curlHandle,"curl_easy_setopt");
    curl_easy_perform_=(CURLcode (*)(CURL *))dlsym(curlHandle,"curl_easy_perform");
    curl_easy_cleanup_=(void (*)(CURL *)) dlsym(curlHandle,"curl_easy_cleanup");
    curl_global_init_=(CURLcode (*)(long flags))dlsym(curlHandle,"curl_global_init");
    curl_easy_reset_=(void (*)(CURL *))dlsym(curlHandle,"curl_easy_reset");
    curl_easy_strerror_=(const char *(*)(CURLcode))dlsym(curlHandle,"curl_easy_strerror");
    curl_global_cleanup_=(void (*)(void))dlsym(curlHandle,"curl_global_cleanup");
    (*curl_global_init_)(CURL_GLOBAL_ALL);
    curl=(*curl_easy_init_)();
    if (curl) {
        CURLcode res;
        const char* url = download_url.c_str();
        curl_easy_setopt_(curl, CURLOPT_URL, url);
        curl_easy_setopt_(curl, CURLOPT_HEADER, 1);
        curl_easy_setopt_(curl, CURLOPT_NOBODY, 1);
        curl_easy_setopt_(curl, CURLOPT_HEADERFUNCTION, header_callback);
        http_code="0";
        res = curl_easy_perform_(curl);
        if(res != CURLE_OK)
        {
              gtkMessageBox("Error downloading java" ,curl_easy_strerror_(res));
              status_downl=ERROR_DOWNLOAD;
        }
        else if(http_code.find("20")==std::string::npos&&http_code.find("30")==std::string::npos)
        {
            gtkMessageBox("Error downloading java" ,http_code.c_str());
            status_downl=ERROR_DOWNLOAD;
        }
        /* always cleanup */
        curl_easy_cleanup_(curl);
        if(status_downl==ERROR_DOWNLOAD)
            return;
        curl=(*curl_easy_init_)();
        if (curl) {
            const char* url = download_url.c_str();
            outfile = fopen(archive_path.c_str(), "wb");
            curl_easy_setopt_(curl, CURLOPT_URL, url);
            curl_easy_setopt_(curl, CURLOPT_WRITEDATA, outfile);
            curl_easy_setopt_(curl, CURLOPT_WRITEFUNCTION, my_write_func);
            curl_easy_setopt_(curl, CURLOPT_READFUNCTION, my_read_func);
            curl_easy_setopt_(curl, CURLOPT_NOPROGRESS, 0L);
            curl_easy_setopt_(curl, CURLOPT_PROGRESSFUNCTION, my_progress_func);
            timeout_error=false;
            last_time=0;
        }
    }
}
CURLcode download_in_thread()
{

    if (curl) {

        CURLcode res;
        status_downl=DOWNLOADING;
        res = curl_easy_perform_(curl);
        if(res != CURLE_OK)
        {
              std::cout<<curl_easy_strerror_(res)<<std::endl;
              if(status_downl!=ABORT_DOWNLOAD)
                status_downl=ERROR_DOWNLOAD;
        }
        fclose(outfile);

        curl_easy_cleanup_(curl);
        return res;
    }
}
int showDialog()
{
    gtkDialog(bin_dir + file_separator() + "../resources/dialog_java_not_found.glade", url_manual);
    if (dialog_result == CANCEL)
        exit(1);
    if (dialog_result == CHOOSE_FILE) {
        return 1;
    }
    if (dialog_result == DOWNLOAD) {

        /* Must initialize libcurl before any threads are started */
        return 1;

    }
    return 0;
}
void download_java()
{
    init_curl();
    if(status_downl==ERROR_DOWNLOAD)
        return;
    GError* error = NULL;
    builder = gtk_builder_new();
    std::string path_to_glade = bin_dir + file_separator() + "../resources/download_dialog.glade";
    if (!gtk_builder_add_from_file(builder, path_to_glade.c_str(), &error)) {
        g_warning("%s", error->message);
        g_error_free(error);
        return;
    }
    dialog_dwnl = GTK_WIDGET(gtk_builder_get_object(builder, "download_dialog"));
    gtk_builder_connect_signals(builder, NULL);
    Bar = GTK_WIDGET(gtk_builder_get_object(builder, "prog_bar"));
    upLabel=GTK_LABEL(gtk_builder_get_object(builder, "up_text"));
    downLabel=GTK_LABEL(gtk_builder_get_object(builder, "down_text"));
    gtk_progress_bar_set_fraction(GTK_PROGRESS_BAR(Bar), 0);
    gtk_label_set_text(GTK_LABEL(upLabel),"Please wait while java is downloading.");
    /* Init thread */
    //g_thread_init(NULL);
    //adj = (GtkAdjustment*)gtk_adjustment_new(0, 0, 100, 0, 0, 0);
    g_object_unref(G_OBJECT(builder));
    gtk_window_set_transient_for(GTK_WINDOW(dialog_dwnl),GTK_WINDOW(dialog));
    // Показываем форму и виджеты на ней
    gtk_widget_show(dialog_dwnl);
    static CURLcode res=CURLE_OK;
    std::thread th = std::thread([] { res=download_in_thread(); });
    g_timeout_add_seconds(1, updateProgress, NULL);
    gtk_main();
    th.join();
    if (status_downl == ERROR_DOWNLOAD)
    {
        std::stringstream stream;
        if(timeout_error)
            stream<<"Timeout was reached\nCheck internet connection";
        else
            stream<<curl_easy_strerror_(res)<<"\nCheck internet connection";
        gtkMessageBox("Error downloading java",stream.str().c_str());
    }
    if (status_downl == ABORT_DOWNLOAD||status_downl == ERROR_DOWNLOAD)
        return;
    std::string command = "tar -C " + archive_dir + " -xvf " + archive_path;
    system(command.c_str());
    command = "rm " + archive_path;
    system(command.c_str());
    DIR* dir;
    struct dirent* ent;
    if ((dir = opendir(archive_dir.c_str())) != NULL) {
        while ((ent = readdir(dir)) != NULL) {
            std::string dir_name = ent->d_name;
            if (dir_name == "." || dir_name == "..")
                continue;
            djava_home = archive_dir + file_separator() + dir_name;
        }
        closedir(dir);
    }
}
#endif

bool isUnreasonableVersion(std::ostream& os, const std::string& version)
{
    if (version.empty() || isdigit(version[0]) == false) {
        os << "\"";
        os << version;
        os << "\" is not a number";
        os << std::endl;
        return true;
    }
    if (version < "1.8") {
        os << version;
        os << " is too old";
        os << std::endl;
        return true;
    }
    return false;
}

std::vector<std::string> get_potential_libjvm_paths();
std::vector<std::string> get_potential_libjvm_paths_from_path(std::string path_parameter);

struct UsageError : std::runtime_error {
    explicit UsageError(const std::string& description)
        : std::runtime_error(description)
    {
    }
};

typedef std::deque<std::string> NativeArguments;

extern "C" {
#ifdef __linux__
typedef jint (*CreateJavaVM)(JavaVM**, void**, void*);
typedef jint (*CreateJvmFuncPtr)(JavaVM**, void**, JavaVMInitArgs*);
#else
typedef jint(_stdcall* CreateJavaVM)(JavaVM**, void**, void*);
typedef jint(_stdcall* CreateJvmFuncPtr)(JavaVM**, void**, JavaVMInitArgs*);
#endif
}

struct ErrorReporter {
public:
    std::string ARGV0;
    NativeArguments launcher_args;
    std::string support_address;
    std::ostringstream progress_os;
    int typeError;

private:
    std::string getUsage() const;
    void generateReport(const std::exception& ex, const std::string& usage) const;

public:
    ErrorReporter()
        : ARGV0("<unknown>")
    {
        typeError = 0;
    }

    void reportUsageError(const UsageError& ex) const
    {
        generateReport(ex, getUsage());
    }

    void reportFatalException(const std::exception& ex) const
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
    if (len != -1) {
        buff[len] = '\0';
        return std::string(buff);
    }
}
#endif

int executeCmdEx(const char* cmd, std::string& result)
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
    if (pos == std::string::npos)
        return s;
    return s.replace(pos, toReplace.length(), replaceWith);
}
std::wstring
replaceFirstOccurrenceW(
    std::wstring& s,
    const std::wstring& toReplace,
    const std::wstring& replaceWith)
{
    std::size_t pos = s.find(toReplace);
    if (pos == std::string::npos)
        return s;
    return s.replace(pos, toReplace.length(), replaceWith);
}


SharedLibraryHandle openSharedLibrary(const std::string& sl_file, std::string path, bool from_file_java_paths)
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
    std::string java_exe = "java" + extension_exe_file();
    std::string sl_f = sl_file;
    std::string path_to_java = path + file_separator() + java_exe;
    std::string cmd = "\"" + path_to_java + "\"" + " -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);
    if (out.find("Property settings") == std::string::npos) {
        std::string err = "Error 02: File " + sl_f + " not_found";
        throw err;
    }

    std::string str = get_property_from_regex("os\\.arch", out);
    //std::cout<<str<<"\n";
    bool support = str == arch;

    if (!support) {

        std::string err = "Error 01: File " + sl_f + " not support arch this application! this application need in java with arch: " + arch;
        throw err;
    }

    str = get_property_from_regex("java\\.version", out);
    if (!str.empty() && isUnreasonableVersion(err_rep.progress_os, str)) {
        std::string err = "Error 03: File " + sl_f + " not support version this application! this application need in java with version>=1.8";
        err_rep.typeError = NOT_SUPPORTED_VERSION;
        throw err;
    }
    SetEnvironmentVariableA("PATH", path.c_str());
    void* sl_handle = LoadLibraryA(sl_file.c_str());
    if (sl_handle == 0) {
        DWORD l_err = GetLastError();
#else
    void* sl_handle = dlopen(sl_file.c_str(), RTLD_LAZY);
    if (sl_handle == 0) {
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
    if (!from_file_java_paths) {
        std::ofstream file_java_paths; // поток для записи
        file_java_paths.open(path_to_java_paths.c_str()); // окрываем файл для записи
        if (file_java_paths.is_open()) {
            file_java_paths << "jvm=" << sl_file << std::endl;
            file_java_paths << "path=" << path << std::endl;
        }
    }
    return sl_handle;
}
SharedLibraryHandle
openSharedLibrary(const std::string& sl_file, bool from_file_java_paths)
{
    std::string server_jvm = "server" + file_separator() + "jvm.dll";
    std::string client_jvm = "client" + file_separator() + "jvm.dll";
    std::string java_exe = "java" + extension_exe_file();
    std::string sl_f = sl_file;
    std::string path_to_java = replaceFirstOccurrence(sl_f, server_jvm, "");
    path_to_java = replaceFirstOccurrence(path_to_java, client_jvm, "");
    return openSharedLibrary(sl_file, path_to_java, from_file_java_paths);
}

static std::string readFile(const std::string& path)
{
    std::ifstream is(path.c_str());
    if (is.good() == false) {
        throw std::runtime_error("Couldn't open \"" + path + "\".");
    }
    std::ostringstream contents;
    contents << is.rdbuf();
    return contents.str();
}

#ifdef _WIN32

static std::string readRegistryFile(const HKEY hive, const std::string& path)
{
    winreg::RegKey key;
    key.Open(hive, std::wstring(path.begin(), path.end()));
    std::wstring w_version = key.GetStringValue(L"JavaHome");
    std::string contents = std::string(w_version.begin(), w_version.end());
    // MSDN's article on RegQueryValueEx explains that the value may or may not
    // include the null terminator.
    if (contents.empty() == false && contents[contents.size() - 1] == '\0') {
        return contents.substr(0, contents.size() - 1);
    }
    return contents;
}

struct JvmRegistryKey {
public:
    typedef JvmRegistryKey self;

private:
    HKEY m_hive;
    std::string ver;
    std::string reg_path;
    std::string jre_path;

public:
    JvmRegistryKey(HKEY& hive, const std::string& reg_root, const std::string& a_ver,
        const std::string& a_jre_path)
        : m_hive(hive)
        , ver(a_ver)
        , reg_path(reg_root + "\\" + ver)
        , jre_path(a_jre_path)
    {
    }

    std::string readJreBin() const
    {
        std::string java_home = readRegistryFile(m_hive, reg_path);
        std::string jre_bin = java_home + jre_path + file_separator() + "bin";
        return jre_bin;
    }

    bool operator<(const self& rhs) const { return ver < rhs.ver; }

    void dumpTo(std::ostream& os) const { os << reg_path; }

    friend std::ostream& operator<<(std::ostream& os, const self& rhs)
    {
        rhs.dumpTo(os);
        return os;
    }
};

typedef std::vector<JvmRegistryKey> JvmRegistryKeys;

#endif

#ifdef _WIN32

void findVersionsInRegistry(std::ostream& os, HKEY& hive, JvmRegistryKeys& jvm_reg_keys,
    const std::string& reg_ath,
    const char* jre_path)
{
    std::string s_hive = utils::toString(hive);
    os << "Looking for registered JVMs under \"";
    os << s_hive;
    os << reg_ath;
    os << "\" [";
    os << std::endl;
    try {
        winreg::RegKey key;
        key.Open(hive, std::wstring(reg_ath.begin(), reg_ath.end()));
        std::wstring w_version = key.GetStringValue(L"CurrentVersion");
        std::string version = std::string(w_version.begin(), w_version.end());
        if (isUnreasonableVersion(os, version)) {
            return;
        }
        JvmRegistryKey jvmRegistryKey(hive, reg_ath, version,
            jre_path);
        jvm_reg_keys.push_back(jvmRegistryKey);
    }
    catch (const std::exception& ex) {
        os << ex.what();
        os << std::endl;
    }
    os << "]";
    os << std::endl;
}
SharedLibraryHandle checkParameters(bool from_file)
{
    std::ostream& os = err_rep.progress_os;
    if (!djvm.empty() && !dpath.empty()) {
        os << "Trying potential path \"";
        os << djvm;
        os << "\" [";
        os << std::endl;
        try {
            return openSharedLibrary(djvm, dpath, from_file);
        }
        catch (std::string& ex) {
            if (ex.find("Error 01") == 0) {
                err_rep.typeError = NOT_SUPPORTED_ARCH;
            }
            os << ex;
            os << std::endl;
        }
        catch (const std::exception& ex) {
            os << ex.what();
            os << std::endl;
        }
        os << "]";
        os << std::endl;
    }
    if (!djava_home.empty()) {

        std::vector<std::string> paths = get_potential_libjvm_paths_from_path(djava_home);
        for (std::vector<std::string>::const_iterator it = paths.begin(), en = paths.end(); it != en;
             ++it) {
            std::string p_jvm = *it;
            os << "Trying potential path \"";
            os << p_jvm;
            os << "\" [";
            os << std::endl;
            try {
                return openSharedLibrary(p_jvm, false);
            }
            catch (std::string& ex) {
                if (ex.find("Error 01") == 0) {
                    err_rep.typeError = NOT_SUPPORTED_ARCH;
                }
                os << ex;
                os << std::endl;
            }
            catch (const std::exception& ex) {
                os << ex.what();
                os << std::endl;
            }
            os << "]";
            os << std::endl;
        }
    }
    return 0;
}
SharedLibraryHandle tryPaths()
{
    std::ostream& os = err_rep.progress_os;
    SharedLibraryHandle handle = checkParameters(false);
    if (handle != 0)
        return handle;
    std::ifstream in(path_to_java_paths); // окрываем файл для чтения
    bool f_open = in.is_open();
    if (f_open) {
        std::string line;
        while (getline(in, line)) {
            size_t offset = line.find('=');
            if (startsWith(line, "java_home")) {
                djava_home = line.substr(offset + 1);
            }
            if (startsWith(line, "jvm")) {
                djvm = line.substr(offset + 1);
            }
            if (startsWith(line, "path")) {
                dpath = line.substr(offset + 1);
            }
        }
    }
    in.close();
    //std::cout<<djava_home<<";"<<djvm<<";"<<dpath<<std::endl;
    handle = checkParameters(true);
    if (handle != 0)
        return handle;

    std::vector<std::string> p_paths = get_potential_libjvm_paths();

    for (std::vector<std::string>::const_iterator it = p_paths.begin(), en = p_paths.end(); it != en;
         ++it) {
        std::string p_jvm = *it;
        os << "Trying potential path \"";
        os << p_jvm;
        os << "\" [";
        os << std::endl;
        try {
            return openSharedLibrary(p_jvm, false);
        }
        catch (std::string& ex) {
            if (ex.find("Error 01") == 0) {
                err_rep.typeError = NOT_SUPPORTED_ARCH;
            }
            os << ex;
            os << std::endl;
        }
        catch (const std::exception& ex) {
            std::ostream& os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
        os << "]";
        os << std::endl;
    }
    return 0;
}

SharedLibraryHandle tryVersions(const char* jvm_dir, HKEY hive,
    const char* java_vendor, const char* jdk_name,
    const char* jre_name)
{

    std::ostream& os = err_rep.progress_os;

    const std::string reg_prefix = utils::toString("SOFTWARE\\") + java_vendor + "\\";
    const std::string jre_reg_path = reg_prefix + jre_name;
    const std::string jdk_reg_path = reg_prefix + jdk_name;

    JvmRegistryKeys jvm_reg_keys;

    findVersionsInRegistry(os, hive, jvm_reg_keys, jre_reg_path, "");
    findVersionsInRegistry(os, hive, jvm_reg_keys, jdk_reg_path, "");
    findVersionsInRegistry(os, hive, jvm_reg_keys, jdk_reg_path, "\\jre");
    std::sort(jvm_reg_keys.begin(), jvm_reg_keys.end());
    while (jvm_reg_keys.empty() == false) {
        JvmRegistryKey jvm_reg_key = jvm_reg_keys.back();
        jvm_reg_keys.pop_back();
        os << "Trying \"";
        os << jvm_reg_key;
        os << "\" [";
        os << std::endl;
        try {
            std::string jre_bin = jvm_reg_key.readJreBin();
            std::string jvm_location = jre_bin + "\\" + jvm_dir + "\\jvm.dll";
            return openSharedLibrary(jvm_location, false);
        }
        catch (std::string& ex) {
            if (ex.find("Error 01") == 0) {
                err_rep.typeError = NOT_SUPPORTED_ARCH;
            }
            os << ex;
            os << std::endl;
        }
        catch (const std::exception& ex) {
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

SharedLibraryHandle tryHives(const char* jvm_dir, const char* java_vendor,
    const char* jdk_name, const char* jre_name)
{
    typedef std::deque<HKEY> Hives;
    Hives hives;
    hives.push_back(HKEY_CURRENT_USER);
    hives.push_back(HKEY_LOCAL_MACHINE);
    for (Hives::const_iterator it = hives.begin(), en = hives.end(); it != en;
         ++it) {
        HKEY hive = *it;
        try {
            return tryVersions(jvm_dir, hive, java_vendor, jdk_name, jre_name);
        }
        catch (const std::exception& ex) {
            std::ostream& os = err_rep.progress_os;
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

std::vector<std::string> get_search_suffixes()
{
    std::vector<std::string> search_suffixes;
    search_suffixes.push_back("");
#ifdef __linux__
#if INTPTR_MAX == INT64_MAX
     search_suffixes.push_back("/jre/lib/amd64/server");
     search_suffixes.push_back("/jre/lib/amd64/client");
     search_suffixes.push_back("/jre/lib/amd64");
     search_suffixes.push_back("/lib/amd64/server");
     search_suffixes.push_back("/lib/amd64/client");
     search_suffixes.push_back("/lib/amd64");

 #else
     search_suffixes.push_back("/jre/lib/i386/server");
     search_suffixes.push_back("/jre/lib/i386/client");
     search_suffixes.push_back("/jre/lib/i386");
     search_suffixes.push_back("/lib/i386/server");
     search_suffixes.push_back("/lib/i386/client");
     search_suffixes.push_back("/lib/i386");
 #endif
    search_suffixes.push_back("/lib/server");
    search_suffixes.push_back("/lib/client");
#endif
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
    std::string file_name;
    std::string cmd = path_parameter + " -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);
    if (out.find("Property settings") != std::string::npos) {
#ifdef __linux__
        std::string jhome_pat = "java.home = ";
        int jhome_pos = out.find(jhome_pat.c_str()) + jhome_pat.length();
        int end_pos = out.find("\n", jhome_pos);
        path = strdup(out.substr(jhome_pos, end_pos - jhome_pos).c_str());
#endif
    }
    cmd = path_parameter + file_separator() + "java -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);
    if (out.find("Property settings") != std::string::npos) {
#ifdef __linux__
        std::string jhome_pat = "java.home = ";
        int jhome_pos = out.find(jhome_pat.c_str()) + jhome_pat.length();
        int end_pos = out.find("\n", jhome_pos);
        path = strdup(out.substr(jhome_pos, end_pos - jhome_pos).c_str());
#endif
    }
#ifdef __linux__
    file_name = "libjvm.so";
#elif WIN32
    file_name = "jvm.dll";
#endif
    for (std::vector<std::string>::iterator it_s = search_suffixes.begin(), en_s = search_suffixes.end(); it_s != en_s;
         ++it_s) {
        std::string suffix = *it_s;
        std::string res_path = path + suffix + file_separator() + file_name;
        libjvm_potential_paths.push_back(res_path);
    }
    return libjvm_potential_paths;
}

std::vector<std::string> get_potential_libjvm_paths()
{
    std::vector<std::string> libjvm_potential_paths;

    std::vector<std::string> search_prefixes;
    std::vector<std::string> search_suffixes = get_search_suffixes();
    std::string file_name;

// From heuristics
#ifdef _WIN32
    search_prefixes = { "" };
    search_suffixes = { file_separator() + "jre" + file_separator() + "bin" + file_separator() + "server", file_separator() + "bin" + file_separator() + "server", file_separator() + "bin" + file_separator() + "client" };
    file_name = "jvm.dll";
#else
    search_prefixes.push_back("/usr/lib/jvm/default-java"); // ubuntu / debian distros
    search_prefixes.push_back("/usr/lib/jvm/java"); // rhel6
    search_prefixes.push_back("/usr/lib/jvm"); // centos6
    search_prefixes.push_back("/usr/lib64/jvm"); // opensuse 13
    search_prefixes.push_back("/usr/local/lib/jvm/default-java"); // alt ubuntu / debian distros
    search_prefixes.push_back("/usr/local/lib/jvm/java"); // alt rhel6
    search_prefixes.push_back("/usr/local/lib/jvm"); // alt centos6
    search_prefixes.push_back("/usr/local/lib64/jvm"); // alt opensuse 13
    for (int i = 8; i < 20; i++) {
        std::stringstream ss;
        ss << i;
        std::string version = ss.str();
        search_prefixes.push_back("/usr/lib/jvm/java-" + version + "-openjdk-amd64");
        search_prefixes.push_back("/usr/local/lib/jvm/java-" + version + "-openjdk-amd64");
        search_prefixes.push_back("/usr/lib/jvm/java-" + version + "-oracle");
        search_prefixes.push_back("/usr/local/lib/jvm/java-" + version + "-oracle");
    }
    search_prefixes.push_back("/usr/lib/jvm/default"); // alt centos
    search_prefixes.push_back("/usr/java/latest"); // alt centos
    file_name = "libjvm.so";
#endif
    // From direct environment variable
    char* env_value = NULL;
    if ((env_value = getenv("JAVA_HOME")) != NULL) {
        search_prefixes.insert(search_prefixes.begin(), env_value);
    }
    else {
        // If the environment variable is not set,
        // then there may be java is in global path?
        std::string out;
        executeCmdEx("java -XshowSettings:properties -version", out);
#ifdef _WIN32
        std::ostream& os = err_rep.progress_os;
        os << out;
        std::string str = get_property_from_regex("java\\.home", out);
        if (!str.empty()) {
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

            do {
                //wide char array

                //convert from wide char to narrow char array
                char ch[260];
                char DefChar = ' ';
                WideCharToMultiByte(CP_ACP, 0, file_data.cFileName, -1, ch, 260, &DefChar, NULL);

                //A std:string  using the char* constructor.

                std::string fileName(ch);
                std::string full_file_name = "C:\\Program Files\\Java\\" + fileName;

                if (fileName[0] == '.')
                    continue;
                search_prefixes.push_back(full_file_name);
            } while (FindNextFile(dir, &file_data));

        FindClose(dir);
        if ((dir = FindFirstFile(L"C:\\Program Files (x86)\\Java\\*", &file_data)) != INVALID_HANDLE_VALUE)

            do {
                //wide char array

                //convert from wide char to narrow char array
                char ch[260];
                char DefChar = ' ';
                WideCharToMultiByte(CP_ACP, 0, file_data.cFileName, -1, ch, 260, &DefChar, NULL);

                //A std:string  using the char* constructor.

                std::string fileName(ch);
                std::string full_file_name = "C:\\Program Files (x86)\\Java\\" + fileName;

                if (fileName[0] == '.')
                    continue;
                search_prefixes.push_back(full_file_name);
            } while (FindNextFile(dir, &file_data));
        FindClose(dir);
        if (djava_home != "") {
            search_prefixes.insert(search_prefixes.begin(), djava_home);
        }
#elif __linux__
        std::string jhome_pat = "java.home = ";
        int jhome_pos = out.find(jhome_pat.c_str()) + jhome_pat.length();
        int end_pos = out.find("\n", jhome_pos);
        std::string java_env = out.substr(jhome_pos, end_pos - jhome_pos);
        search_prefixes.insert(search_prefixes.begin(), java_env);
        if (!djava_home.empty())
            search_prefixes.insert(search_prefixes.begin(), djava_home);

#endif
    }

    // Generate cross product between search_prefixes, search_suffixes, and
    // file_name
    for (std::vector<std::string>::const_iterator it = search_prefixes.begin(), en = search_prefixes.end(); it != en;
         ++it) {
        for (std::vector<std::string>::iterator it_s = search_suffixes.begin(), en_s = search_suffixes.end(); it_s != en_s;
             ++it_s) {
            std::string prefix = *it;
            std::string suffix = *it_s;
            std::string path = prefix + file_separator() + suffix + file_separator() + file_name;
            libjvm_potential_paths.push_back(path.c_str());
        }
    }

    return libjvm_potential_paths;
}
SharedLibraryHandle checkInputDialog()
{
    SharedLibraryHandle handle =0;
    if (showDialog()!=0)
        {
        handle = checkParameters(false);
        if(handle==0)
            return checkInputDialog();
        else return handle;
    } else return 0;
}

#ifdef _WIN32

struct JavaVendorRegistryLocation {
    const char* java_vendor;
    const char* jdk_name;
    const char* jre_name;

    JavaVendorRegistryLocation(const char* a_java_vendor, const char* a_jdk_name,
        const char* a_jre_name)
        : java_vendor(a_java_vendor)
        , jdk_name(a_jdk_name)
        , jre_name(a_jre_name)
    {
    }
};

SharedLibraryHandle tryVendors(const char* jvm_dir)
{
    typedef std::deque<JavaVendorRegistryLocation> JavaVendorRegistryLocations;
    JavaVendorRegistryLocations vendor_reg_locations;
    vendor_reg_locations.push_back(JavaVendorRegistryLocation(
        "JavaSoft", "Java Development Kit", "Java Runtime Environment"));
    vendor_reg_locations.push_back(JavaVendorRegistryLocation(
        "JavaSoft", "JDK", "Java Runtime Environment"));
    vendor_reg_locations.push_back(JavaVendorRegistryLocation(
        "IBM", "Java Development Kit", "Java2 Runtime Environment"));
    for (JavaVendorRegistryLocations::const_iterator it = vendor_reg_locations.begin();
         it != vendor_reg_locations.end(); ++it) {
        try {
            return tryHives(jvm_dir, it->java_vendor, it->jdk_name, it->jre_name);
        }
        catch (const std::exception& ex) {
            std::ostream& os = err_rep.progress_os;
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
    typedef std::deque<const char*> JvmDirectories;
    JvmDirectories jvmDirectories;
    if (isClient == false && isServer == false) {
        isClient = true;
        isServer = true;
    }
    if (isClient) {
        jvmDirectories.push_back("client");
    }
    if (isServer) {
        jvmDirectories.push_back("server");
    }
    for (JvmDirectories::const_iterator it = jvmDirectories.begin(),
                                        en = jvmDirectories.end();
         it != en; ++it) {
        const char* jvmDirectory = *it;
        try {
            return tryVendors(jvmDirectory);
        }
        catch (const std::exception& ex) {
            std::ostream& os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
    }
    std::ostringstream os;
    os << "tryDirectories(" << isClient << ", " << isServer << ") failed";
    return 0;
}


// Once we've successfully opened a shared library, I think we're committed to
// trying to use it or else who knows what its DLL entry point has done. Until
// we've successfully opened it, though, we can keep trying alternatives.
SharedLibraryHandle openWindowsJvmLibrary(bool isClient, bool isServer)
{
    std::ostream& os = err_rep.progress_os;
    os << "Trying to find ";
    os << sizeof(void*) * 8;
    os << " bit jvm.dll - we need a 1.8 or newer JRE or JDK.";
    os << std::endl;
    os << "Error messages were:";
    os << std::endl;
    SharedLibraryHandle handle = tryPaths();
    if (handle != 0)
        return handle;
    handle = tryDirectories(isClient, isServer);
    if (handle != 0)
        return handle;
    std::string log_file = user_dir + "\\launcher.log";
    printErrorToLogFile(log_file.c_str(), err_rep.progress_os.str());
    if (checkInputDialog() == 0) {
        throw std::string("CANCEL");
    }
    return checkParameters(false);
}

#endif

#ifdef __linux__
int try_dlopen(std::vector<std::string> potential_paths, void*& out_handle, bool from_file_java_paths)
{
    std::ostream& os = err_rep.progress_os;
    std::string java_bin_path;
    for (std::vector<std::string>::iterator it = potential_paths.begin(), en = potential_paths.end(); it != en;
         ++it) {
        std::string p_path = *it;
        if (p_path != "") {
            os << "Trying potential path \"";
            os << p_path;
            os << "\" [";
            os << std::endl;
            out_handle = dlopen(p_path.c_str(), RTLD_NOW | RTLD_LOCAL);
            java_bin_path = p_path;
        }

        if ((out_handle != 0) && (!from_file_java_paths)) {

            std::ofstream file_java_paths; // поток для записи
            file_java_paths.open(path_to_java_paths); // окрываем файл для записи
            if (file_java_paths.is_open()) {
                file_java_paths << "jvm=" << p_path << std::endl;
            }
            file_java_paths.close();
            break;
        }
    }

    if (out_handle == 0) {
        os << "]\nerror open library";
        return 0;
    }
    os << "]";
    os << std::endl;

    std::vector<std::string> suffixes=get_search_suffixes();
    for(int i=0;i<suffixes.size();i++)
    {
        if(!suffixes.at(i).empty())
            java_bin_path=replaceFirstOccurrence(java_bin_path,suffixes.at(i)+"/libjvm.so","/bin/");
    }
    setenv("PATH",java_bin_path.c_str(),1);
    return 1;
}
SharedLibraryHandle checkParameters(bool from_file)
{
    void* handler = 0;
    if (djvm != "") {
        std::vector<std::string> paths;
        paths.push_back(djvm.c_str());
        if (try_dlopen(paths, handler, from_file)) {
            return static_cast<SharedLibraryHandle>(handler);
        }
    }
    if (djava_home != "") {
        std::vector<std::string> paths = get_potential_libjvm_paths_from_path(djava_home);
        if (try_dlopen(paths, handler, false)) {
            return static_cast<SharedLibraryHandle>(handler);
        }
    }
    return handler;
}
#endif



SharedLibraryHandle openJvmLibrary(bool isClient, bool isServer)
{
#ifdef _WIN32
    return openWindowsJvmLibrary(isClient, isServer);
#else
    //checkInputDialog();
    (void)isClient;
    (void)isServer;
    void* handler = 0;
    handler = checkParameters(false);
    if (handler != 0)
        return handler;
    std::ifstream in(path_to_java_paths); // окрываем файл для чтения
    bool f_open = in.is_open();
    if (f_open) {
        std::string line;
        while (getline(in, line)) {
            size_t offset = line.find('=');
            if (startsWith(line, "java_home")) {
                djava_home = line.substr(offset + 1);
            }
            if (startsWith(line, "jvm")) {
                djvm = line.substr(offset + 1);
            }
            if (startsWith(line, "path")) {
                dpath = line.substr(offset + 1);
            }
        }
    }
    in.close();
    handler = checkParameters(true);
    if (handler != 0)
        return handler;
    std::vector<std::string> paths = get_potential_libjvm_paths();
    if (try_dlopen(paths, handler, false)) {
        return static_cast<SharedLibraryHandle>(handler);
    }
    else {
        std::ostringstream os;
        os << "dlopen failed with "
           << dlerror() << ".";
        return checkInputDialog();
        return 0;
    }
#endif
}

struct Properties : public std::map<std::string, std::string> {
    void parse(const NativeArguments& arguments)
    {
        for (NativeArguments::const_iterator it = arguments.begin(),
                                             en = arguments.end();
             it != en; ++it) {
            std::string option = *it;
            if ((startsWith(option, "-D") == false) && (startsWith(option, "eq.") == false)) {
                continue;
            }
            size_t offset = option.find('=');
            if (offset == std::string::npos) {
                continue;
            }
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

class LauncherArgumentParser {
private:
    Properties properties;
    bool isClient;
    bool isServer;
    NativeArguments jvmArguments;
    std::string className;
    NativeArguments mainArguments;

public:
    explicit LauncherArgumentParser(const NativeArguments& launcherArguments)
    {
        properties.parse(launcherArguments);
        // Try to set the mailing list address before reporting errors.
        err_rep.support_address = properties["supportAddress"];
        djava_home = properties["java_home"];
        dpath = properties["path"];
        djvm = properties["jvm"];
        user_dir = properties["eq.user.home.dir"];
#ifdef WIN32
        std::wstring wuser_dir = utils::convertUtf8ToUtf16(user_dir);
        wuser_dir = replaceFirstOccurrenceW(wuser_dir, L"$HOME",_wgetenv(L"USERPROFILE"));
        wuser_dir = replaceFirstOccurrenceW(wuser_dir, wother_file_separator(), wfile_separator());
        user_dir=utils::convertUtf16ToUtf8(wuser_dir);
        //std::cout<<path_to_java_paths<<std::endl;
        if (!CreateDirectoryW(wuser_dir.c_str(), NULL)) {
            if (GetLastError() == ERROR_PATH_NOT_FOUND) {
                std::wstring error_message = L"error creating directory \"" + wuser_dir + L"\"";
                MessageBox(GetActiveWindow(), error_message.c_str(), L"Error", MB_OK);
                exit(EXIT_FAILURE);
            }
        }
#elif __linux__
        struct passwd* user = NULL;
        uid_t user_id = getuid();
        user = getpwuid(user_id);
        path_to_java_paths = replaceFirstOccurrence(user_dir, "$HOME", user->pw_dir);

#endif
        path_to_java_paths = user_dir + file_separator() + properties["eq.java_paths.filename"];
#ifdef WIN32
        archive_dir = utils::convertUtf8ToUtf16(user_dir + file_separator() + "java");
#if INTPTR_MAX == INT64_MAX
        archive_dir += L"64";
        path_to_java_paths += "64";
#endif
        //temp=temp+archive_name;
        if (!CreateDirectoryW(archive_dir.c_str(), NULL)) {
            if (GetLastError() == ERROR_PATH_NOT_FOUND) {
                std::wstring error_message = L"error creating directory \"" + archive_dir + L"\"";
                MessageBox(GetActiveWindow(), error_message.c_str(), L"Error", MB_OK);
                exit(EXIT_FAILURE);
            }
        };
        archive_path = archive_dir + wfile_separator() + archive_name;
#else
        mkdir(user_dir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        archive_dir = user_dir + file_separator() + "java";
#if INTPTR_MAX == INT64_MAX
        path_to_java_paths += "64";
        archive_dir += "64";
#endif
        mkdir(archive_dir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        archive_path = archive_dir + file_separator() + archive_name;
#endif
        isClient = false;
        isServer = false;
        NativeArguments::const_iterator it = launcherArguments.begin();
        NativeArguments::const_iterator end = launcherArguments.end();
        while (it != end && startsWith(*it, "-")) {
            std::string option = *it++;
            if (option == "-cp" || option == "-classpath") {
                if (it == end) {
                    throw UsageError(option + " requires an argument.");
                }
                // Translate to a form the JVM understands.
                std::string classPath = *it++;
                jvmArguments.push_back("-Djava.class.path=" + classPath);
            }
            else if (option == "-client") {
                isClient = true;
            }
            else if (option == "-server") {
                isServer = true;
            }
            else if (startsWith(option, "-D") || startsWith(option, "-X")) {
                jvmArguments.push_back(option);
            }
            else if (!startsWith(option, "eq.")) {
                mainArguments.push_back(option);
            }
        }
        if (it == end) {
            throw UsageError("No class specified.");
        }
        className = *it++;
        while (it != end) {
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

template <class ExtraInfo>
struct JvmOption : JavaVMOption {
    JvmOption(const char* ostr_arg, ExtraInfo ei_arg)
    {
        optionString = const_cast<char*>(ostr_arg);
        extraInfo = const_cast<void*>(reinterpret_cast<const void*>(
            reinterpret_cast<uintptr_t>(ei_arg)));
    }
};

template <class ExtraInfo>
JvmOption<ExtraInfo> makeJvmOption(const char* ostr_arg,
    ExtraInfo ei_arg)
{
    return JvmOption<ExtraInfo>(ostr_arg, ei_arg);
}

JvmOption<void*> makeJvmOption(const char* ostr_arg)
{
    void* ei_arg = 0;
    return JvmOption<void*>(ostr_arg, ei_arg);
}

struct JavaInvocation {
private:
    JavaVM* vm;
    JNIEnv* env;
    LauncherArgumentParser& l_args;

private:
    CreateJavaVM findCreateJavaVM()
    {
        SharedLibraryHandle sl_handle = 0;
        sl_handle = l_args.openJvmLibrary();

        CreateJavaVM jvm = 0;
#ifdef __linux__
        try {
            jvm = reinterpret_cast<CreateJavaVM>(reinterpret_cast<uintptr_t>(
                dlsym(sl_handle, "JNI_CreateJavaVM")));
        }
        catch (const std::exception& ex) {
            std::ostream& os = err_rep.progress_os;
            os << ex.what();
            os << std::endl;
        }
#else
        jvm = reinterpret_cast<CreateJavaVM>(reinterpret_cast<uintptr_t>(
            GetProcAddress(reinterpret_cast<HMODULE>(sl_handle), "JNI_CreateJavaVM")));
#endif
        if (jvm == 0) {

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
                os << "A Java Runtime Environment (JRE) or Java Development Kit (JDK) must be available in order to run Red Expert. "
                   << "No Java virtual machine was found.\n"
                   << "If the required Java Runtime Environment is not installed, you can download it from from the website:\n"
                   << "http://java.com/\n"
                   << "then try again.";
            throw std::runtime_error(os.str());
        }
        return jvm;
    }

    jclass findClass(const std::string& c_name)
    {
        // Internally, the JVM tends to use '/'-separated class names.
        // Externally, '.'-separated class names are more common.
        // '.' is never valid in a class name, so we can unambiguously translate.
        std::string can_name(c_name);
        for (std::string::iterator it = can_name.begin(),
                                   end = can_name.end();
             it != end; ++it) {
            if (*it == '.') {
                *it = '/';
            }
        }

        jclass j_class = env->FindClass(can_name.c_str());
        if (j_class == 0) {
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
        jmethodID method = env->GetStaticMethodID(cl_main, "main", "([Ljava/lang/String;)V");
        if (method == 0) {
            throw std::runtime_error("GetStaticMethodID(\"main\") failed.");
        }
        return method;
    }
#ifdef __linux__
    jstring makeJavaString(const char* n_string)
    {
        jstring j_string = env->NewStringUTF(n_string);
                if (j_string == 0) {
                    std::ostringstream os;
                    os << "NewStringUTF(\"" << n_string << "\") failed.";
                    throw std::runtime_error(os.str());
                }
         return j_string;
    }
#else
    jstring makeJavaString(const wchar_t* n_string)
    {
        jstring j_string =env->NewString((const jchar *)n_string, wcslen(n_string));
        return j_string;
    }
 #endif

    jobjectArray convertArguments(const NativeArguments& n_args)
    {
        jclass str_cl = findClass("java/lang/String");
        #ifdef __linux__
        jstring def_args = makeJavaString("");
        #else
        jstring def_args = makeJavaString(L"");
        #endif
        jobjectArray j_args = env->NewObjectArray(
            n_args.size(), str_cl, def_args);
        if (j_args == 0) {
            std::ostringstream os;
            os << "NewObjectArray(" << n_args.size() << ") failed.";
            throw std::runtime_error(os.str());
        }
        for (size_t index = 0; index != n_args.size(); ++index) {
            std::string n_arg = n_args[index];
#ifdef _WIN32
            jstring j_arg = makeJavaString(utils::convertUtf8ToUtf16(n_arg).c_str());
#else
            jstring j_arg = makeJavaString(n_arg.c_str());
#endif
            env->SetObjectArrayElement(j_args, index, j_arg);
        }
        return j_args;
    }

public:
    explicit JavaInvocation(LauncherArgumentParser& l_argsA)
        : l_args(l_argsA)
    {
        typedef std::vector<JavaVMOption>
            JavaVMOptions; // Required to be contiguous.
        const NativeArguments& jvm_args = l_args.getJvmArguments();
        JavaVMOptions j_vm_opts;
        for (size_t i = 0; i != jvm_args.size(); ++i) {
            j_vm_opts.push_back(makeJvmOption(jvm_args[i].c_str()));
        }

        j_vm_opts.push_back(makeJvmOption("abort", &abortJvm));

        JavaVMInitArgs j_vm_init_args;

        j_vm_init_args.version = JNI_VERSION_1_8;
        j_vm_init_args.options = &j_vm_opts[0];
        j_vm_init_args.nOptions = j_vm_opts.size();
        j_vm_init_args.ignoreUnrecognized = false;

        CreateJavaVM cr_java_vm = findCreateJavaVM();
        int result = cr_java_vm(&vm, reinterpret_cast<void**>(&env), &j_vm_init_args);
        if (result < 0) {
            std::ostringstream os;
            os << "JNI_CreateJavaVM(options=[";
            for (size_t i = 0; i < j_vm_opts.size(); ++i) {
                os << (i > 0 ? ", " : "") << '"' << j_vm_opts[i].optionString
                   << '"';
            }
            os << "]) failed with " << JniError(result) << ".";
            throw std::runtime_error(os.str());
        }
    }

    void reportAnyJavaException(std::ostream& os)
    {
        jthrowable j_exc = env->ExceptionOccurred();
        if (j_exc == 0) {
            return;
        }
        // Report it via stderr first, in case we fail later and overwrite the
        // pending exception.
        env->ExceptionDescribe();
        os << "A Java exception occurred.";
        os << std::endl;
        jclass str_utl = env->FindClass("org/apache/commons/lang/exception/ExceptionUtils");
        if (str_utl == 0) {
            os << "FindClass(\"org.apache.commons.lang.exception.ExceptionUtils\") failed.";
            os << std::endl;
            return;
        }
        jmethodID throw_stack = env->GetStaticMethodID(str_utl, "getStackTrace",
            "(Ljava/lang/Throwable;)Ljava/lang/String;");
        if (throw_stack == 0) {
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
        if (env->ExceptionCheck()) {
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
        jobjectArray j_args = convertArguments(l_args.getMainArguments());
        env->CallStaticVoidMethod(j_class, j_method, j_args);
        if (env->ExceptionCheck() == false) {
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

void ErrorReporter::generateReport(const std::exception& ex,
    const std::string& usage) const
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

    reportFatalErrorViaGui("Red Expert", os.str(), support_address, typeError);
}

#ifdef _WIN32
VOID CALLBACK TimerProc(

    HWND hwnd,
    UINT uMsg,
    UINT_PTR idEvent,
    DWORD dwTime)
{
    if (idEvent == 1001) {

        long filesize = ds.GetFileSize();
        long progress = ds.GetProgress();
        if (filesize != 0) {
            //SetDlgItemText(dialog_download, 1, mes.c_str());
            float procent = ((float)progress) / ((float)filesize);
            procent *= 100;
            int pb_pos = (int)procent; // увеличить
            SendMessage(h_progress, PBM_SETPOS, pb_pos, 0);
            if (progress == filesize) {
                EndDialog(h_dialog_download, 0);
                KillTimer(h_dialog_download, idEvent);
            }
        }
    }
}
static int showError=0;
HRESULT error_code;
INT_PTR CALLBACK DlgDownloadProc(HWND hw, UINT msg, WPARAM wp, LPARAM lp)
{

    switch (msg) {
    case WM_INITDIALOG: {
        h_dialog_download = hw;
        h_progress = CreateWindowEx(0, //создаём окно
            PROGRESS_CLASS,
            L"ProgressBar", //тип окна
            WS_CHILD | WS_BORDER | WS_VISIBLE, //стиль окна
            10, 40, //расположение
            270, 20,
            hw, //родительское окно
            NULL,
            GetModuleHandle(NULL), //эту переменную надо объявить в начале программы HANDLE hInst;
            NULL);
        SendMessage(h_progress, PBM_SETRANGE, 0, (LPARAM)MAKELONG(0, 100));
        //Установим шаг
        SendMessage(h_progress, PBM_SETSTEP, (WPARAM)1, 0); //шаг 1
        ds.OnStartBinding();
        showError = 0;
        /* сообщение о создании диалога */
        th = std::thread([] {
            HRESULT res=URLDownloadToFile(
                                  0,
                                  download_url,
                                  archive_path.c_str(),
                                  0,
                                  &ds);
            EndDialog(h_dialog_download, 0);
            KillTimer(h_dialog_download, 1001);
            if(res!=S_OK&&res!=E_ABORT)
            {

                showError=1;
                error_code=res;
               // }
               // else  MessageBox(GetActiveWindow(),L"Unknown error", TEXT("Error download"), MB_OK);

            }
            });
        SetTimer(hw, 1001, 1000, TimerProc);
        return TRUE;
    }
    case WM_COMMAND: /* сообщение от управляющих элементов */
        if (LOWORD(wp) == 3) {
            ds.AbortDownl();
            EndDialog(hw, 0);
        }
    }
    return FALSE;
}
INT_PTR CALLBACK DlgProc(HWND hw, UINT msg, WPARAM wp, LPARAM lp)
{
    std::wstring m_mes;
    std::wstring arch;
#if INTPTR_MAX == INT32_MAX
    arch = L"x86";
#elif INTPTR_MAX == INT64_MAX
    arch = L"amd64";
#endif
    switch (msg) {
    case WM_INITDIALOG: /* сообщение о создании диалога */
        SendMessage(GetDlgItem(hw, CHOOSE_FILE), BM_SETCHECK, BST_CHECKED, 0);
        if (err_rep.typeError == NOT_SUPPORTED_ARCH) {
            m_mes.append(L"Java with invalid architecture found. ");
        }
        else if (err_rep.typeError == NOT_SUPPORTED_ARCH) {
            m_mes.append(L"Java with invalid version found. ");
        }
        else {
            m_mes.append(L"Java not found. ");
        }
        m_mes.append(L"You can specify the path to jvm manually\nor download java automatically or manually from ");
        m_mes.append(L"<A HREF=\"");
        m_mes.append(url_manual);
        m_mes.append(L"\">");
        m_mes.append(url_manual);
        m_mes.append(L"</A>");
        m_mes.append(L"\nNote that you need Java 1.8 or higher with ");
        m_mes.append(arch);
        m_mes.append(L" architecture. ");
        SetDlgItemText(hw, 1, m_mes.c_str());
        return TRUE;
    case WM_COMMAND: /* сообщение от управляющих элементов */
        if (LOWORD(wp) == 6) {
            if (IsDlgButtonChecked(hw, DOWNLOAD))
            {
                HINSTANCE h = GetModuleHandle(NULL);
                EnableWindow(hw, FALSE);
                DialogBox(h, MAKEINTRESOURCEW(P_BAR_DIALOG), NULL, DlgDownloadProc);
                th.join();
                EnableWindow(hw, TRUE);
                DeleteUrlCacheEntry(download_url);
                if(showError==1)
                {
                    LPTSTR errorText = NULL;
                        std::wstring mes = L"Error code: ";
                        mes.append(std::to_wstring(error_code));
                        mes.append(L"\nCheck internet connection.");
                        MessageBox(hw,mes.c_str(), TEXT("Error download"), MB_OK);
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
                for (int zi = 0; zi < numitems; zi++) {
                    ZIPENTRY ze;
                    GetZipItem(hz, zi, &ze); // fetch individual details
                    UnzipItem(hz, zi, ze.name); // e.g. the item's name.
                }
                CloseZip(hz);
                _wremove(archive_path.c_str());
                HANDLE dir;
                WIN32_FIND_DATA file_data;
                std::wstring str = archive_dir + wfile_separator() + L"*";
                if ((dir = FindFirstFile(str.c_str(), &file_data)) != INVALID_HANDLE_VALUE)

                    do {
                        //wide char array

                        //convert from wide char to narrow char array


                        //A std:string  using the char* constructor.

                        std::string fileName=utils::convertUtf16ToUtf8(file_data.cFileName);
                        std::string full_file_name = utils::convertUtf16ToUtf8(archive_dir) + file_separator() + fileName;

                        if (fileName[0] == '.')
                            continue;
                        djava_home = full_file_name;
                    } while (FindNextFile(dir, &file_data));

                FindClose(dir);
                result_dialog = DOWNLOAD;
                EndDialog(hw, 0);
        }
            if (IsDlgButtonChecked(hw, CHOOSE_FILE))
            {
                std::wstring ws = basicOpenFolder();
                if (ws != L"") {
                    //std::wcout<<L"temp="<<temp<<std::endl;
                    std::wcout<<L"ws="<<ws<<std::endl;
                    djava_home = utils::convertUtf16ToUtf8(ws);
                    std::cout<<"djava_home="<<djava_home<<std::endl;
                    result_dialog = CHOOSE_FILE;
                    EndDialog(hw, 0);
                }

            }

        }
        if(LOWORD(wp)==7)
        {
            result_dialog = CANCEL;
            EndDialog(hw, 0);
        }
        return TRUE;
    case WM_NOTIFY:
        if (LOWORD(wp) == 1) {
            switch (((LPNMHDR)lp)->code) {

            case NM_CLICK: // Fall through to the next case.

            case NM_RETURN: {
                PNMLINK pNMLink = (PNMLINK)lp;
                LITEM item = pNMLink->item;

                if (item.iLink == 0) {
                    ShellExecute(NULL, L"open", item.szUrl, NULL, NULL, SW_SHOW);
                }

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
    DialogBox(h, MAKEINTRESOURCEW(DIALOG_X), NULL, DlgProc);

    if (result_dialog == DOWNLOAD) {

        return 1;
    }
    else if (result_dialog == CHOOSE_FILE) {
            return 1;
    }
    else {
        return 0;
    }
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
    if (handle == 0) {
        // No mention is made of GetLastError() at the MSDN page du jour.
        throw std::runtime_error("AddVectoredContinueHandler failed");
    }
    // The handle of the next handler seems to be at our handle.
    // The previous handle follows.
    // The rest of the structure remained opaque to me, though I didn't try
    // RtlDecodePointer.
    void* w_handle = *reinterpret_cast<void**>(handle);
    ULONG rc = RemoveVectoredContinueHandler(w_handle);
    if (rc == 0) {
    }
    rc = RemoveVectoredContinueHandler(handle);
    if (rc == 0) {
        throw std::runtime_error("RemoveVectoredContinueHandler(handle) failed");
    }
}

#else

static void deferToHotSpotExceptionHandler()
{
}

#endif

static int runJvm(const NativeArguments& l_args)
{
    try {
        LauncherArgumentParser parser(l_args);
        deferToHotSpotExceptionHandler();
        JavaInvocation j_invocation(parser);
        return j_invocation.invokeMain();
    }
    catch (std::string& ex) {
        std::cout << ex << std::endl;
        int res = ex.compare("CANCEL");
        if (res == 0)
            return 1;
        err_rep.reportUsageError(UsageError(ex));
    }
    catch (const UsageError& ex) {
        err_rep.reportUsageError(ex);
        return 1;
    }
    catch (const std::exception& ex) {
        err_rep.reportFatalException(ex);
        return 1;
    }
}

int main(int argc, char* argv[])
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

    int app_pid;
#ifdef __linux__
    app_exe_path = getSelfPath();
    std::string tmp_path = getSelfPath();
    bin_dir = dirname((char*)tmp_path.c_str());
    app_pid = getpid();
#else
    // hide console window
    ::ShowWindow(::GetConsoleWindow(), SW_HIDE);
    HMODULE h_module = GetModuleHandleW(NULL);
    WCHAR path[MAX_PATH];
    GetModuleFileNameW(h_module, path, MAX_PATH);
    std::wstring ws(path);
    std::string str=utils::convertUtf16ToUtf8(ws);
    app_exe_path = str;
    char buf[256];
    GetCurrentDirectoryA(256, buf);
    bin_dir = std::string(buf);
    app_pid = GetCurrentProcessId();
#endif

    app_dir = bin_dir + file_separator() + "..";
    paths.append(app_dir + file_separator() + "RedExpert.jar");
    paths.append(separator);

#ifdef __linux__
    paths.append(app_dir + "/createDesktopEntry.sh");
    paths.append(separator);
    paths.append(app_dir + "/redexpert.desktop");
    paths.append(separator);
#endif

    std::string lib_dir(app_dir + file_separator() + "lib");

#ifdef __linux__
    DIR* dir;
    struct dirent* ent;
    if ((dir = opendir(lib_dir.c_str())) != NULL) {
        while ((ent = readdir(dir)) != NULL) {
            std::string conv_file(ent->d_name);
            if (conv_file.rfind("fbplugin-impl", 0) == 0 || conv_file.rfind("fbclient", 0) == 0 || conv_file.rfind("jaybird", 0) == 0)
                continue;
            paths.append(lib_dir + "/" + ent->d_name);
            paths.append(separator);
        }
        closedir(dir);
    }
    else {
        err_rep.reportFatalException(std::system_error(ENOENT, std::generic_category(), "No library directory found"));
        exit(EXIT_FAILURE);
    }
#else
    WIN32_FIND_DATA data;
    std::wstring wlib_dir(lib_dir.begin(), lib_dir.end());
    wlib_dir.append(L"\\*");
    HANDLE h_find = FindFirstFile(wlib_dir.c_str(), &data);

    if (h_find != INVALID_HANDLE_VALUE) {
        do {
            // convert from wide char to narrow char array
            char buffer[1024];
            char def_char = '\0';
            WideCharToMultiByte(CP_ACP, 0, data.cFileName, -1, buffer, 260, &def_char,
                NULL);
            std::string conv_file(buffer);
            if (conv_file.rfind("fbplugin-impl", 0) == 0 || conv_file.rfind("fbclient", 0) == 0 || conv_file.rfind("jaybird", 0) == 0)
                continue;
            if (conv_file != "." && conv_file != "..") {
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
    char* class_path = (char*)stdString.c_str();

    NativeArguments launcher_args;

    err_rep.ARGV0 = *argv++;
    while (*argv != 0) {
        launcher_args.push_back(*argv++);
    }
    err_rep.launcher_args = launcher_args;

    launcher_args.push_back(class_path);
    launcher_args.push_back("org/executequery/ExecuteQuery");

    launcher_args.push_back("-exe_path=" + app_exe_path);
    std::string str_pid = utils::toString(app_pid);
    launcher_args.push_back("-exe_pid=" + str_pid);
    std::string path_config;

    path_config = replaceFirstOccurrence(app_exe_path, "bin" + file_separator() + "RedExpert64" + extension_exe_file(), "config" + file_separator() + "redexpert_config.ini");
    path_config = replaceFirstOccurrence(path_config, "bin" + file_separator() + "RedExpert" + extension_exe_file(), "config" + file_separator() + "redexpert_config.ini");
    std::ifstream in(path_config); // окрываем файл для чтения
    bool f_open = in.is_open();
    std::string line;
    if (f_open) {
        while (getline(in, line)) {
            if (startsWith(line, "eq.")) {
                launcher_args.push_back(line);
            }
        }
    }
    in.close();
    return runJvm(launcher_args);
}
