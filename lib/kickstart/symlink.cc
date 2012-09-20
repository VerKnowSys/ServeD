/*
    Author: Michał (tallica) Lipski
    © 2012 - VerKnowSys
*/

#include "core.h"

extern "C" {

    int isSymlink(const char *path) {
        struct stat st;

        if (lstat(path, &st) < 0) {
            cerr << "Calling lstat() on ‘" << path << "’ failed." << endl;
            return 0;
        }

        return S_ISLNK(st.st_mode) == 1;
    }

}
