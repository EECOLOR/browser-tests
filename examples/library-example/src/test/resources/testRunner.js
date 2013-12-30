var queryString = window.location.search;
// get the testName from the url
var testName = queryString.split("=")[1]

// let requirejs handle loading the test
define([ 'jquery', 'testUtilities', testName ], function($, utils, test) {

  utils.log.info("\033[32mTest runner running test '" + testName + "'\033[0m");
  utils.log.info("");

  try {
    test(utils);
  } catch (e) {
    utils.event.error(e);
  }

  utils.log.info("");
  utils.log.info("\033[36mTest '" + testName + "' complete\033[0m");
  utils.log.info("");

  /*
   * This is not strictly nescessary, but speeds up testing because the
   * framework does not have to wait on the idleTimeout
   */
  utils.done()
});
