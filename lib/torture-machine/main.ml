(*
    @author: dmilith
    © 2012 - VerKnowSys
*)


open Unix;;
open Sys;;
open Array;;
open Printf;;
open Signals;;


let arguments = List.tl (to_list Sys.argv);;
(* let rejection a_list = true;; *)
(* let arguments = List.filter rejection prep_args;; *)

let version = "0.1.0" in
let head = "Torture Tool - v" ^ version in
print_endline head;;


let rec go_through_args args results =
    if List.length args <= 0
    then results
    else
        let head = List.hd args in
        let tail = List.tl args in
        let results = results ^ " " ^ head in
        go_through_args tail results;;


(* let mapped_value e = e ^ " " in List.map mapped_value arguments;; *)
(* print_endline (string_of_int (List.length arguments - 2));; *)
let arg_result = go_through_args arguments "";;
(* let mapped_result = mapped_value arg_result *)

(* printf ("Arguments given" ^ arg_result);; *)
(* printf "Signals module\n";; *)

(* let e = Signals.pidValid 123 in
print_endline (string_of_bool e);;

let e = Signals.pidValid (-123) in
print_endline (string_of_bool e);;
 *)

let pid = int_of_string (List.nth arguments 0) in
Signals.deathWatchPid pid;;

(* print_endline (Signals.attackPid -123);; *)
(* print_endline mapped_result;; *)
(* read_line ();; *)
