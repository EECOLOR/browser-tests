define([ 'jquery' ], function($) {

  var currentSuite = null;

  var reporter = {

    reportRunnerStarting : function(runner) {
      currentSuite = null;
      info("");
    },

    reportSpecStarting : function(spec) {
      if (currentSuite != spec.suite) {
        currentSuite = spec.suite;
        info(currentSuite.description);
      }
    },

    reportSpecResults : function(spec) {
      var results = spec.results();
      var description = spec.description;

      if (results.passed) {
        succeeded("  " + success() + " " + description);
      } else {
        error(" " + failure() + " " + description);

        $.each(results.getItems(), displayResult);
      }
    },

    reportSuiteResults : function(suite) {
      var results = suite.results();

      info("");
      var title = "Total for suite " + suite.description;
      var message =
        results.totalCount + " specs, " + results.failedCount + " failure";

      if (results.passedCount != results.totalCount) {
        error(title);
        errorWithInfoColor(message);
      } else {
        info(title);
        infoWithInfoColor(message);
      }
      info("");
    },

    reportRunnerResults : function(runner) {
      // no need to report
    },
    log : info
  };


  var successColor = ???;
  var errorColor = ???;
  var infoColor = ???;

  function info(str) {
    // testOutput.log.info(str)
  }

  function infoWithInfoColor(str) {
    info(withColor(infoColor, str));
  }

  function errorWithInfoColor(str) {
    error(withColor(infoColor, str));
  }

  function error(str) {
    // testOutput.log.error(str)
  }

  function withColor(color, message) {
    // testOutput.color(message, color)
  }

  function failure() { withColor(errorColor, "x"); }
  function success() { withColor(successColor, "+"); }

  function displayResult(index, result) {
    switch (result.type) {

    case "log":
      info("    " + result);
      break;
    case "expect":
      if (!r.passed()) {
        var stack = getScriptStack(r.trace.stack);

        if (stack.length == 0)
          //testOutput.failure(s"    $message")
        else
          //testOutput.error(s"    $message", stack)
      }
      break;
    }
  }

  function getScriptStack(stack) {
    if (stack instanceof String) {
      var stackTracePattern = "^(.+?) ([^ ]+\\.js):(\\d+).*?$";

      var stackArray = stack.split("\n");
      return $.map(stackArray, function(value) {
        //match regex
        // if not matched, throw javascript exception
        var fileName = ???
        var lineNumber = ???
        return {
          fileName : fileName,
          lineNumber : parseInt(lineNumber, 10)
        };
      });
    } else {
     return [];
    }
  }

  function succeeded(str) {

  }

  return reporter;
});
