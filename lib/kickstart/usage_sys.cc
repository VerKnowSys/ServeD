/*
    Author: Daniel (dmilith) Dettlaff
    © 2012 - VerKnowSys
*/

#include "core.h"


#include <kvm.h>
#include <sys/sysctl.h>
#include <sys/user.h>
#include <sys/proc.h>
#include <paths.h>
#include <sys/param.h>
#include <sys/capability.h>
#include <sys/socket.h>
#include <sys/sysctl.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <err.h>
#include <libprocstat.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libprocstat.h>


#define ord(c) ((int)(unsigned char)(c))


int	hflag = 1, nflag = 0, Cflag = 0;

static struct cap_desc {
	cap_rights_t	 cd_right;
	const char	*cd_desc;
} cap_desc[] = {
	/* General file I/O. */
	{ CAP_READ,		"rd" },
	{ CAP_WRITE,		"wr" },
	{ CAP_MMAP,		"mm" },
	{ CAP_MAPEXEC,		"me" },
	{ CAP_FEXECVE,		"fe" },
	{ CAP_FSYNC,		"fy" },
	{ CAP_FTRUNCATE,	"ft" },
	{ CAP_SEEK,		"se" },

	/* VFS methods. */
	{ CAP_FCHFLAGS,		"cf" },
	{ CAP_FCHDIR,		"cd" },
	{ CAP_FCHMOD,		"cm" },
	{ CAP_FCHOWN,		"cn" },
	{ CAP_FCNTL,		"fc" },
	{ CAP_FPATHCONF,	"fp" },
	{ CAP_FLOCK,		"fl" },
	{ CAP_FSCK,		"fk" },
	{ CAP_FSTAT,		"fs" },
	{ CAP_FSTATFS,		"sf" },
	{ CAP_FUTIMES,		"fu" },
	{ CAP_CREATE,		"cr" },
	{ CAP_DELETE,		"de" },
	{ CAP_MKDIR,		"md" },
	{ CAP_RMDIR,		"rm" },
	{ CAP_MKFIFO,		"mf" },

	/* Lookups - used to constraint *at() calls. */
	{ CAP_LOOKUP,		"lo" },

	/* Extended attributes. */
	{ CAP_EXTATTR_GET,	"eg" },
	{ CAP_EXTATTR_SET,	"es" },
	{ CAP_EXTATTR_DELETE,	"ed" },
	{ CAP_EXTATTR_LIST,	"el" },

	/* Access Control Lists. */
	{ CAP_ACL_GET,		"ag" },
	{ CAP_ACL_SET,		"as" },
	{ CAP_ACL_DELETE,	"ad" },
	{ CAP_ACL_CHECK,	"ac" },

	/* Socket operations. */
	{ CAP_ACCEPT,		"at" },
	{ CAP_BIND,		"bd" },
	{ CAP_CONNECT,		"co" },
	{ CAP_GETPEERNAME,	"pn" },
	{ CAP_GETSOCKNAME,	"sn" },
	{ CAP_GETSOCKOPT,	"gs" },
	{ CAP_LISTEN,		"ln" },
	{ CAP_PEELOFF,		"pf" },
	{ CAP_SETSOCKOPT,	"ss" },
	{ CAP_SHUTDOWN,		"sh" },

	/* Mandatory Access Control. */
	{ CAP_MAC_GET,		"mg" },
	{ CAP_MAC_SET,		"ms" },

	/* Methods on semaphores. */
	{ CAP_SEM_GETVALUE,	"sg" },
	{ CAP_SEM_POST,		"sp" },
	{ CAP_SEM_WAIT,		"sw" },

	/* Event monitoring and posting. */
	{ CAP_POLL_EVENT,	"po" },
	{ CAP_POST_EVENT,	"ev" },

	/* Strange and powerful rights that should not be given lightly. */
	{ CAP_IOCTL,		"io" },
	{ CAP_TTYHOOK,		"ty" },

#ifdef NOTYET
	{ CAP_PDGETPID,		"pg" },
	{ CAP_PDWAIT4,		"pw" },
	{ CAP_PDKILL,		"pk" },
#endif
};


    static const u_int	cap_desc_count = sizeof(cap_desc) /
			    sizeof(cap_desc[0]);


    void addr_to_string(struct sockaddr_storage *ss, char *buffer, int buflen) {
    	char buffer2[INET6_ADDRSTRLEN];
    	struct sockaddr_in6 *sin6;
    	struct sockaddr_in *sin;
    	struct sockaddr_un *sun;

    	switch (ss->ss_family) {
    	case AF_LOCAL:
    		sun = (struct sockaddr_un *)ss;
    		if (strlen(sun->sun_path) == 0)
    			strlcpy(buffer, "-", buflen);
    		else
    			strlcpy(buffer, sun->sun_path, buflen);
    		break;

    	case AF_INET:
    		sin = (struct sockaddr_in *)ss;
    		snprintf(buffer, buflen, "%s:%d", inet_ntoa(sin->sin_addr),
    		    ntohs(sin->sin_port));
    		break;

    	case AF_INET6:
    		sin6 = (struct sockaddr_in6 *)ss;
    		if (inet_ntop(AF_INET6, &sin6->sin6_addr, buffer2,
    		    sizeof(buffer2)) != NULL)
    			snprintf(buffer, buflen, "%s.%d", buffer2,
    			    ntohs(sin6->sin6_port));
    		else
    			strlcpy(buffer, "-", sizeof(buffer));
    		break;

    	default:
    		strlcpy(buffer, "", buflen);
    		break;
    	}
    }


    const char* stringify_address(struct sockaddr_storage *ss) {
    	char addr[PATH_MAX];
    	addr_to_string(ss, addr, sizeof(addr));
    	return addr;
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


    // static u_int width_capability(cap_rights_t rights) {
    //     u_int count, i, width;
    //     count = 0;
    //     width = 0;
    //     for (i = 0; i < cap_desc_count; i++) {
    //         if (rights & cap_desc[i].cd_right) {
    //             width += strlen(cap_desc[i].cd_desc);
    //             if (count)
    //                 width++;
    //             count++;
    //         }
    //     }
    //     return (width);
    // }


    // static void
    // print_capability(cap_rights_t rights, u_int capwidth)
    // {
    //     u_int count, i, width;
    //
    //     count = 0;
    //     width = 0;
    //     for (i = width_capability(rights); i < capwidth; i++) {
    //         if (rights || i != 0)
    //             printf(" ");
    //         else
    //             printf("-");
    //     }
    //     for (i = 0; i < cap_desc_count; i++) {
    //         if (rights & cap_desc[i].cd_right) {
    //             printf("%s%s", count ? "," : "", cap_desc[i].cd_desc);
    //             width += strlen(cap_desc[i].cd_desc);
    //             if (count)
    //                 width++;
    //             count++;
    //         }
    //     }
    // }


    const char* procstat_files(struct procstat *procstat, struct kinfo_proc *kipp) {
    	struct sockstat sock;
    	struct filestat_list *head;
    	struct filestat *fst;
    	int error;
        stringstream out;

    	head = procstat_getfiles(procstat, kipp, 0);
    	if (head == NULL)
    		return (const char*)"ERROR: HEAD NULL";

    	STAILQ_FOREACH(fst, head, next) {
            if (fst->fs_type == PS_FST_TYPE_SOCKET) {

    			error = procstat_get_socket_info(procstat, fst, &sock, NULL);
                // cout << "SOCK:: " << sock.dom_family << " TYPE: " << sock.type << " PROTO: " << sock.proto << " ";
    			if (error != 0) {
                    return "ERROR PROCSTAT GET SOCKET";
    				break;
    			}
                out << protocol_to_string(sock.dom_family, sock.type, sock.proto) << "|";
    			// printf("%-3s ",
    			//                     );
                if (sock.dom_family == AF_LOCAL) {
                    struct sockaddr_un *sun =
                        (struct sockaddr_un *)&sock.sa_local;

                    if (sun->sun_path[0] != 0)
                        out << stringify_address(&sock.sa_local);
                    else
                        out << stringify_address(&sock.sa_peer);
                } else {
    				out << stringify_address(&sock.sa_local) << "|" << stringify_address(&sock.sa_peer);
                }
            }
    	}
        procstat_freefiles(procstat, head);
        return (const char*)out.str().c_str();
    }


    static int
    kinfo_proc_compare(const void *a, const void *b)
    {
    	int i;

    	i = ((const struct kinfo_proc *)a)->ki_pid -
    	    ((const struct kinfo_proc *)b)->ki_pid;
    	if (i != 0)
    		return (i);
    	i = ((const struct kinfo_proc *)a)->ki_tid -
    	    ((const struct kinfo_proc *)b)->ki_tid;
    	return (i);
    }


    void kinfo_proc_sort(struct kinfo_proc *kipp, int count) {
    	qsort(kipp, count, sizeof(*kipp), kinfo_proc_compare);
    }


extern "C" {


    const char* getProcessUsage(int uid, bool consoleOutput) {

        int count = 0;
        char** args = NULL;
        string command, output;

        kvm_t* kd = kvm_open(NULL, "/dev/null", NULL, O_RDONLY, NULL);
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


