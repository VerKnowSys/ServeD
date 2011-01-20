#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <pthread.h>


/**

@author dmilith

*/


void *print_message_function(void *ptr);


int main (int argc, char const *argv[]) {

    pthread_t thread1, thread2;
    char *message1 = "Thread 1";
    char *message2 = "Thread 2";
    int ret1, ret2;
    
    ret1 = pthread_create(&thread1, NULL, print_message_function, (void*) message1);
    ret2 = pthread_create(&thread2, NULL, print_message_function, (void*) message2);
    
    pthread_join(thread1, NULL);
    pthread_join(thread2, NULL);
    
    return 0;
}


void *print_message_function(void *ptr) {
     char *message;
     message = (char*)ptr;
     printf("%s \n", message);
     
     for (long long i = 0; i < 4895478545845847; ++i) {
         long double res = 0;
         long double a = rand() * 1000000000000045900, b = rand() * 1000000000000045900, c = rand() * 1000000000000045900;
         long double d = rand() * 1000000000000045900, e = rand() * 1000000000000045900, f = rand() * 1000000000000045900;
         res = pow((a*d)/1.333333333334567890+(f-c)+(b*e)/(a-d)+(c*f)/1.333333333334567890+(a-b-c-d-e-f)*3.3333333333345678904345792 + rand() * sqrt(3543.434343*a/1.333333333334567890+b/1.333333333334567890+c/1.333333333334567890+d/1.333333333334567890+a/1.333333333334567890+b/1.333333333334567890+c/1.333333333334567890+d/1.333333333334567890+a), 3600) / 1.333333333334567890 +
         pow((a*d)/1.333333333334567890+(f-c)+(b*e)/(a-d)+(c*f)/1.333333333334567890+(a-b-c-d-e-f)*3.14345792 + rand() * 1000000000000045900 * sqrt(3543.434343*a/1.333333333334567890+b/1.333333333334567890+c/1.333333333334567890+d/1.333333333334567890+a/1.333333333334567890+b/1.333333333334567890+c/1.333333333334567890+d/1.333333333334567890+a), 360032323.2934587239456324765283465823734);
     }
     return NULL;
}
