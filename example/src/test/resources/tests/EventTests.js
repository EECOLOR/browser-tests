define(function() {

  return function(utils) {

    var event = utils.event;

    // These events will be counted by the sbt test runner

    event.error('A superb error without stacktrace');
    event.failure('We could not succeed here, so we failed...');
    event.succeeded('Ahh, yes, we succeeded');
    event.skipped('Skipping this one');
    event.pending('Not sure why, but this is pending...');
    event.ignored('I did not like this one, so I ignored it');
    event.canceled('Hmmm, jup, canceled');
    try {
      giveMeAStackTracePlease();
    } catch(e) {
      event.error('It probably depends on your browser if you get to see a stacktrace on this one', e);
      event.failure('We failed, but we might have a stack available', e);
    }
  };
});