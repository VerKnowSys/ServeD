
#include "core.h"


#include <sys/cdefs.h>

#include <sys/param.h>
#include <sys/socket.h>
#include <sys/socketvar.h>
#include <sys/sysctl.h>
#include <sys/file.h>
#include <sys/user.h>
#include <sys/un.h>
#include <sys/unpcb.h>
#include <net/route.h>
#include <netinet/in.h>
#include <netinet/in_pcb.h>
#include <netinet/tcp.h>
#include <netinet/tcp_seq.h>
#include <netinet/tcp_var.h>
#include <arpa/inet.h>


int	 opt_4 = 1;		/* Show IPv4 sockets */
int	 opt_6 = 0;		/* Show IPv6 sockets */
int	 opt_c = 1;		/* Show connected sockets */
int	 opt_L = 1;		/* Don't show IPv4 or IPv6 loopback sockets */
int	 opt_l = 1;		/* Show listening sockets */
// int     opt_u;        /* Show Unix domain sockets */
// int     opt_v;        /* Verbose mode */


/*
 * Default protocols to use if no -P was defined.
 */
// const char *default_protos[] = {"tcp", "udp", "divert" };
// size_t default_numprotos = sizeof(default_protos) / sizeof(default_protos[0]);

// int    *protos = NULL;    /* protocols to use */
// size_t     numprotos;    /* allocated size of protos[] */
// int    *ports = NULL;


// #define INT_BIT (sizeof(int)*CHAR_BIT)
// #define SET_PORT(p) do { ports[p / INT_BIT] |= 1 << (p % INT_BIT); } while (0)
// #define CHK_PORT(p) (ports[p / INT_BIT] & (1 << (p % INT_BIT)))

struct sock {
	void *socket;
	void *pcb;
	int vflag;
	int family;
	int proto;
	const char *protoname;
	struct sockaddr_storage laddr;
	struct sockaddr_storage faddr;
	struct sock *next;
};

#define HASHSIZE 1009
struct sock *sockhash[HASHSIZE];
struct xfile *xfiles = NULL;
int nxfiles;


void sockaddr(struct sockaddr_storage *sa, int af, void *addr, int port) {
	struct sockaddr_in *sin4;
	struct sockaddr_in6 *sin6;

	bzero(sa, sizeof *sa);
	switch (af) {
	case AF_INET:
		sin4 = (struct sockaddr_in *)sa;
		sin4->sin_len = sizeof *sin4;
		sin4->sin_family = af;
		sin4->sin_port = port;
		sin4->sin_addr = *(struct in_addr *)addr;
		break;
	case AF_INET6:
		sin6 = (struct sockaddr_in6 *)sa;
		sin6->sin6_len = sizeof *sin6;
		sin6->sin6_family = af;
		sin6->sin6_port = port;
		sin6->sin6_addr = *(struct in6_addr *)addr;
		break;
	default:
		abort();
	}
}


static void gather_inet(int proto) {
	struct xinpgen *xig = NULL, *exig = NULL;
	struct xinpcb *xip = NULL;
	struct xtcpcb *xtp = NULL;
	struct inpcb *inp = NULL;
	struct xsocket *so = NULL;
	struct sock *sock = NULL;
	const char *varname = NULL, *protoname = NULL;
	size_t len = 0, bufsize = 0;
	void *buf = NULL;
	int hash = 0, retry = 0, vflag = 0;

	if (opt_4)
		vflag |= INP_IPV4;
    // if (opt_6)
    //     vflag |= INP_IPV6;

	switch (proto) {
	case IPPROTO_TCP:
		varname = "net.inet.tcp.pcblist";
		protoname = "tcp";
		break;
	case IPPROTO_UDP:
		varname = "net.inet.udp.pcblist";
		protoname = "udp";
		break;
	case IPPROTO_DIVERT:
		varname = "net.inet.divert.pcblist";
		protoname = "div";
		break;
	default:
		cerr << "protocol " << proto << " not supported" << endl;
	}

	bufsize = 8192;
	retry = 5;
	do {
		for (;;) {
			if ((buf = realloc(buf, bufsize)) == NULL)
				cerr << "realloc()" << endl;
			len = bufsize;
			if (sysctlbyname(varname, buf, &len, NULL, 0) == 0) {
                varname = NULL;
                break;
			}
			if (errno == ENOENT)
				goto out;
			if (errno != ENOMEM)
				cerr << "sysctlbyname()" << endl;
			bufsize *= 2;
		}
		xig = (struct xinpgen *)buf;
		exig = (struct xinpgen *)(void *)
		    ((char *)buf + len - sizeof *exig);
		if (xig->xig_len != sizeof *xig || exig->xig_len != sizeof *exig)
			cerr << "struct xinpgen size mismatch" << endl;
	} while (xig->xig_gen != exig->xig_gen && retry--);

    // if (xig->xig_gen != exig->xig_gen && opt_v)
    //     warnx("warning: data may be inconsistent");

	for (;;) {
		xig = (struct xinpgen *)(void *)((char *)xig + xig->xig_len);
		if (xig >= exig)
			break;
		switch (proto) {
		case IPPROTO_TCP:
			xtp = (struct xtcpcb *)xig;
			if (xtp->xt_len != sizeof *xtp) {
				warnx("struct xtcpcb size mismatch");
				goto out;
			}
			inp = &xtp->xt_inp;
			so = &xtp->xt_socket;
			break;
		case IPPROTO_UDP:
		case IPPROTO_DIVERT:
			xip = (struct xinpcb *)xig;
			if (xip->xi_len != sizeof *xip) {
				warnx("struct xinpcb size mismatch");
				goto out;
			}
			inp = &xip->xi_inp;
			so = &xip->xi_socket;
			break;
		default:
			cerr << "protocol " <<  proto << " not supported" << endl;
		}
		if ((inp->inp_vflag & vflag) == 0)
			continue;
		if (inp->inp_vflag & INP_IPV4) {
			if ((inp->inp_fport == 0 && !opt_l) ||
			    (inp->inp_fport != 0 && !opt_c))
				continue;
#define __IN_IS_ADDR_LOOPBACK(pina) \
	((ntohl((pina)->s_addr) >> IN_CLASSA_NSHIFT) == IN_LOOPBACKNET)
			if (opt_L &&
			    (__IN_IS_ADDR_LOOPBACK(&inp->inp_faddr) ||
			     __IN_IS_ADDR_LOOPBACK(&inp->inp_laddr)))
				continue;
#undef __IN_IS_ADDR_LOOPBACK
		} else if (inp->inp_vflag & INP_IPV6) {
			if ((inp->inp_fport == 0 && !opt_l) ||
			    (inp->inp_fport != 0 && !opt_c))
				continue;
			if (opt_L &&
			    (IN6_IS_ADDR_LOOPBACK(&inp->in6p_faddr) ||
			     IN6_IS_ADDR_LOOPBACK(&inp->in6p_laddr)))
				continue;
		} else {
            // if (opt_v)
            //     warnx("invalid vflag 0x%x", inp->inp_vflag);
			continue;
		}
        // if ((sock = (struct sock*)calloc(1, sizeof *sock)) == NULL)
        if ((sock = (struct sock*)malloc(sizeof *sock)) == NULL)
			cerr << "malloc()" << endl;
		sock->socket = so->xso_so;
		sock->proto = proto;
		if (inp->inp_vflag & INP_IPV4) {
			sock->family = AF_INET;
			sockaddr(&sock->laddr, sock->family, &inp->inp_laddr, inp->inp_lport);
			sockaddr(&sock->faddr, sock->family, &inp->inp_faddr, inp->inp_fport);
		} else if (inp->inp_vflag & INP_IPV6) {
			sock->family = AF_INET6;
			sockaddr(&sock->laddr, sock->family, &inp->in6p_laddr, inp->inp_lport);
			sockaddr(&sock->faddr, sock->family, &inp->in6p_faddr, inp->inp_fport);
		}
		sock->vflag = inp->inp_vflag;
		sock->protoname = protoname;
		hash = (int)((uintptr_t)sock->socket % HASHSIZE);
		sock->next = sockhash[hash];
		sockhash[hash] = sock;
	}
out:
    free(buf);
}


void getfiles() {
	size_t len;

	if ((xfiles = (struct xfile*)malloc(len = sizeof *xfiles)) == NULL)
		err(1, "malloc()");
	while (sysctlbyname("kern.file", xfiles, &len, 0, 0) == -1) {
		if (errno != ENOMEM)
			cerr << "sysctlbyname()" << endl;
		len *= 2;
		if ((xfiles = (struct xfile*)realloc(xfiles, len)) == NULL)
			cerr << "realloc()" << endl;
	}
	if (len > 0 && xfiles->xf_size != sizeof *xfiles)
		cerr << "struct xfile size mismatch" << endl;
	nxfiles = len / sizeof *xfiles;
    free(xfiles);
}


const string printaddr(int af, struct sockaddr_storage *ss) {
	char addrstr[INET6_ADDRSTRLEN] = { '\0', '\0' };
	struct sockaddr_un *sun = NULL;
	void *addr = NULL; /* Keep compiler happy. */
	int off, port = 0;
    stringstream ou;

	switch (af) {
	case AF_INET:
		addr = &((struct sockaddr_in *)ss)->sin_addr;
		if (inet_lnaof(*(struct in_addr *)addr) == INADDR_ANY)
			addrstr[0] = '*';
		port = ntohs(((struct sockaddr_in *)ss)->sin_port);
		break;
	case AF_INET6:
		addr = &((struct sockaddr_in6 *)ss)->sin6_addr;
		if (IN6_IS_ADDR_UNSPECIFIED((struct in6_addr *)addr))
			addrstr[0] = '*';
		port = ntohs(((struct sockaddr_in6 *)ss)->sin6_port);
		break;
	case AF_UNIX:
		sun = (struct sockaddr_un *)ss;
		off = (int)((char *)&sun->sun_path - (char *)sun);
        ou << sun->sun_path;
        return ou.str();
	}
	if (addrstr[0] == '\0')
		inet_ntop(af, addr, addrstr, sizeof addrstr);

	if (port == 0) {
        ou << addrstr << ":*";
        sun = NULL;
        addr = NULL;
        return ou.str();
    } else {
        ou << addrstr << ":" << port;
        sun = NULL;
        addr = NULL;
        return ou.str();
    }
}


const char* getprocname(pid_t pid) {
	struct kinfo_proc proc;
	size_t len;
	int mib[4];

	mib[0] = CTL_KERN;
	mib[1] = KERN_PROC;
	mib[2] = KERN_PROC_PID;
	mib[3] = (int)pid;
	len = sizeof proc;
	if (sysctl(mib, 4, &proc, &len, NULL, 0) == -1) {
		/* Do not warn if the process exits before we get its name. */
		if (errno != ESRCH)
			warn("sysctl()");
		return ("??");
	}
	return (proc.ki_comm);
}


// const int check_ports(struct sock *s) {
//     int port;
//     if (ports == NULL)
//         return (1);
//     if ((s->family != AF_INET) && (s->family != AF_INET6))
//         return (1);
//     if (s->family == AF_INET)
//         port = ntohs(((struct sockaddr_in *)(&s->laddr))->sin_port);
//     else
//         port = ntohs(((struct sockaddr_in6 *)(&s->laddr))->sin6_port);
//     if (CHK_PORT(port))
//         return (1);
//     if (s->family == AF_INET)
//         port = ntohs(((struct sockaddr_in *)(&s->faddr))->sin_port);
//     else
//         port = ntohs(((struct sockaddr_in6 *)(&s->faddr))->sin6_port);
//     if (CHK_PORT(port))
//         return (1);
//     return (0);
// }


const char* display() {
	struct xfile *xf = NULL;
	struct sock *s = NULL;
	void *p = NULL;
	int hash = 0, n = 0;
    // setpassent(1);
    string result = "";
	for (xf = xfiles, n = 0; n < nxfiles; ++n, ++xf) {
        stringstream out;
		if (xf->xf_data == NULL)
			continue;
		hash = (int)((uintptr_t)xf->xf_data % HASHSIZE);
		for (s = sockhash[hash]; s != NULL; s = s->next)
			if ((void *)s->socket == xf->xf_data)
				break;
		if (s == NULL)
			continue;
        // if (!check_ports(s))
        //     continue;

        out << (u_long)xf->xf_pid << "|";
        out << getprocname(xf->xf_pid) << "|";
        out << (u_long)xf->xf_uid << "|";
        out << s->protoname << "|";

		switch (s->family) {
    		case AF_INET:
    		case AF_INET6:
                out << printaddr(s->family, &s->laddr) << "|" << printaddr(s->family, &s->faddr);
    			break;
            // case AF_UNIX:
            //                 if (s->laddr.ss_len > 0) { /* server */
            //                     out << "|" << printaddr(s->family, &s->laddr);
            //         break;
            //     }
            //     p = *(void **)&s->faddr; /* client */
            //     if (p == NULL)
            //         break;
            //     for (hash = 0; hash < HASHSIZE; ++hash) {
            //         for (s = sockhash[hash]; s != NULL; s = s->next)
            //             if (s->pcb == p)
            //                 break;
            //         if (s != NULL)
            //             break;
            //     }
            //     if (s == NULL || s->laddr.ss_len == 0) {
            //                     out << "|??";
            //                 } else {
            //                     out << "|" << printaddr(s->family, &s->laddr);
            //                 }
            //                 break;
    		default:
    			abort();
		}
        out << endl;
        result += out.str();
	}
    return result.c_str();
}


const char* getSocketUsage() {

    gather_inet(IPPROTO_TCP);
    gather_inet(IPPROTO_UDP);
    getfiles();

	return display();
}

