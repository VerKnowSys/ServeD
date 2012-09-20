/*
    Author: Michał (tallica) Lipski
    © 2012 - VerKnowSys
*/

#include "core.h"

extern "C" {

    bool isSymlink(const char *path) {
        struct stat st;

        if (lstat(path, &st) < 0) {
            cerr << "Calling lstat() on ‘" << path << "’ failed." << endl;
            return false;
        }

        return S_ISLNK(st.st_mode) == 1;
    }

}
