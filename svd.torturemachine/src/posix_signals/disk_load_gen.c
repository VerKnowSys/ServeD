#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <pthread.h>

/**

@author dmilith

*/


void *print_message_function(void *ptr);


int main (int argc, char const *argv[]) {

    pthread_t thread1, thread2, thread3, thread4, thread5, thread6;
    char *message1 = "Thread 1", *message2 = "Thread 2", *message3 = "Thread 3", *message4 = "Thread 4", *message5 = "Thread 5", *message6 = "Thread 6";

    int ret1, ret2, ret3, ret4, ret5, ret6;
    ret1 = pthread_create(&thread1, NULL, print_message_function, (void*) message1);
    ret2 = pthread_create(&thread2, NULL, print_message_function, (void*) message2);
    ret3 = pthread_create(&thread3, NULL, print_message_function, (void*) message3);
    ret4 = pthread_create(&thread4, NULL, print_message_function, (void*) message4);
    ret5 = pthread_create(&thread5, NULL, print_message_function, (void*) message5);
    ret6 = pthread_create(&thread6, NULL, print_message_function, (void*) message6);

    printf("Ret1: %d, Ret2: %d, Ret3: %d, Ret4: %d, Ret5: %d, Ret6: %d\n", ret1, ret2, ret3, ret4, ret5, ret6);

    pthread_join(thread1, NULL);
    pthread_join(thread2, NULL);
    pthread_join(thread3, NULL);
    pthread_join(thread4, NULL);
    pthread_join(thread5, NULL);
    pthread_join(thread6, NULL);
    
    return 0;
}


void *print_message_function(void *ptr) {
     char *message;
     message = (char*)ptr;
     printf("%s \n", message);
     
     FILE *ff, *ff2, *ff3, *ff4, *ff5, *ff6;
     
     char* d = "234n32jn4kjfn43jk2fn3jk4fnkj34nfjk3n4f2i34fu23j94f823f8572hhf78erh8rdh8vhd8fvh8h23h4fi324ifb3ifbi32b4fi32b4fib32u4fb32u4bfu3y4bf3y24bfuy324ufn3ui4fi3ui5ugn4iugn4iugn4uigb4iu5gbiu54hngui43ng5iu4ngiu54n3giu54ngi3bgyu5b4yugv345vgt4v5ugv34u5vgu435gvuybeyurgbewyurgberuygbweurybgywuerbguweyrvgy425ytv4gtv43t5vy4tvg5y4tvg54ygtv54ygtv54ygt3v45ygtv4y35y7f8sagfd87gf87sgf87sgfgs87adf8as7gf8gwq84hqw87hr4h2384h823n84tn83258g7587g78ehwg7herghrhghhrgghhr7g8ehw8rgh7ew80g7w9eghr789hfduysguyrbewyubrguiweruygiyurewbguyrewgvry4295y4bgyuiewuybgffuznoufbg4285ygveyuwibgurybdsghfudbiguyewvy5gbvw8gbreuywgbfsbughbreuwbgufdjgbuewoygb8435vg74wvgurdsigb4j5ghbjksugev485gvusydgvjdfsvg234n32jn4kjfn43jk2fn3jk4fnkj34nfjk3n4f2i34fu23j94f8bfuy324ufn3ui4fi3ui5ugn4iugn4iugn4uigb4iu5gbiu54hngui43ng5iu4ngiu54n3giu54ngi3bgyu5b4yugv345vgt4v5ugv34u5vgu435gvuybeyurgbewyurgberuygbweuryb3ui5ugn4iugn4iugn4uigb4iu5gbiu54hngui43ng5iu4ngiu54n3giu54ngi3bgyu5b4yugv345vgt4v5ugv34u5vgu435gvuybeyurgbewyurgberuygbweurybgywuerbguweyrvgy425ytv4gtv43t5vy4tvg5y4tvg54ygtv54ygtv54ygt3v45ygtv4y35y7f8sagfd87gf87sgf87sgfgs87adf8as7gf8gwq84hqw87hr4h2384h823n84tn83258g7587g78ehwg7herghrhghhrgghhr7g8ehw8rgh7ew80g7w9eghr789hfduysguyrbewyubrguiweruygiyurewbguyrewgvry4295y4bgyuiewuybgffuznoufbg4285ygveyuwibgurybdsghfudbiguyewvy5gbvw8gbreuywgbfsbughbreuwbgufdjgbuewoygb8435vg74wvgurdsigb4j5ghbjksugev485gvusydgvjdfsvg";
    const char *file1 = "/tmp/RTZ1", *file2 = "/tmp/RTZ2", *file3 = "/tmp/RTZ3", *file4 = "/tmp/RTZ4", *file5 = "/tmp/RTZ5", *file6 = "/tmp/RTZ6";;
     
     
    for (long long i = 0; i < 4895478545845847; ++i) {
         
        ff = fopen(file1, "w");
        ff2 = fopen(file2, "w");
        ff3 = fopen(file3, "w");
        ff4 = fopen(file4, "w");
        ff5 = fopen(file5, "w");
        ff6 = fopen(file6, "w");

            fprintf(ff, "%s", d);
            fprintf(ff2, "%s", d);
            fprintf(ff3, "%s", d);
            fprintf(ff4, "%s", d);
            fprintf(ff5, "%s", d);
            fprintf(ff6, "%s", d);

            fflush(ff);
            fflush(ff2);
            fflush(ff3);
            fflush(ff4);
            fflush(ff5);
            fflush(ff6);

        fclose(ff);
        fclose(ff2);
        fclose(ff3);
        fclose(ff4);
        fclose(ff5);
        fclose(ff6);

        ff = fopen("/dev/urandom", "r");
        ff2 = fopen("/dev/urandom", "r");
        ff3 = fopen("/dev/urandom", "r");
        ff4 = fopen("/dev/urandom", "r");
        ff5 = fopen("/dev/urandom", "r");
        ff6 = fopen("/dev/urandom", "r");

            d = malloc(sizeof(d) + 1);
            fscanf(ff, "%s", d, sizeof(ff));
            fscanf(ff2, "%s", d, sizeof(ff2));
            fscanf(ff3, "%s", d, sizeof(ff3));
            fscanf(ff4, "%s", d, sizeof(ff4));
            fscanf(ff5, "%s", d, sizeof(ff5));
            fscanf(ff6, "%s", d, sizeof(ff6));

        fclose(ff);
        fclose(ff2);
        fclose(ff3);
        fclose(ff4);
        fclose(ff5);
        fclose(ff6);
     }

     return NULL;
}
