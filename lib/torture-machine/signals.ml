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
let rec deathWatchPid ?(signal = sigint) pid =
    try
        kill pid signal;
        sleep 1;
        if pidValid pid then
            begin
                printf "Death watch is over pid %d\n%!" pid;
                match signal with
                    | x when x = Sys.sigint ->
                        printf "Trying interrupt\n%!";
                        deathWatchPid ~signal:sigterm pid

                    | x when x = Sys.sigterm ->
                        printf "Trying quit\n%!";
                        deathWatchPid ~signal:sigquit pid

                    | x when x = Sys.sigquit ->
                        printf "Stubborn process will die\n%!";
                        deathWatchPid ~signal:sigkill pid

                    | _ ->
                        printf "Zombie process detected? pid: %d\n%!" pid;
                        deathWatchPid ~signal:sigkill pid

            end
        else
            printf "Pid %d was terminated.\n%!" pid
    with
        | _ ->
            printf "Pid %d is already terminated.\n%!" pid


