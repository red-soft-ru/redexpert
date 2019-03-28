#ifndef REPORT_FATAL_ERROR_VIA_GUI_H_included
#define REPORT_FATAL_ERROR_VIA_GUI_H_included

/**
 * Functions and helpers for displaying errors in graphical mode
 */

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

inline void printErrorToLogFile(const std::string& app_mess)
{
    const char* log_file = "red_expert.log";
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

inline void reportFatalErrorViaGui(const std::string& programName, const std::string& applicationMessage, std::string supportAddress)
{
    if (supportAddress.empty())
    {
        supportAddress = std::string("rdb.support@red-soft.ru");
    }
    std::ostringstream os;
    os << applicationMessage;
#if USE_MESSAGE_BOX
    os << std::endl;
    os << "Please copy this message to the clipboard with Ctrl-C and mail it to " << supportAddress << ".";
    os << std::endl;
    os << "(Windows won't let you select the text but Ctrl-C works anyway.)";
    os << std::endl;
#endif
    std::string platformMessage(os.str());
#if USE_MESSAGE_BOX
    printErrorToLogFile(platformMessage);
    MessageBox(GetActiveWindow(), utils::convertUtf8ToUtf16("Please, check red_expert.log for error details").c_str(), utils::convertUtf8ToUtf16(programName).c_str(), MB_OK);
#else
    (void)programName;
#endif
    std::cerr << platformMessage;
    printErrorToLogFile(platformMessage);
#ifdef __linux__
    gtkMessageBox(programName.c_str(), "Please, check red_expert.log for error details");
#endif
}

inline void reportFatalErrorViaGui(const std::string& programName, const std::string& applicationMessage)
{
    reportFatalErrorViaGui(programName, applicationMessage, "");
}

#endif
