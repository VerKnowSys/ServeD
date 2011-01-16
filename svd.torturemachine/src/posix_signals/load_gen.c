#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <pthread.h>


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
         long double a, b, c = rand() * 100;
         long double d, e, f = rand() * 100;
         res = pow((a*d)/(f-c)+(b*e)/(a-d)+(c*f)/(a-b-c-d-e-f)*3.14345792 + rand() * sqrt(3543.434343*a/b/c/d/a/b/c/d/a), 3600) *
         pow((a*d)/(f-c)+(b*e)/(a-d)+(c*f)/(a-b-c-d-e-f)*3.14345792 + rand() * sqrt(3543.434343*a/b/c/d/a/b/c/d/a), 3600);
     }
     return NULL;
}
