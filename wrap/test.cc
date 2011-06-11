#include "svd_wrap.h"

/* example in case of standalone building (no dylib/so mode) */
int main() {
    
    cout << spawn(501, (char*)"/bin/cat /private/var/log/AvConnect.log", (char*)"/var/tmp/OUT_TEST_LOG") << endl; // NOTE: this wont work cause uid 75 don't have access to /var/log/kernel.log

    cout << spawn(75, (char*)"/bin/sleep 50", (char*)"/dev/null") << endl;
    
    cout << spawn(75, (char*)"/bsdggdgsgsdgfdsgsdfdin/ls -la -m", (char*)"/var/tmp/OUT_TEST_LSLAM") << endl;
    
    cout << spawn(501, (char*)"/opt/postgresql9/bin/initdb -D /srv/temp", (char*)"/var/tmp/INITDB_TEST") << endl;
    
    return 0;
}