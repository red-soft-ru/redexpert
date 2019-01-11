#include <jni.h>
#include <iostream>
#include <iostream>
#include <stdlib.h>
// C RunTime Header Files
#include <stdio.h>
#include <cstdlib>
#include <malloc.h>
#include <memory.h>
#include <vector>

#ifdef __linux__
#include <dirent.h>
#include <dlfcn.h>
#include <unistd.h>
#include <gtk/gtk.h>
#include <libgen.h>

// Create type for pointer to the JNI_CreateJavaVM function
typedef jint (*CreateJvmFuncPtr) (JavaVM**, void**, JavaVMInitArgs*);

CreateJvmFuncPtr createJvm = NULL;
void* jvm_lib;
char* error;
#else
// Windows Header Files:
#pragma comment(lib, "user32.lib")
#include <windows.h>
#include <ShellAPI.h>
#include <regex>

typedef JNIIMPORT jint(JNICALL *JNI_createJavaVM)(JavaVM **pvm, JNIEnv **env, void *args);
JNI_createJavaVM createJVM = NULL;
HMODULE hJVM = NULL;
#endif

#define CLEAR(x) memset(&x, 0, sizeof(x))

int executeCmdEx(const char* cmd, std::string &result)
{
    char buffer[128];
    int retCode = -1; // -1 if error ocurs.
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
            retCode = pclose(pipe);
#else
            retCode = _pclose(pipe);
#endif
            throw;
        }
#ifdef __linux__
        retCode = pclose(pipe);
#else
        retCode = _pclose(pipe);
#endif
    }
    return retCode;
}

#ifdef __linux__
inline bool fileExists(const std::string& name) {
    return (access( name.c_str(), F_OK) != -1);
}

void gtkMessageBox(char *title, char *message)
{
    GtkWidget *dialog;
    GtkWidget *label;
    GtkWidget *content_area;

    gtk_init(NULL, NULL);

    dialog = gtk_dialog_new_with_buttons(title,
                                         NULL,
                                         GTK_DIALOG_MODAL,
                                         GTK_STOCK_OK,
                                         GTK_RESPONSE_ACCEPT,
                                         NULL);
    gtk_container_set_border_width (GTK_CONTAINER(dialog), 5);
    content_area = gtk_dialog_get_content_area(GTK_DIALOG(dialog));
    gtk_container_set_border_width (GTK_CONTAINER(content_area), 15);

    label = gtk_label_new(message);
    gtk_container_add(GTK_CONTAINER(content_area), label);
    gtk_widget_show(label);

    gtk_dialog_run(GTK_DIALOG(dialog));
    gtk_widget_destroy(dialog);
}

std::string getSelfpath()
{
    char buff[PATH_MAX];
    ssize_t len = ::readlink("/proc/self/exe", buff, sizeof(buff)-1);
    if (len != -1)
    {
        buff[len] = '\0';
        return std::string(buff);
    }
    /* handle error condition */
}

// New method returns pointer to the JNI_CreateJavaVM function
CreateJvmFuncPtr findCreateJvm() {

    char* java_env = getenv("JAVA_HOME");
    std::string jvm_path;
    if (java_env == NULL || strcmp(java_env, "") == 0)
    {
        gtkMessageBox("Application error", "Please, set the JAVA_HOME environment variable and restart the program!");
        exit(EXIT_FAILURE);
    }
    jvm_path = java_env;
    std::string out;
    std::string cmd = java_env;
    cmd.append("/bin/java -version");
    executeCmdEx(cmd.c_str(), out);
    int jver_pos = out.find("\"") + 1;
    int first_dot_pos = out.find(".");
    int second_dot_pos = out.find(".", first_dot_pos + 1);
    std::string str_ver = out.substr(jver_pos, second_dot_pos - jver_pos);
    if(jver_pos)
    {
        double version = atof(str_ver.c_str());
        if (version < 1.9)
        {
            std::string tmp_path = jvm_path;
            tmp_path.append("/jre/lib/amd64/server/libjvm.so");
            if (!fileExists(tmp_path))
                jvm_path.append("/lib/amd64/server/libjvm.so");
            else
                jvm_path.append("/jre/lib/amd64/server/libjvm.so");
        }
        else
            jvm_path.append("/lib/server/libjvm.so");
    }
    else
    {
        gtkMessageBox("Application error", "Cannot find Java version! Cannot determine the version of java from version string.");
        exit(EXIT_FAILURE);
    }

    jvm_lib = dlopen(jvm_path.c_str(), RTLD_LAZY); // Get handle to jvm shared library
    error = dlerror(); //Check for errors on dlopen
    if(jvm_lib == NULL || error != NULL)
    {
        gtkMessageBox("Application error", "Failed to load JVM! Please check to make sure that Java is configured correctly.");
        exit(EXIT_FAILURE);
    }

    // Load pointer to the function within the shared library
    createJvm = (CreateJvmFuncPtr) dlsym(jvm_lib, "JNI_CreateJavaVM");
    error = dlerror();
    if(error != NULL)
    {
        printf("Success JVM creating\n");
    }
    return createJvm;
}
#else

bool fileExists(const std::string& path)
{
    return GetFileAttributesA(path.c_str()) != INVALID_FILE_ATTRIBUTES;
}

int windowsMessageBox(LPCWSTR title, LPCWSTR message)
{
    int msgboxID = MessageBox(
        NULL,
        (LPCWSTR)message,
        (LPCWSTR)title,
        MB_ICONERROR | MB_OK | MB_DEFBUTTON2
    );

    switch (msgboxID)
    {
    case IDOK:
         exit(EXIT_FAILURE);
        break;
    }

    return msgboxID;
}

bool loadJVMLibrary()
{
    char* java_env;
    size_t len;
    errno_t err = _dupenv_s(&java_env, &len, "JAVA_HOME");
    bool use_jhome = true;
    std::string jvm_path;
    if (java_env == NULL || strcmp(java_env, "") == 0)
    {
        windowsMessageBox(L"Application error", L"Please set the JAVA_HOME environment variable and restart the program!");
        exit(EXIT_FAILURE);
    }
    jvm_path = java_env;
    std::string out;
    std::string cmd = "\"";
    cmd.append(java_env);
    cmd.append("\\bin\\java.exe\" -version");
    executeCmdEx(cmd.c_str(), out);
    int jver_pos = out.find("\"") + 1;
    int first_dot_pos = out.find(".");
    int second_dot_pos = out.find(".", first_dot_pos + 1);
    std::string str_ver = out.substr(jver_pos, second_dot_pos - jver_pos);
    if(jver_pos)
    {
        double version = atof(str_ver.c_str());
        if (version < 1.9)
        {
            std::string tmp_path = jvm_path;
            tmp_path.append("\\jre\\bin\\server\\jvm.dll");
            if (!fileExists(tmp_path))
                jvm_path.append("\\bin\\server\\jvm.dll");
            else
                jvm_path.append("\\jre\\bin\\server\\jvm.dll");
        }
        else
            jvm_path.append("\\bin\\server\\jvm.dll");
    }
    else
    {
        windowsMessageBox(L"Application error", L"Cannot determine the version of java from version string");
        exit(EXIT_FAILURE);
    }

    hJVM = LoadLibraryA(jvm_path.c_str());
    if(!hJVM)
    {
        windowsMessageBox(L"Application error", L"Failed to load JVM! Please check to make sure that Java is configured correctly.");
        exit(EXIT_FAILURE);
    }

    createJVM = (JNI_createJavaVM) GetProcAddress(hJVM, "JNI_CreateJavaVM");

    if(createJVM != NULL)
    {
        printf("Success JVM creating\n");
    }

    free(java_env);
}
#endif

std::string itos(int n)
{
   const int max_size = 16;
   char buffer[max_size] = {0};
   sprintf(buffer, "%d", n);
   return std::string(buffer);
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
    app_exe_path = getSelfpath();
    std::string tmp_path = app_exe_path;
    bin_dir = dirname((char*)tmp_path.c_str());
    app_pid = getpid();
#else
    // hide console window
    ::ShowWindow(::GetConsoleWindow(), SW_HIDE);
    HMODULE hModule = GetModuleHandleW(NULL);
    WCHAR path[MAX_PATH];
    GetModuleFileNameW(hModule, path, MAX_PATH);
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
        while ((ent = readdir (dir)) != NULL)
        {
            paths.append(lib_dir + "/" + ent->d_name);
            paths.append(separator);
        }
        closedir (dir);
    }
    else
    {
        exit(EXIT_FAILURE);
    }
#else
    WIN32_FIND_DATA data;
    std::wstring wlib_dir(lib_dir.begin(), lib_dir.end());
    wlib_dir.append(L"\\*");
    HANDLE hFind = FindFirstFile(wlib_dir.c_str(), &data);

    if ( hFind != INVALID_HANDLE_VALUE )
    {
        do
        {
            // convert from wide char to narrow char array
            char buffer[1024];
            char def_char = '\0';
            WideCharToMultiByte(CP_ACP, 0, data.cFileName, -1, buffer, 260, &def_char, NULL);
            std::string conv_file(buffer);
            if (conv_file != "." && conv_file != "..")
            {
                paths.append(lib_dir + "\\");
                paths.append(conv_file);
                paths.append(separator);
            }
        }
        while (FindNextFile(hFind, &data));
        FindClose(hFind);
        int localLength = paths.length();
        paths.resize(paths.length() - 2);
    }
#endif

    int sep_idx = paths.find_last_of(separator);
    paths = paths.substr(0, sep_idx);

    // JavaVM variables
    JavaVM* jvm(0);
    JNIEnv* env(0);
    JavaVMInitArgs jvm_args;
    CLEAR(jvm_args);
    std::vector<JavaVMOption> options;
    CLEAR(options);

    // add to java class path Red Expert jar
    std::string stdString = paths;
    char* class_path = (char*)stdString.c_str();
    JavaVMOption class_opt;
    class_opt.optionString = class_path;
    class_opt.extraInfo = 0;
    options.push_back(class_opt);

    for (int i = 1; i < argc; i++)
    {
        JavaVMOption opt;
        opt.optionString = argv[i];
        opt.extraInfo = 0;
        options.push_back(opt);
    }

    jvm_args.version = JNI_VERSION_1_8;
    jvm_args.options = options.data();
    jvm_args.nOptions = options.size();
    jvm_args.ignoreUnrecognized = JNI_TRUE;

    // try to create java vm
    //New code:
    jint retCrJvm;
#ifdef __linux__
    CreateJvmFuncPtr createJVM = findCreateJvm();
    printf("findCreateJVM() returned 0x%x\n", createJVM);
    retCrJvm = createJVM(&jvm, (void**)&env, &jvm_args);
#else
    loadJVMLibrary();
    retCrJvm = createJVM(&jvm, &env, &jvm_args);
#endif
    //End new code
    if (retCrJvm != JNI_OK)
    {
#ifdef __linux__
        gtkMessageBox("Application error", "Cannot create JVM!");
#else
        windowsMessageBox(L"Application error", L"Cannot create JVM!");
#endif
        exit(EXIT_FAILURE);
    }

    // find class with main method
    jclass class_ = env->FindClass("org/executequery/ExecuteQuery");
    if (class_ == 0)
    {
#ifdef __linux__
        gtkMessageBox("Application error", "Error when start Red Expert. org.executequery.ExecuteQuery class not found!");
#else
        windowsMessageBox(L"Application error", L"Error when start Red Expert. org.executequery.ExecuteQuery class not found!");
#endif
        exit(EXIT_FAILURE);
    }

    // get main method
    jmethodID method_id = env->GetStaticMethodID(class_, "main", "([Ljava/lang/String;)V");
    if (method_id == 0)
    {
#ifdef __linux__
        gtkMessageBox("Application error", "Error when start Red Expert. Main method not found!");
#else
        windowsMessageBox(L"Application error", L"Error when start Red Expert. Main method not found!");
#endif
        exit(EXIT_FAILURE);
    }

    jobjectArray ret;
    int i;

    std::vector<std::string> app_args;

    for (int i = 0; i < argc; i++)
    {
        app_args.push_back(argv[i]);
    }

    app_args.push_back("-exe_path=" + app_exe_path);
    std::string str_pid = itos(app_pid);
    app_args.push_back("-exe_pid=" + str_pid);

    ret = (jobjectArray)env->NewObjectArray(app_args.size(),
                                            env->FindClass("java/lang/String"), env->NewStringUTF(""));

    for (i = 0; i < app_args.size(); i++)
    {
        std::string stdString = app_args.at(i);
        char* arg = (char*)stdString.c_str();
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(arg));
    }

    // run main
    env->CallStaticVoidMethod(class_, method_id, ret);

    // clean
    jvm->DestroyJavaVM();

    exit(EXIT_SUCCESS);
}
