/*
 * pstree: display a process heirarchy.
 */

#include "config.h"
#include "ptree.h"
#include "svd.h"

#include <stdio.h>
#include <pwd.h>
#include <sys/types.h>
#include <unistd.h>

#if HAVE_LIBGEN_H
# include <libgen.h>
#endif

#if !HAVE_BASENAME
char*
basename(char *p) {
    char *ret = strrchr(p, '/');
    return ret ? ret+1 : p;
}
#endif
#include <string.h>


/* extern int printcard(char *,...);
extern void ejectcard();
extern void cardwidth(); */

int showargs = 0;	/* -a:  show the entire command line */
int compress = 1;	/* !-c: compact duplicate subtrees */
int clipping = 1;	/* !-l: clip output to screenwidth */
int sortme   = 1;	/* !-n: sort output */
int showpid  = 0;	/* -p:  show process ids */
int showuser = 1;	/* -u:  show username transitions */
int exposeargs = 0;	/* -s:  expand spaces inside arguments to \040 */


DataStructure * data;

/* 2010-10-13 23:45:51 - dmilith - NOTE: helper to create dynamic data structure */
DataStructure* createDataStructure(char* processName, unsigned int pid) {
    DataStructure* aProcess = malloc(sizeof(DataStructure));
    if (NULL != aProcess){
        aProcess->processName = processName;
        aProcess->pid = pid;
        aProcess->next = NULL;
    }
    return aProcess;
}


/* 2010-10-13 23:45:51 - dmilith - NOTE: helper to add process to dynamic data structure */
DataStructure* addProcessToDataStructure(DataStructure *data, char* processName, unsigned int pid) {
    DataStructure* newProcess = createDataStructure(processName, pid);
    if (NULL != newProcess) {
        newProcess->next = data;
    }
    return newProcess;
}


Proc * sibsort(Proc *);
int cmpchild(Proc *a, Proc *b);


/* do a more macosish sort;  try to not pay attention to case when sorting.
 */
int
cmp(Proc *a, Proc *b)
{
    int rc = strcasecmp(a->process, b->process);

    if ( rc == 0 )
	rc = strcmp(a->process, b->process);

    if ( rc == 0 )
	rc = cmpchild(a, b);

    if ( rc == 0 )
	return a->pid - b->pid;

    return rc;
}


/*compare two ->child trees
 */
int cmpchild(Proc *a, Proc *b)
{
    int rc;

    if ( a && b ) {
	    if ( a->children != b->children )
    	    return a->children - b->children;

    	if ( !a->sorted ) {
    	    a->child = sibsort(a->child);
    	    a->sorted = 1;
    	}
    	if ( !b->sorted ) {
    	    b->child = sibsort(b->child);
    	    b->sorted = 1;
    	}

    	a = a->child;
    	b = b->child;

    	while ( a && b ) {
    	    if ((rc = cmp(a,b)))
        		return rc;
    	    a = a->sib;
    	    b = b->sib;
    	}
    }
    if ( a == b)
        return 0;
    else if ( a )
        return 1;
    else
        return -1;
}


/* sort the sibling list by ripping into two substrings, sorting
 * each substring, then stitching them together with a merge sort.
 */
Proc *
sibsort(Proc *p)
{
    Proc *d, *tail, *left = 0, *right = 0;
    int even = 0;

    if ( !(p && p->sib) ) return p;

    /* split into two lists */
    while (p) {
    	d = p;
    	p = p->sib;

    	if (even) {
    	    d->sib = left;
    	    left = d;
    	} else {
    	    d->sib = right;
    	    right = d;
    	}
    	even = !even;
    }

    /* sort them */
    if ( left ) left = sibsort(left);
    if ( right ) right = sibsort(right);

    /* merge them together */
    for ( p = tail = 0; left && right;  ) {
    	if (cmp(left,right) < 0) {
    	    d = left;
    	    left = left->sib;
    	} else {
	        d = right;
    	    right = right->sib;
    	}
	    if ( p )
    	    tail->sib = d;
    	else
    	    p = d;
    	tail = d;
    }
    tail->sib = left ? left : right;

    return p;
}


/* compare two process trees (for subtree compaction)
 * process trees are identical if
 *     (a) the processes are the same
 *     (b) their ->child trees are identical
 *     (c) [if required] all of their siblings are identical
 */
int
sameas(Proc *a, Proc *b, int walk)
{
    if ( ! (a && b) )
	return (a == b);

    if ( a->pid == b->pid )
	return 1;

    if ( strcmp(a->process, b->process) != 0 )
	return 0;

    if ( showargs ) {
	if ( a->renamed != b->renamed )
	    return 0;

	if ( S(a->cmdline) != S(b->cmdline) )
	    return 0;
	if ( memcmp(T(a->cmdline), T(b->cmdline), S(a->cmdline)) )
	    return 0;
    }

    if ( !sameas(a->child, b->child, 1) )
	return 0;

    if ( walk ) do {
	if ( !sameas(a->sib, b->sib, 0) )
	    return 0;
	a = a->sib;
	b = b->sib;
    } while ( a && b );

    return 1;
}


/* print() a subtree, indented by a header.
 */
void
calculate(int first, int count, Proc *node)
{
    int index;
    data = addProcessToDataStructure(data, node->process, node->pid);
    
    if ( node->child ) {
    	if ( sortme && !node->sorted ) {
    	    node->child = sibsort(node->child);
    	    node->sorted = 1;
    	}
    	node = node->child;
    } else
        return;

    count = 0;
    first = 1;
    index = 0;
    
    do {
    	if ( compress && sameas(node, node->sib, 0) )
    	    count++;
    	else {
    	    calculate(first,count,node);
    	    count=first=0;
    	}
        index++;
    } while ((node = node->sib));
}


/*
 * @author dmilith
 * Converts DataStructure to char*
 */
char*
extractString(DataStructure* given) {
    DataStructure* iter = NULL;
    char buffer[5+1];
    char* breakp = ",";
    char* endp = "/";
    char* result = malloc(strlen("\0") + 1);
    
    for (iter = given; NULL != iter; iter = iter->next) {
        char * concat = malloc(
            strlen((char*)iter->processName) + 
            strlen(breakp) + 
            (sizeof buffer) +
            strlen(endp) +
            1);
        if (concat) {
            strcpy(concat, iter->processName);
            strcat(concat, breakp);
            sprintf(buffer, "%d", (unsigned int)iter->pid);
            strcat(concat, buffer);
            strcat(concat, endp);
            
            result = realloc(result, strlen(result) + strlen(concat) + 1 + 2);
            if (result) {
                strcat(result, concat);
            }
        }
        free(concat);
    }
    
    return result;
}


char*
processes(int comp__, int sort__) {

    Proc *init;
        
    showpid  = 1; 
    showuser = 1;
    exposeargs = 1;
    sortme = sort__;
    showuser = 1;

    data = createDataStructure("root", 0);


    if (comp__ == 0) {
        compress = 1; /* don't show userland threads */
    } else {
        compress = 0; /* show userland threads */
    }
    
    init = ptree(0);

    if ( !init ) {
      return "NONE";
    }

    calculate(1,0,init);
    
    return extractString(data);
}
