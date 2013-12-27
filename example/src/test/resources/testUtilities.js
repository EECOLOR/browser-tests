define([ 'jquery' ], function($) {

  /*
   * The server is quite strict, we need to specify json as a content type
   */
  function post(url, data) {
    data = JSON.stringify(data);
    //console.log(data);
    $.ajax({
      type : 'POST',
      url : url,
      data : data,
      contentType : 'application/json'
    });
  }

  function postMsg(url, msg, stack) {
    post(url, {message : msg, stack: stack});
  }

  function log(type) {
    // note: stack is only available for trace
    return function(msg, stack) {
      postMsg('/log/' + type, msg, stack);
    };
  }

  function event(type) {
    // note: stack is only available for error and failure
    return function (msg, stack) {
      var newMsg = msg;
      var newStack = stack;
      if (typeof msg != 'string') {
        newMsg = getMsg(msg);
        if (stack == undefined) {
          newStack = getStack(msg);
        }
      }
      if (!(stack instanceof Array) && stack != undefined) {
        newStack = getStack(stack);
      }

      if (newStack != undefined) newStack = newStack.slice(0, 5);

      postMsg('/event/' + type, newMsg, newStack);
    };
  }

  // You would probably need a library to extract the correct stacks
  function getStack(e) {
    var stack = [];
    if (e.stack) {
      var stackLines = e.stack.split('\n');
      for (var i in stackLines) {
        var stackLine = stackLines[i];
        stackLine = stackLine.replace(new RegExp("http://.+?:\\d+"), "");
        var parts = stackLine.split('@');
        var allParts = parts[parts.length - 1].split(':');
        allParts.unshift(parts[0]);
        if (allParts[1].length == 0) allParts[1] = "[unknown method]"
        stack.push({
          declaringClass : '[unknown class]',
          methodName : allParts[0],
          fileName : allParts[1],
          lineNumber : parseInt(allParts[2])
        });
      }
    }
    return stack;
  }

  function getMsg(e) {
    return e.message || "" + e;
  }

  return {
    log : {
      info : log('info'),
      error : log('error'),
      debug : log('debug'),
      warn : log('warn'),
      trace : log('trace')
    },
    event : {
      error : event('error'),
      failure : event('failure'),
      succeeded : event('succeeded'),
      skipped : event('skipped'),
      pending : event('pending'),
      ignored : event('ignored'),
      canceled : event('canceled')
    }
  };
});
