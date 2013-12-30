define(function() {

  return function(utils) {

    var log = utils.log;

    // Just call the different log functions, these are just output and
    // not recorded by the test framework
    log.info('Start of Log Tests');
    log.error('Testing error display');
    // debug does not show up, probably because of some bug or because
    // I did not have logging configured correctly
    log.debug('Debugging some interesting bug');
    log.warn('I warn you, the trace message will probably be hidden');
    log.trace('This message will probably hidden');

    // We can also use ansi colors!
    log.info('\033[31mOne\033[0m \033[32mTwo\033[0m \033[33mThree\033[0m \033[34mFour\033[0m')
  };
});
