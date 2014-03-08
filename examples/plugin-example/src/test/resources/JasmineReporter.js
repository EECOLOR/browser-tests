define([ 'jquery', 'TestOutput' ], function($, testOutput) {

  var currentSuite = null;

  var reporter = {

    reportRunnerStarting : function(runner) {
      currentSuite = null;
      info('');
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

      if (results.passed()) {
        testOutput.succeeded('  ' + successIcon() + ' ' + description);
      } else {
        error(' ' + failureIcon() + ' ' + description);

        $.each(results.getItems(), displayResult);
      }
    },

    reportSuiteResults : function(suite) {
      var results = suite.results();

      info('');
      var title = 'Total for suite ' + suite.description;
      var message = results.totalCount + ' specs, ' + results.failedCount
          + ' failure';

      if (results.passedCount != results.totalCount) {
        error(title);
        errorWithInfoColor(message);
      } else {
        info(title);
        infoWithInfoColor(message);
      }
      info('');
    },

    reportRunnerResults : function(runner) {
      testOutput.done();
    },
    log : info
  };

  var errorColor = '\u001b[31m';
  var successColor = '\u001b[32m';
  var infoColor = '\u001b[34m';
  var resetColor = '\u001b[0m';

  function info(str) {
    testOutput.log.info(str);
  }

  function infoWithInfoColor(str) {
    info(withColor(infoColor, str));
  }

  function errorWithInfoColor(str) {
    error(withColor(infoColor, str));
  }

  function error(str) {
    testOutput.log.error(str);
  }

  function withColor(color, message) {
    return color + message.split('\n').join(resetColor + '\n' + color) + resetColor;
  }

  function failureIcon() {
    return withColor(errorColor, 'x');
  }
  function successIcon() {
    return withColor(successColor, '+');
  }

  function displayResult(index, result) {
    switch (result.type) {

    case 'log':
      info('    ' + result);
      break;
    case 'expect':
      if (!result.passed()) {
        var stack = getScriptStack(result.trace.stack);

        if (stack.length == 0)
          testOutput.failure(' ' + result.message);
        else
          testOutput.error(' ' + result.message, stack);
      }
      break;
    }
  }

  function getScriptStack(stack) {
    if (stack instanceof String) {
      var stackTracePattern = /^(.+?) ([^ ]+\.js):(\d+).*?$/;

      var stackArray = stack.split('\n');
      return $.map(stackArray, function(value) {
        var match = value.match(stackTracePattern);
        if (match) {
          var fileName = match[1];
          var lineNumber = match[2];
          return {
            fileName : fileName,
            lineNumber : parseInt(lineNumber, 10)
          };
        } else
          throw new Error('Stack element did not match pattern. Value: '
              + value);
      });
    } else
      return [];
  }

  return reporter;
});
