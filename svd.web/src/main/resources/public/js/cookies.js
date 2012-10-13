// Published on October 31, 2006 for Possibilities
//   ezine. See http://www.willmaster.com/ article titled
//   "Setting and Reading Cookies with JavaScript."
//
////
//
// These functions set and read cookies. See article
//   mentioned above for information and instructions.
//
////  ////  ////  ////

function SetCookie() {
if(arguments.length < 2) { return; }
var n = arguments[0];
var v = arguments[1];
var d = 0;
if(arguments.length > 2) { d = parseInt(arguments[2]); }
var exp = '';
if(d > 0) {
    var now = new Date();
    then = now.getTime() + (d * 24 * 60 * 60 * 1000);
    now.setTime(then);
    exp = '; expires=' + now.toGMTString();
    }
document.cookie = n + "=" + escape(String(v)) + '; path=/' + exp;
} // function SetCookie()

function ReadCookie(n) {
var cookiecontent = new String();
if(document.cookie.length > 0) {
    var cookiename = n+ '=';
    var cookiebegin = document.cookie.indexOf(cookiename);
    var cookieend = 0;
    if(cookiebegin > -1) {
        cookiebegin += cookiename.length;
        cookieend = document.cookie.indexOf(";",cookiebegin);
        if(cookieend < cookiebegin) { cookieend = document.cookie.length; }
        cookiecontent = document.cookie.substring(cookiebegin,cookieend);
        }
    }
return unescape(cookiecontent);
} // function ReadCookie()
