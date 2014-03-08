var queryString = window.location.search;
// get the testName from the url
var testName = queryString.split("test=")[1];

define(['jquery', 'jasmine', 'JasmineReporter'], function($, jasmine, jasmineReporter) {

  var jasmineEnv = jasmine.getEnv();
  jasmineEnv.addReporter(jasmineReporter);

  $(function(){
    require([testName], function(){
      jasmineEnv.execute();
    });
  });
});
