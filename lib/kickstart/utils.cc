/*
    Author: Daniel (dmilith) Dettlaff
    © 2011-2012 - VerKnowSys
*/

#include "core.h"


// #ifdef DEVEL

    // int getdir (string dir, vector<string> &files) {
    //     DIR *dp;
    //     struct dirent *dirp;
    //     if((dp = opendir(dir.c_str())) == NULL) {
    //         cerr << "Error opening directory: " << dir << endl;
    //         return DIRECTORY_OPEN_ERROR;
    //     }
    //
    //     while ((dirp = readdir(dp)) != NULL) {
    //         files.push_back(string(dirp->d_name));
    //     }
    //     closedir(dp);
    //     return 0;
    // }

// #endif


void log_message(string message) {
    FILE *logfile;
    logfile = fopen(INTERNAL_LOG_FILE, "a+");
    if (!logfile) {
        exit(DIAGNOSTIC_LOG_ERROR);
    }
    fprintf(logfile, (char*)"%s\n", message.c_str());
    fclose(logfile);
}


// bool processAlive(pid_t pid) {
//     if (kill(pid, 0) != -1) { /* pid as first param, signal 0 determines no real action, but error checking is still performed */
//         return true;
//     }
//     return false;
// }


string currentDir() {
   char temp[MAXPATHLEN];
   return (getcwd(temp, MAXPATHLEN) ? string( temp ) : string(""));
}


string escape(string input) {
    vector<string> toBeEscaped;
    toBeEscaped.push_back(" ");
    toBeEscaped.push_back(":");
    toBeEscaped.push_back(";");
    toBeEscaped.push_back("/");
    toBeEscaped.push_back("#");
    toBeEscaped.push_back("\\");
    toBeEscaped.push_back("'");
    toBeEscaped.push_back("\"");
    for (unsigned int ind = 0; ind < toBeEscaped.size(); ind++) {
        int position = input.find(toBeEscaped[ind]);
        while (position != string::npos) {
            input.replace(position, 1, "_");
            position = input.find(toBeEscaped[ind], position + 1);
        }
    }
    return input;
}


bool fileExists(string strFilename) {
    struct stat stFileInfo;
    bool blnReturn;
    int intStat;

    intStat = stat(strFilename.c_str(), &stFileInfo);
    if(intStat == 0) {
        blnReturn = true;
    } else {
        blnReturn = false;
    }

    return blnReturn;
}


void defaultSignalHandler(int sig) {
	switch(sig) {
    	case SIGQUIT:
    	case SIGINT:
    	case SIGTERM:
    		cerr << "SIGTERM/INT (terminate) signal catched. Quitting" << endl;
    		// string rmCmd = "/bin/rm " + string(LOCK_FILE);
    		// system(rmCmd.c_str());
            unlink(LOCK_FILE);
    		exit(0);
    		break;

	}
}


vector<string> split(const string& s, const string& delim, const bool keep_empty = true) {
    vector<string> result;
    if (delim.empty()) {
        result.push_back(s);
        return result;
    }
    string::const_iterator substart = s.begin(), subend;
    while (true) {
        subend = search(substart, s.end(), delim.begin(), delim.end());
        string temp(substart, subend);
        if (keep_empty || !temp.empty()) {
            result.push_back(temp);
        }
        if (subend == s.end()) {
            break;
        }
        substart = subend + delim.size();
    }
    return result;
}


std::string escapeJsonString(const std::string& input) {
    std::ostringstream ss;
    // C++11:
    // for (auto iter = input.cbegin(); iter != input.cend(); iter++) {
    // C++98/03:
    for (std::string::const_iterator iter = input.begin(); iter != input.end(); iter++) {
        switch (*iter) {
            case '\\': ss << "\\\\"; break;
            case '"': ss << "\\\""; break;
            // case '/': ss << "\\/"; break;
            case '\b': ss << "\\b"; break;
            case '\f': ss << "\\f"; break;
            case '\n': ss << "\\n"; break;
            case '\r': ss << "\\r"; break;
            case '\t': ss << "\\t"; break;
            default: ss << *iter; break;
        }
    }
    return ss.str();
}