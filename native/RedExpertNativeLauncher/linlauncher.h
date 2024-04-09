#include <pwd.h>
#include <ctime>
#include <dlfcn.h>
#include <dirent.h>
#include <libgen.h>
#include <unistd.h>
#include <string.h>
#include <sys/stat.h>
#include <curl/curl.h>
#include <sys/types.h>
#include <system_error>

#include "baselauncher.h"

#if INTPTR_MAX == INT64_MAX

static std::string url_manual = "https://www.oracle.com/java/technologies/javase-downloads.html";
static std::string download_url = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8.1%2B1/OpenJDK17U-jre_x64_linux_hotspot_17.0.8.1_1.tar.gz";

#elif INTPTR_MAX == INT32_MAX

static std::string url_manual = "https://www.java.com/ru/download/manual.jsp";
static std::string download_url = "https://download.bell-sw.com/java/14+36/bellsoft-jre14+36-linux-i586.tar.gz";

#else
#error "Environment not 32 or 64-bit."
#endif

static double d = 0;
static double t = 0;
static double last_d;

static bool timeout_error;
static time_t last_time;

static int ABORT_DOWNLOAD = -1;
static int DOWNLOADING = 0;
static int FINISHED_DOWNLOADING = 1;
static int ERROR_DOWNLOAD = -2;

static std::string http_code;
static std::string archive_dir;
static std::string archive_path;
static std::string archive_name = "java.tar.gz";

FILE *outfile;
int status_downl;
SharedLibraryHandle curlHandle;

CURL *curl;
GtkWidget *Bar;
GtkWidget *dialog_dwnl;
GtkLabel *upLabel;
GtkLabel *downLabel;
GThread *downl_thread;

void (*curl_easy_reset_)(CURL *);
void (*curl_easy_cleanup_)(CURL *);
void (*curl_global_cleanup_)(void);
const char *(*curl_easy_strerror_)(CURLcode);

CURL *(*curl_easy_init_)(void);
CURLcode (*curl_easy_perform_)(CURL *);
CURLcode (*curl_global_init_)(long flags);
CURLcode (*curl_easy_setopt_)(CURL *, CURLoption, ...);

void download_java();

extern "C"
{
    typedef jint (*CreateJavaVM)(JavaVM **, void **, void *);
    typedef jint (*CreateJvmFuncPtr)(JavaVM **, void **, JavaVMInitArgs *);
}

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

        djvm = properties["jvm"];
        dpath = properties["path"];
        djava_home = properties["java_home"];
        user_dir = properties["eq.user.home.dir"];
        err_rep.support_address = properties["supportAddress"];

        struct passwd *user = NULL;
        uid_t user_id = getuid();
        user = getpwuid(user_id);

        path_to_java_paths = replaceFirstOccurrence(user_dir, "$HOME", user->pw_dir);
        path_to_java_paths = user_dir + file_separator() + properties["eq.java_paths.filename"];

        mkdir(user_dir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        archive_dir = user_dir + file_separator() + "java";

#if INTPTR_MAX == INT64_MAX
        path_to_java_paths += "64";
        archive_dir += "64";
#endif

        mkdir(archive_dir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        archive_path = archive_dir + file_separator() + archive_name;

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
        try
        {
            jvm = reinterpret_cast<CreateJavaVM>(reinterpret_cast<uintptr_t>(dlsym(sl_handle, "JNI_CreateJavaVM")));
        }
        catch (const std::exception &ex)
        {
            std::ostream &os = err_rep.progress_os;
            os << ex.what() << std::endl;
        }

        if (jvm == 0)
        {
            std::ostringstream os;

            os << "dlsym(" << sl_handle << ", JNI_CreateJavaVM) failed with " << dlerror() << ".\n";
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
            jstring j_arg = makeJavaString(n_arg.c_str());

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

std::string ErrorReporter::getUsage() const
{
    std::ostringstream os;
    os << "Usage: " << ARGV0 << " [options] class [args...]" << std::endl;
    os << "where options are:" << std::endl;
    os << "  -client - use client VM" << std::endl;
    os << "  -server - use server VM" << std::endl;
    os << "  -cp <path> | -classpath <path> - set the class search path" << std::endl;
    os << "  -D<name>=<value> - set a system property" << std::endl;
    os << "  -verbose[:class|gc|jni] - enable verbose output" << std::endl;
    os << "or any option implemented internally by the chosen JVM." << std::endl;
    os << std::endl;

    return os.str();
}

void ErrorReporter::generateReport(const std::exception &ex, const std::string &usage) const
{
    std::ostringstream os;
    os << "Error: " << ex.what() << std::endl;
    os << "JVM selection:" << std::endl;
    os << progress_os.str() << std::endl;
    os << usage << std::endl;
    os << "Command line was:" << ARGV0 << " " << utils::join(" ", launcher_args) << std::endl;

    reportFatalErrorViaGui("Red Expert", os.str(), support_address, typeError, locale);
}

std::string getUsabilitySize(long countByte)
{
    int oneByte = 1024;
    int delimiter = 103;
    long drob = 0;

    if (countByte > 1024)
    {
        drob = (countByte % oneByte) / delimiter;
        countByte = countByte / oneByte;
        if (countByte > oneByte)
        {
            drob = (countByte % oneByte) / delimiter;
            countByte = countByte / oneByte;
            if (countByte > oneByte)
            {
                drob = (countByte % oneByte) / delimiter;
                countByte = countByte / oneByte;
                if (countByte > oneByte)
                {
                    drob = (countByte % oneByte) / delimiter;
                    countByte = countByte / oneByte;
                    return std::to_string(countByte) + "," + std::to_string(drob) + "Tb";
                }
                else
                    return std::to_string(countByte) + "," + std::to_string(drob) + "Gb";
            }
            else
                return std::to_string(countByte) + "," + std::to_string(drob) + "Mb";
        }
        else
            return std::to_string(countByte) + "," + std::to_string(drob) + "Kb";
    }
    else
        return std::to_string(countByte) + "b";
}

extern "C" void ok_button_clicked(GtkButton *button, gpointer data)
{

    if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(rb_download)))
    {
        download_java();
        if (status_downl == ABORT_DOWNLOAD || status_downl == ERROR_DOWNLOAD)
            return;

        dialog_result = DOWNLOAD;
    }

    if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(rb_file)))
    {
        char *s = gtkOpenFile(locale);
        if (s == 0)
            return;

        djava_home = s;
        dialog_result = CHOOSE_FILE;
    }

    gtk_widget_destroy(dialog);
    gtk_main_quit();
}

extern "C" void cancel_button_clicked_cb(GtkButton *button, gpointer data)
{
    status_downl = ABORT_DOWNLOAD;
    gtk_widget_destroy(dialog_dwnl);
    gtk_main_quit();
}

extern "C" void destroy_dialog(GtkDialog *d, gpointer data)
{
    if (status_downl == DOWNLOADING)
        status_downl = ABORT_DOWNLOAD;
}

size_t my_write_func(void *ptr, size_t size, size_t nmemb, FILE *stream)
{
    return fwrite(ptr, size, nmemb, stream);
}

size_t my_read_func(void *ptr, size_t size, size_t nmemb, FILE *stream)
{
    return fread(ptr, size, nmemb, stream);
}

int my_progress_func(
    GtkWidget *bar,
    double tt, /* dltotal */
    double dd, /* dlnow */
    double ultotal,
    double ulnow)
{

    t = tt;
    d = dd;

    if (status_downl == ABORT_DOWNLOAD || status_downl == ERROR_DOWNLOAD)
        return 1;

    return 0;
}

static size_t header_callback(char *buffer, size_t size, size_t nitems, void *userdata)
{
    std::string s = buffer;
    if (s.find("HTTP") != std::string::npos)
        http_code = s;

    return nitems * size;
}

static gboolean updateProgress(gpointer data)
{
    if (status_downl == DOWNLOADING)
    {
        time_t cur_time = time(NULL);

        if (last_time == 0)
        {
            last_time = cur_time;
        }
        else
        {
            if (d != last_d)
            {
                last_time = cur_time;
            }
            else if (cur_time - last_time > 20)
            {
                status_downl = ERROR_DOWNLOAD;
                timeout_error = true;
            }
        }

        last_d = d;
        if (t != 0)
        {
            std::string text = getUsabilitySize((long)d) + "/" + getUsabilitySize((long)t);
            gtk_label_set_text(GTK_LABEL(downLabel), text.c_str());
            gtk_progress_bar_set_fraction(GTK_PROGRESS_BAR(Bar), d / t);
        }

        if (d == t && d != 0)
        {
            status_downl = FINISHED_DOWNLOADING;
            gtk_widget_destroy(dialog_dwnl);
            gtk_main_quit();

            return FALSE;
        }
    }
    else if (status_downl == ERROR_DOWNLOAD)
    {
        gtk_widget_destroy(dialog_dwnl);
        gtk_main_quit();

        return FALSE;
    }

    return TRUE;
}

void init_curl()
{
    status_downl = DOWNLOADING;

    curlHandle = dlopen("libcurl.so.4", RTLD_LAZY);
    if (curlHandle == 0)
    {
        char *error_title = "Error downloading Java";
        char *error_message = "libcurl package not found. Please install libcurl4 to automatically download Java.";
        if (locale.find("ru") != -1)
        {
            error_title = "Ошибка скачивания Java";
            error_message = "Пакет libcurl не найден. Пожалуйста, установите libcurl4, чтобы автоматически скачать Java.";
        }

        gtkMessageBox(error_title, error_message);
        status_downl = ERROR_DOWNLOAD;
    }

    curl_easy_init_ = (CURL * (*)(void)) dlsym(curlHandle, "curl_easy_init");
    curl_easy_reset_ = (void (*)(CURL *))dlsym(curlHandle, "curl_easy_reset");
    curl_easy_cleanup_ = (void (*)(CURL *))dlsym(curlHandle, "curl_easy_cleanup");
    curl_global_cleanup_ = (void (*)(void))dlsym(curlHandle, "curl_global_cleanup");
    curl_easy_perform_ = (CURLcode(*)(CURL *))dlsym(curlHandle, "curl_easy_perform");
    curl_global_init_ = (CURLcode(*)(long flags))dlsym(curlHandle, "curl_global_init");
    curl_easy_strerror_ = (const char *(*)(CURLcode))dlsym(curlHandle, "curl_easy_strerror");
    curl_easy_setopt_ = (CURLcode(*)(CURL *, CURLoption, ...))dlsym(curlHandle, "curl_easy_setopt");

    (*curl_global_init_)(CURL_GLOBAL_ALL);
    curl = (*curl_easy_init_)();

    if (curl)
    {
        CURLcode res;
        const char *url = download_url.c_str();
        curl_easy_setopt_(curl, CURLOPT_URL, url);
        curl_easy_setopt_(curl, CURLOPT_HEADER, 1);
        curl_easy_setopt_(curl, CURLOPT_NOBODY, 1);
        curl_easy_setopt_(curl, CURLOPT_HEADERFUNCTION, header_callback);
        http_code = "0";
        res = curl_easy_perform_(curl);

        char *error_title = "Error downloading Java";
        if (locale.find("ru") != -1)
            error_title = "Ошибка скачивания Java";

        if (res != CURLE_OK)
        {
            gtkMessageBox(error_title, curl_easy_strerror_(res));
            status_downl = ERROR_DOWNLOAD;
        }
        else if (http_code.find("20") == std::string::npos && http_code.find("30") == std::string::npos)
        {
            gtkMessageBox(error_title, http_code.c_str());
            status_downl = ERROR_DOWNLOAD;
        }

        curl_easy_cleanup_(curl);
        if (status_downl == ERROR_DOWNLOAD)
            return;

        curl = (*curl_easy_init_)();
        if (curl)
        {
            const char *url = download_url.c_str();
            outfile = fopen(archive_path.c_str(), "wb");

            curl_easy_setopt_(curl, CURLOPT_URL, url);
            curl_easy_setopt_(curl, CURLOPT_FOLLOWLOCATION, 1L);
            curl_easy_setopt_(curl, CURLOPT_WRITEDATA, outfile);
            curl_easy_setopt_(curl, CURLOPT_WRITEFUNCTION, my_write_func);
            curl_easy_setopt_(curl, CURLOPT_READFUNCTION, my_read_func);
            curl_easy_setopt_(curl, CURLOPT_NOPROGRESS, 0L);
            curl_easy_setopt_(curl, CURLOPT_PROGRESSFUNCTION, my_progress_func);

            timeout_error = false;
            last_time = 0;
        }
    }
}

CURLcode download_in_thread()
{
    if (curl)
    {
        status_downl = DOWNLOADING;

        CURLcode res = curl_easy_perform_(curl);
        if (res != CURLE_OK)
        {
            std::cout << curl_easy_strerror_(res) << std::endl;
            if (status_downl != ABORT_DOWNLOAD)
                status_downl = ERROR_DOWNLOAD;
        }

        fclose(outfile);
        curl_easy_cleanup_(curl);

        return res;
    }
}

int showDialog()
{
    std::string path_to_glade = bin_dir + file_separator();
    if (locale.find("ru") != -1)
        path_to_glade += "../resources/dialog_java_not_found_ru.glade";
    else
        path_to_glade += "../resources/dialog_java_not_found_en.glade";

    gtkDialog(path_to_glade, url_manual, locale);

    if (dialog_result == CANCEL)
        exit(1);

    if (dialog_result == CHOOSE_FILE || dialog_result == DOWNLOAD)
        return 1;

    return 0;
}

void download_java()
{
    init_curl();
    if (status_downl == ERROR_DOWNLOAD)
        return;

    GError *error = NULL;
    builder = gtk_builder_new();

    std::string path_to_glade = bin_dir + file_separator();
    if (locale.find("ru") != -1)
        path_to_glade += "../resources/download_dialog_ru.glade";
    else
        path_to_glade += "../resources/download_dialog_en.glade";

    if (!gtk_builder_add_from_file(builder, path_to_glade.c_str(), &error))
    {
        g_warning("%s", error->message);
        g_error_free(error);

        return;
    }

    char *upLabelText = "Please wait while Java is downloading.";
    if (locale.find("ru") != -1)
        upLabelText = "Пожалуйста, дождитесь полного скачивания Java";

    dialog_dwnl = GTK_WIDGET(gtk_builder_get_object(builder, "download_dialog"));
    gtk_builder_connect_signals(builder, NULL);
    Bar = GTK_WIDGET(gtk_builder_get_object(builder, "prog_bar"));
    upLabel = GTK_LABEL(gtk_builder_get_object(builder, "up_text"));
    downLabel = GTK_LABEL(gtk_builder_get_object(builder, "down_text"));
    gtk_progress_bar_set_fraction(GTK_PROGRESS_BAR(Bar), 0);
    gtk_label_set_text(GTK_LABEL(upLabel), upLabelText);

    g_object_unref(G_OBJECT(builder));
    gtk_window_set_transient_for(GTK_WINDOW(dialog_dwnl), GTK_WINDOW(dialog));

    // Показываем форму и виджеты на ней
    gtk_widget_show(dialog_dwnl);
    static CURLcode res = CURLE_OK;
    std::thread th = std::thread([]
                                 { res = download_in_thread(); });

    g_timeout_add_seconds(1, updateProgress, NULL);
    gtk_main();
    th.join();

    if (status_downl == ERROR_DOWNLOAD)
    {
        char *error_title = "Error downloading java";
        std::string timeout_error_message = "Timeout was reached";
        std::string check_connection_message = "\nCheck internet connection";
        if (locale.find("ru") != -1)
        {
            error_title = "Ошибка скачивания Java";
            timeout_error_message = "Превышено время ожидания ответа от сервера";
            check_connection_message = "\nПожалуйста, проверьте интернет соединение.";
        }

        std::stringstream stream;
        if (timeout_error)
            stream << timeout_error_message;
        else
            stream << curl_easy_strerror_(res);
        stream << check_connection_message;

        gtkMessageBox(error_title, stream.str().c_str());
    }

    if (status_downl == ABORT_DOWNLOAD || status_downl == ERROR_DOWNLOAD)
        return;

    std::string command = "tar -C " + archive_dir + " -xvf " + archive_path;
    system(command.c_str());
    command = "rm " + archive_path;
    system(command.c_str());

    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir(archive_dir.c_str())) != NULL)
    {
        while ((ent = readdir(dir)) != NULL)
        {
            std::string dir_name = ent->d_name;
            if (dir_name == "." || dir_name == "..")
                continue;

            djava_home = archive_dir + file_separator() + dir_name;
        }
        closedir(dir);
    }
}

int executeCmdEx(const char *cmd, std::string &result)
{
    char buffer[128];
    int ret_code = -1; // -1 if error ocurs.
    std::string command(cmd);
    command.append(" 2>&1"); // also redirect stderr to stdout

    result = "";
    FILE *pipe;

    pipe = popen(command.c_str(), "r");
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
            ret_code = pclose(pipe);
            throw;
        }

        ret_code = pclose(pipe);
    }

    return ret_code;
}

SharedLibraryHandle openSharedLibrary(const std::string &sl_file, std::string path, bool from_file_java_paths)
{
    std::ostringstream os;

    void *sl_handle = dlopen(sl_file.c_str(), RTLD_LAZY);
    if (sl_handle == 0)
    {
        os << "dlopen(\"" << sl_handle << "\") failed with " << dlerror() << "." << std::endl;
        os << "If you can't otherwise explain why this call failed, consider ";
        os << "whether all of the shared libraries ";
        os << "used by this shared library can be found." << std::endl;

        throw std::runtime_error(os.str());
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

std::vector<std::string> get_search_suffixes()
{
    std::vector<std::string> search_suffixes;

    search_suffixes.push_back("");
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
    std::string file_name = "libjvm.so";

    std::string cmd = path_parameter + " -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);
    if (out.find("Property settings") != std::string::npos)
    {
        std::string jhome_pat = "java.home = ";
        int jhome_pos = out.find(jhome_pat.c_str()) + jhome_pat.length();
        int end_pos = out.find("\n", jhome_pos);

        path = strdup(out.substr(jhome_pos, end_pos - jhome_pos).c_str());
    }

    cmd = path_parameter + file_separator() + "java -XshowSettings:properties -version";
    executeCmdEx(cmd.c_str(), out);
    if (out.find("Property settings") != std::string::npos)
    {
        std::string jhome_pat = "java.home = ";
        int jhome_pos = out.find(jhome_pat.c_str()) + jhome_pat.length();
        int end_pos = out.find("\n", jhome_pos);

        path = strdup(out.substr(jhome_pos, end_pos - jhome_pos).c_str());
    }

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

    std::string file_name = "libjvm.so";
    std::vector<std::string> search_suffixes = get_search_suffixes();

    std::vector<std::string> search_prefixes;
    search_prefixes.push_back("/usr/lib/jvm/default-java");       // ubuntu / debian distros
    search_prefixes.push_back("/usr/lib/jvm/java");               // rhel6
    search_prefixes.push_back("/usr/lib/jvm");                    // centos6
    search_prefixes.push_back("/usr/lib64/jvm");                  // opensuse 13
    search_prefixes.push_back("/usr/local/lib/jvm/default-java"); // alt ubuntu / debian distros
    search_prefixes.push_back("/usr/local/lib/jvm/java");         // alt rhel6
    search_prefixes.push_back("/usr/local/lib/jvm");              // alt centos6
    search_prefixes.push_back("/usr/local/lib64/jvm");            // alt opensuse 13
    search_prefixes.push_back("/usr/lib/jvm/default");            // alt centos
    search_prefixes.push_back("/usr/java/latest");                // alt centos

    for (int i = 8; i < 20; i++)
    {
        std::stringstream ss;
        ss << i;
        std::string version = ss.str();

        search_prefixes.push_back("/usr/lib/jvm/java-" + version + "-openjdk-amd64");
        search_prefixes.push_back("/usr/local/lib/jvm/java-" + version + "-openjdk-amd64");
        search_prefixes.push_back("/usr/lib/jvm/java-" + version + "-oracle");
        search_prefixes.push_back("/usr/local/lib/jvm/java-" + version + "-oracle");
    }

    char *env_value = NULL;
    if ((env_value = getenv("JAVA_HOME")) != NULL)
    {
        search_prefixes.insert(search_prefixes.begin(), env_value);
    }
    else
    {
        std::string out;
        executeCmdEx("java -XshowSettings:properties -version", out);

        std::string jhome_pat = "java.home = ";
        int jhome_pos = out.find(jhome_pat.c_str()) + jhome_pat.length();
        int end_pos = out.find("\n", jhome_pos);

        std::string java_env = out.substr(jhome_pos, end_pos - jhome_pos);
        search_prefixes.insert(search_prefixes.begin(), java_env);

        if (!djava_home.empty())
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

int try_dlopen(std::vector<std::string> potential_paths, void *&out_handle, bool from_file_java_paths)
{
    std::ostream &os = err_rep.progress_os;
    std::string java_bin_path;

    for (std::vector<std::string>::iterator it = potential_paths.begin(), en = potential_paths.end(); it != en; ++it)
    {
        std::string p_path = *it;
        if (p_path != "")
        {
            os << "Trying potential path \"" << p_path << "\" [" << std::endl;
            out_handle = dlopen(p_path.c_str(), RTLD_NOW | RTLD_LOCAL);
            java_bin_path = p_path;
        }

        if ((out_handle != 0) && (!from_file_java_paths))
        {
            std::ofstream file_java_paths;
            file_java_paths.open(path_to_java_paths);

            if (file_java_paths.is_open())
                file_java_paths << "jvm=" << p_path << std::endl;
            file_java_paths.close();

            break;
        }
    }

    if (out_handle == 0)
    {
        os << "]\nerror open library";
        return 0;
    }

    os << "]" << std::endl;

    std::vector<std::string> suffixes = get_search_suffixes();
    for (int i = 0; i < suffixes.size(); i++)
        if (!suffixes.at(i).empty())
            java_bin_path = replaceFirstOccurrence(java_bin_path, suffixes.at(i) + "/libjvm.so", "/bin/");

    std::stringstream path_env;
    path_env << getenv("PATH") << ";" << java_bin_path.c_str();
    setenv("PATH", path_env.str().c_str(), 1);

    return 1;
}

SharedLibraryHandle openJvmLibrary(bool isClient, bool isServer)
{

    (void)isClient;
    (void)isServer;
    void *handler = 0;

    handler = checkParameters(false);
    if (handler != 0)
        return handler;

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

    handler = checkParameters(true);
    if (handler != 0)
        return handler;

    std::vector<std::string> paths = get_potential_libjvm_paths();
    if (try_dlopen(paths, handler, false))
    {
        return static_cast<SharedLibraryHandle>(handler);
    }
    else
    {
        std::ostringstream os;
        os << "dlopen failed with " << dlerror() << ".";

        return checkInputDialog();
    }
}

SharedLibraryHandle checkParameters(bool from_file)
{
    void *handler = 0;

    if (djvm != "")
    {
        std::vector<std::string> paths;
        paths.push_back(djvm.c_str());

        if (try_dlopen(paths, handler, from_file))
            return static_cast<SharedLibraryHandle>(handler);
    }

    if (djava_home != "")
    {
        std::vector<std::string> paths = get_potential_libjvm_paths_from_path(djava_home);
        if (try_dlopen(paths, handler, false))
            return static_cast<SharedLibraryHandle>(handler);
    }

    return handler;
}

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

std::string file_separator()
{
    return "/";
}

std::string other_file_separator()
{
    return "\\";
}

std::wstring wfile_separator()
{
    return L"/";
}

std::wstring wother_file_separator()
{
    return L"\\";
}

std::string extension_exe_file()
{
    return "";
}

std::string runnable_command()
{
    return "'" + getSelfPath() + "'";
}

static void deferToHotSpotExceptionHandler() {}

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

    setenv("XDG_CURRENT_DESKTOP", "NONE", 1);

    std::string separator = ":";
    std::string paths("-Djava.class.path=");
    std::string tmp_path = getSelfPath();

    int app_pid = getpid();
    app_exe_path = getSelfPath();
    bin_dir = dirname((char *)tmp_path.c_str());
    app_dir = bin_dir + file_separator() + "..";

    paths.append(app_dir + file_separator() + "RedExpert.jar");
    paths.append(separator);
    paths.append(app_dir + "/createDesktopEntry.sh");
    paths.append(separator);
    paths.append(app_dir + "/redexpert.desktop");
    paths.append(separator);

    DIR *dir;
    struct dirent *ent;

    std::string lib_dir(app_dir + file_separator() + "lib");
    if ((dir = opendir(lib_dir.c_str())) != NULL)
    {
        while ((ent = readdir(dir)) != NULL)
        {
            std::string conv_file(ent->d_name);
            if (conv_file.rfind("fbplugin-impl", 0) == 0 || conv_file.rfind("fbclient", 0) == 0 || conv_file.rfind("jaybird", 0) == 0)
                continue;

            paths.append(lib_dir + "/" + ent->d_name);
            paths.append(separator);
        }

        closedir(dir);
    }
    else
    {
        err_rep.reportFatalException(std::system_error(ENOENT, std::generic_category(), "No library directory found"));
        exit(EXIT_FAILURE);
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