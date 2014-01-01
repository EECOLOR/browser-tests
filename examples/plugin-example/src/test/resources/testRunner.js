var queryString = window.location.search;
// get the testName from the url
var testName = queryString.split("test=")[1]

// let requirejs handle loading the test
define(['jquery'], function($) {

  function post(url, msg) {

    var info = {
      type : 'POST',
      url : url,
      contentType : 'application/json'
    };

    if (msg) {
      info["data"] = JSON.stringify({message : msg});
    }

    $.ajax(info);
  }


   post('/log/info', 'Test runner running test ' + testName);
   post('/event/done');
});
