#ifndef REPORT_FATAL_ERROR_VIA_GUI_H_included
#define REPORT_FATAL_ERROR_VIA_GUI_H_included

/**
 * Functions and helpers for displaying errors in graphical mode
 */

static const int NOT_SUPPORTED_ARCH=1;

#ifdef _WIN32
#define USE_MESSAGE_BOX 1
#include <windows.h>
#include "utils.h"
#else
#define USE_MESSAGE_BOX 0

#include <gtk/gtk.h>

void gtkMessageBox(const char *title, const char *message)
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

#endif

#include <iostream>
#include <fstream>
#include <sstream>

inline std::string getCurrentDateTime()
{
    time_t now = time(0);
    struct tm  tstruct;
    char  buf[80];
    tstruct = *localtime(&now);
    strftime(buf, sizeof(buf), "%Y-%m-%d %X", &tstruct);
    return std::string(buf);
}

inline void printErrorToLogFile(const char* log_file, const std::string& app_mess)
{    
    std::ofstream ofs(log_file, std::ofstream::out | std::ofstream::app);
    if(ofs)
    {
        ofs << getCurrentDateTime();
        ofs << std::endl;
        ofs << app_mess;
        ofs << std::endl;
    }
    if(ofs.bad())    //bad() function will check for badbit
    {
        std::cerr << "Writing to log file failed!" << std::endl;
    }
}

inline void reportFatalErrorViaGui(const std::string& programName, const std::string& applicationMessage, std::string supportAddress,int typeError)
{
    const char* log_file = 0;
    std::string path;
#ifdef __linux__
    char* tmp_env = NULL;
    if ((tmp_env = getenv("TMPDIR")) != NULL)
    {
        path.append(tmp_env);
    }
    else
    {
#ifdef P_tmpdir
        path.append(P_tmpdir);
#endif
    }
    if (path.length() == 0)
        path.append("/tmp/");
    path.append("/red_expert.log");
    log_file = path.c_str();
#else
    DWORD dw_ret = 0;
    TCHAR t_path[MAX_PATH];    
    //  Gets the temp path env string (no guarantee it's a valid path).
    dw_ret = GetTempPath(MAX_PATH,          // length of the buffer
                           t_path); // buffer for path
    if (dw_ret > MAX_PATH || (dw_ret == 0))
    {
        std::cerr << "GetTempPath failed";
        log_file = "red_expert.log";
    }
    else
    {
        std::wstring w_path = std::wstring(t_path);
        path = utils::convertUtf16ToUtf8(w_path);
        path.append("\\red_expert.log");
        log_file = path.c_str();
    }

#endif
    if (supportAddress.empty())
    {
        supportAddress = std::string("rdb.support@red-soft.ru");
    }
    std::string arch;
    #if INTPTR_MAX == INT32_MAX
    arch = "x86";
    #elif INTPTR_MAX == INT64_MAX
    arch = "amd64";
    #endif
    std::ostringstream os;
    os << applicationMessage;
    os << std::endl;
    os << "Please copy this message to the clipboard with Ctrl-C and mail it to " << supportAddress << ".";
    os << std::endl;
    std::string platformMessage(os.str());
    std::string m_mes("Launch error because Java not found.");
    if(typeError==NOT_SUPPORTED_ARCH)
    {
        m_mes.append(" This application need in java with arch: ");
        m_mes.append(arch);
        m_mes.append(".");
    }
    m_mes.append(" Please, check ");
    m_mes.append(log_file);
    m_mes.append(" for error details.");
    m_mes.append("You just need to run the application once with the parameter -Djava_home=java_path,");
    m_mes.append("where java_path is the path to Java. And the application will remember the path");
#if USE_MESSAGE_BOX
    printErrorToLogFile(log_file, platformMessage);
    MessageBox(GetActiveWindow(), utils::convertUtf8ToUtf16(m_mes).c_str(), utils::convertUtf8ToUtf16(programName).c_str(), MB_OK);
#else
    (void)programName;
#endif
    std::cerr << platformMessage;
    printErrorToLogFile(log_file, platformMessage);
#ifdef __linux__
    gtkMessageBox(programName.c_str(), m_mes.c_str());
#endif
}

inline void reportFatalErrorViaGui(const std::string& programName, const std::string& applicationMessage)
{
    reportFatalErrorViaGui(programName, applicationMessage, "",0);
}

#endif
