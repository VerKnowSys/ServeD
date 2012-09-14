/*
    Author: Michał (tallica) Lipski
    © 2012 - VerKnowSys
*/

#include "core.h"

#define NS 1000000000

extern "C" {

  int adjustSystemTime(double offset) {
    timespec ts;

    clock_gettime(CLOCK_REALTIME, &ts);

    long current_time  = (ts.tv_sec * NS) + ts.tv_nsec;
    long adjusted_time = current_time + (offset * NS);
    long nsec          = adjusted_time % NS;
    ts.tv_sec          = (adjusted_time - nsec) / NS;
    ts.tv_nsec         = nsec;

    #ifdef DEVEL
    cout << "Offset time: " << offset << " s" << endl;
    cout << "Current time: " << current_time << " ns; Adjusted time: " << adjusted_time << " ns" << endl;
    #endif

    return ! clock_settime(CLOCK_REALTIME, &ts);
  }

}
