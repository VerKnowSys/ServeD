(*
    @author: dmilith
    © 2012 - VerKnowSys
*)

open Sys;;
open Unix;;
open Printf;;


(* Checks validity of pid *)
let pidValid pid =
    match pid with
        | x when x <= 0 -> false
        | _ ->
            try
                kill pid 0; (* it won't raise exception if pid is alive *)
                true
            with
                | e -> false (* printf "Thrown exception %s" (Printexc.to_string e); *)


(* Performs attack on pid using signal *)
let rec deathWatchPid ?(signal = sigint) ?(sleep_time = 3) pid =
    try
        kill pid signal;
        sleep sleep_time;
        if pidValid pid then
            begin
                printf "Death watch is over pid %d\n%!" pid;
                match signal with
                    | x when x == sigint ->
                        printf "Trying termination\n%!";
                        deathWatchPid ~signal:sigterm ~sleep_time:sleep_time pid

                    | x when x == sigterm ->
                        printf "Trying quit\n%!";
                        deathWatchPid ~signal:sigquit ~sleep_time:sleep_time pid

                    | x when x == sigquit ->
                        printf "Stubborn process should immediately die\n%!";
                        deathWatchPid ~signal:sigkill ~sleep_time:sleep_time pid

                    | _ ->
                        printf "Zombie process detected? pid: %d is still alive!\n%!" pid;
                        deathWatchPid ~signal:sigkill ~sleep_time:sleep_time pid
            end
        else
            printf "Pid %d was terminated.\n%!" pid
    with
        | e ->
            printf "Pid %d is already terminated (or not accessible if belongs to another user).\n%!" pid


