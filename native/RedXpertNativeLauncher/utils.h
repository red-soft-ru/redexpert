#ifndef UTILS_H
#define UTILS_H

#include <sstream>
#include <stdexcept>
#include <string>
#include <iomanip>

#ifdef _WIN32
#include <exception>
#include <windows.h>
#endif

/**
 * String functions for convenience
 */
namespace utils
{
    // Based on code by Kevlin Henney, shown in "Exceptional C++ Style".
    template <typename T>
    inline std::string toString(const T& value)
    {
      std::stringstream interpreter;
      if (!(interpreter << value))
      {
        throw std::runtime_error("bad lexical cast");
      }
      std::string result = interpreter.str();
      return result;
    }

    inline std::string chomp(const std::string& input)
    {
        if (input.empty() == false && input[input.size() - 1] == '\n')
        {
            return input.substr(0, input.size() - 1);
        }
        return input;
    }

#ifdef _WIN32

    struct WindowsError : std::runtime_error
    {
    private:
        static std::string makeErrorMessage(const std::string& description, DWORD errorCode)
        {
            return description + " failed with Windows error code " + toString(errorCode);
        }

    public:
        WindowsError(const std::string& description, DWORD errorCode)
            : std::runtime_error(makeErrorMessage(description, errorCode))
        {
        }
    };

    inline std::string convertUtf16ToUtf8(const std::basic_string<WCHAR>& input)
    {
        std::string output;
        // WideChartoMultiByte fails for empty input.
        if (input.empty())
        {
            return output;
        }
        int rc = WideCharToMultiByte(CP_ACP, 0, &input[0], input.size(), 0, 0, 0, 0);
        if (rc == 0)
        {
            DWORD lastError = GetLastError();
            throw WindowsError("initial, sizing, call to WideCharToMultiByte() failed", lastError);
        }
        output.resize(rc);
        rc = WideCharToMultiByte(CP_ACP, 0, &input[0], input.size(), &output[0], output.size(), 0, 0);
        if (rc == 0)
        {
            DWORD lastError = GetLastError();
            throw WindowsError("second, converting, call to WideCharToMultiByte() failed", lastError);
        }
        return output;
    }

    inline std::basic_string<WCHAR> convertUtf8ToUtf16(const std::string& input)
    {
        std::basic_string<WCHAR> output;
        // MultiByteToWideChar fails for empty input.
        if (input.empty())
        {
            return output;
        }
        int rc = MultiByteToWideChar(CP_ACP, 0, &input[0], input.size(), 0, 0);
        if (rc == 0)
        {
            DWORD lastError = GetLastError();
            throw WindowsError("initial, sizing, call to MultiByteToWideChar() failed", lastError);
        }
        output.resize(rc);
        rc = MultiByteToWideChar(CP_ACP, 0, &input[0], input.size(), &output[0], output.size());
        if (rc == 0)
        {
            DWORD lastError = GetLastError();
            throw WindowsError("second, converting, call to MultiByteToWideChar() failed", lastError);
        }
        return output;
    }

    inline char* strerror_r(int errnum, char*, size_t)
    {
        return strerror(errnum);
    }

#endif

    template <class ValueInitializer, class Container>
    inline typename Container::value_type join(const ValueInitializer& separatorInitializer, const Container& container)
    {
      typename Container::value_type separator(separatorInitializer);
      typename Container::value_type joined;
      if (container.empty())
      {
        return joined;
      }
      typename Container::const_iterator it = container.begin();
      joined = *it;
      ++ it;
      while (it != container.end())
      {
        joined.insert(joined.end(), separator.begin(), separator.end());
        joined.insert(joined.end(), it->begin(), it->end());
        ++ it;
      }
      return joined;
    }

//    void reportArgValues(std::ostream& os, char const* const* argValues)
//    {
//        os << "arguments [";
//        os << std::endl;
//        while (*argValues != 0) {
//            os << "\"";
//            os << *argValues;
//            ++ argValues;
//            os << "\"";
//            os << std::endl;
//        }
//        os << "] arguments";
//        os << std::endl;
//    }
}

#endif // UTILS_H
