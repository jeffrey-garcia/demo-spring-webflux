console.log("home.js loaded");

var xmlhttp = new XMLHttpRequest();
xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == XMLHttpRequest.DONE) {
        if (xmlhttp.status == 200) {
            console.log('response: ' + xmlhttp.responseText);
        }
        else if (xmlhttp.status == 400) {
            console.log('There was an error 400');
        }
        else {
            console.log('something else other than 200 was returned: ' + xmlhttp.status);
        }
    }
};
// to fire request from emulator to restful service hosted in localhost machine
xmlhttp.open("GET", "http://localhost:8081/demoEntities", true);
xmlhttp.send();