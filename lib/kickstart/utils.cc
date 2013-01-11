/*
    Authors: Daniel (dmilith) Dettlaff, Michał (tallica) Lipski
    © 2011-2013 - VerKnowSys
*/

#include "core.h"


int isSymlink(const char *path) {
    struct stat st;

    if (lstat(path, &st) < 0) {
        cerr << "Calling lstat() on ‘" << path << "’ failed." << endl;
        return 0;
    }

    return S_ISLNK(st.st_mode) == 1;
}


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
        string::size_type position = input.find(toBeEscaped[ind]);
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


void parse(char *line, char **argv) {
    while (*line != '\0') {
        while (*line == ' ' || *line == '\t' || *line == '\n') *line++ = '\0';
        *argv++ = line;
        while (*line != '\0' && *line != ' ' && *line != '\t' && *line != '\n') line++;
    }
    *argv = NULL;
}


/* int checkifexecutable(const char *filename)
 *
 * Return non-zero if the name is an executable file, and
 * zero if it is not executable, or if it does not exist.
 */

int checkifexecutable(const char *filename) {
     int result;
     struct stat statinfo;

     result = stat(filename, &statinfo);
     if (result < 0) return 0;
     if (!S_ISREG(statinfo.st_mode)) return 0;

     if (statinfo.st_uid == geteuid()) return statinfo.st_mode & S_IXUSR;
     if (statinfo.st_gid == getegid()) return statinfo.st_mode & S_IXGRP;
     return statinfo.st_mode & S_IXOTH;
}


/* int findpathof(char *pth, const char *exe)
 *
 * Find executable by searching the PATH environment variable.
 *
 * const char *exe - executable name to search for.
 *       char *pth - the path found is stored here, space
 *                   needs to be available.
 *
 * If a path is found, returns non-zero, and the path is stored
 * in pth.  If exe is not found returns 0, with pth undefined.
 */

int findpathof(char *pth, const char *exe) {
     char *searchpath;
     char *beg, *end;
     int stop, found;
     int len;

     if (strchr(exe, '/') != NULL) {
      if (realpath(exe, pth) == NULL) return 0;
      return  checkifexecutable(pth);
     }

     searchpath = getenv("PATH");
     if (searchpath == NULL) return 0;
     if (strlen(searchpath) <= 0) return 0;

     beg = searchpath;
     stop = 0; found = 0;
     do {
      end = strchr(beg, ':');
      if (end == NULL) {
           stop = 1;
           strncpy(pth, beg, PATH_MAX);
           len = strlen(pth);
      } else {
           strncpy(pth, beg, end - beg);
           pth[end - beg] = '\0';
           len = end - beg;
      }
      if (pth[len - 1] != '/') strncat(pth, "/", 1);
      strncat(pth, exe, PATH_MAX - len);
      found = checkifexecutable(pth);
      if (!stop) beg = end + 1;
     } while (!stop && !found);

     return found;
}
