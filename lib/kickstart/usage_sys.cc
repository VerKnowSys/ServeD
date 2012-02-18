/*
    Author: Daniel (dmilith) Dettlaff
    © 2012 - VerKnowSys
*/

#include "core.h"


extern "C" {


    #define ord(c) ((int)(unsigned char)(c))


    const char* addr_to_string(struct sockaddr_storage *ss) {
    	char buffer2[INET6_ADDRSTRLEN];
    	struct sockaddr_in6 *sin6;
    	struct sockaddr_in *sin;
    	struct sockaddr_un *sun;
        stringstream out;
    	switch (ss->ss_family) {
        	case AF_LOCAL:
        		sun = (struct sockaddr_un *)ss;
                out << sun->sun_path;
        		break;
        	case AF_INET:
        		sin = (struct sockaddr_in *)ss;
                out << inet_ntoa(sin->sin_addr) << ":" << ntohs(sin->sin_port);
        		break;
            case AF_INET6:
                sin6 = (struct sockaddr_in6 *)ss;
        		if (inet_ntop(AF_INET6, &sin6->sin6_addr, buffer2, sizeof(buffer2)) != NULL)
                    out << buffer2 << "." << ntohs(sin6->sin6_port);
                break;
        	default:
                out << "*:*";
        		break;
    	}
        return out.str().c_str();
    }


    static const char* protocol_to_string(int domain, int type, int protocol) {
    	switch (domain) {
        	case AF_INET:
        	case AF_INET6:
        		switch (protocol) {
        		case IPPROTO_TCP:
        			return ("TCP");
        		case IPPROTO_UDP:
        			return ("UDP");
        		case IPPROTO_ICMP:
        			return ("ICM");
        		case IPPROTO_RAW:
        			return ("RAW");
        		case IPPROTO_SCTP:
        			return ("SCT");
        		case IPPROTO_DIVERT:
        			return ("IPD");
        		default:
        			return ("IP?");
        		}
        	case AF_LOCAL:
        		switch (type) {
        		case SOCK_STREAM:
        			return ("UDS");
        		case SOCK_DGRAM:
        			return ("UDD");
        		default:
        			return ("UD?");
        		}
        	default:
        		return ("?");
    	}
    }


    const char* procstat_files(struct procstat *procstat, struct kinfo_proc *kipp) {
    	struct sockstat sock;
    	struct filestat_list *head;
    	struct filestat *fst;
        stringstream out;

    	head = procstat_getfiles(procstat, kipp, 0);
    	if (head == NULL)
    		return "ERROR: HEAD NULL";

    	STAILQ_FOREACH(fst, head, next) {
            if (fst->fs_type == PS_FST_TYPE_SOCKET) { // only sockets
    			if (procstat_get_socket_info(procstat, fst, &sock, NULL) != 0) {
                    return "ERROR PROCSTAT GET SOCKET";
    				break;
    			}
                if ((sock.dom_family == AF_INET) || (sock.dom_family == AF_INET6)) { // only INET domain
                    out << protocol_to_string(sock.dom_family, sock.type, sock.proto) << " ";
                    if (sock.dom_family == AF_LOCAL) {
                        struct sockaddr_un *sun = (struct sockaddr_un *)&sock.sa_local;
                        if (sun->sun_path[0] != 0)
                            out << addr_to_string(&sock.sa_local);
                        else
                            out << addr_to_string(&sock.sa_peer);
                    } else {
        				out << addr_to_string(&sock.sa_local) << "-" << addr_to_string(&sock.sa_peer);
                    }
                    out << "#";
                }
            }
    	}
        procstat_freefiles(procstat, head);
        return out.str().c_str();
    }


    const char* getProcessUsage(int uid, bool consoleOutput) {

        int count = 0;
        char** args = NULL;
        string command, output;

        kvm_t* kd = kvm_open(NULL, NULL, NULL, O_RDONLY, NULL);
        if (kd == 0) {
            if (consoleOutput)
                cerr << "Error initializing kernel descriptor!" << endl;
            return (char*)"KDERR";
        }

        kinfo_proc* procs = kvm_getprocs(kd, KERN_PROC_UID, uid, &count); // get processes directly from BSD kernel
        if (count <= 0) {
            if (consoleOutput)
                cerr << "No processes for given UID!" << endl;
            return (char*)"NOPCS";
        }

        if (consoleOutput) {
            cout << "Process count: " << count << ". Owner UID: " << uid << endl;
            cout << setiosflags(ios::left)
                << setw(6) << "| NO:"
                << setw(27) << "| NAME:"
                << setw(52) << "| CMD:"
                << setw(10) << "| PID:"
                << setw(10) << "| PPID:"
                << setw(10) << "| RSS:"
                << setw(10) << "| MRSS:"
                << setw(16) << "| RUN-TIME(ms):"
                << setw(12) << "| BLK-IN:"
                << setw(12) << "| BLK-OUT:"
                << setw(6) << "| THR:"
                << setw(6) << "| PRI-NRML:"
                << endl;
        }

        for (int i = 0; i < count; ++i) {
            stringstream out;
            command = "";
            args = kvm_getargv(kd, procs, 0);
            for (int y = 0; (args != 0) && (args[y] != 0); y++)
                if (y == 0)
                    command = string(args[y]);
                else
                    command += " " + string(args[y]);

            unsigned int cnt = 0;
            struct procstat* procstat = procstat_open_sysctl();
            struct kinfo_proc *kproc = procstat_getprocs(procstat, KERN_PROC_PID, procs->ki_pid, &cnt);
            string netinfo = "";
            if (cnt != 0)
                netinfo = procstat_files(procstat, kproc);
            procstat_freeprocs(procstat, kproc);
            procstat_close(procstat);
            procstat = NULL;
            kproc = NULL;

            if (consoleOutput) {
                out << setiosflags(ios::left)
                    << "| " << setw(4) << (i + 1)
                    << "| " << setw(25) << (procs->ki_comm)
                    << "| " << setw(50) << (command)
                    << "| " << setw(8) << (procs->ki_pid)
                    << "| " << setw(8) << (procs->ki_ppid)
                    << "| " << setw(8) << (procs->ki_rssize * 4)
                    << "| " << setw(8) << (procs->ki_rusage.ru_maxrss * 4)
                    << "| " << setw(14) << (procs->ki_runtime / 1000)
                    << "| " << setw(10) << (procs->ki_rusage.ru_inblock)
                    << "| " << setw(10) << (procs->ki_rusage.ru_oublock)
                    << "| " << setw(4) << (procs->ki_numthreads)
                    << "| " << setw(6) << ord(procs->ki_pri.pri_level)
                    << "| " << netinfo
                    << endl;
            } else {
                out << (procs->ki_pid)
                    << "|" << (procs->ki_ppid)
                    << "|" << (procs->ki_comm)
                    << "|" << (command)
                    << "|" << (procs->ki_rssize * 4)
                    << "|" << (procs->ki_rusage.ru_maxrss * 4)
                    << "|" << (procs->ki_runtime / 1000)
                    << "|" << (procs->ki_rusage.ru_inblock)
                    << "|" << (procs->ki_rusage.ru_oublock)
                    << "|" << (procs->ki_numthreads)
                    << "|" << ord(procs->ki_pri.pri_level)
                    << "|" << netinfo
                    << endl;
            }

            args = NULL;
            output += out.str();
            procs++;
        }

        kvm_close(kd);
        return output.c_str();
    }


} // extern


