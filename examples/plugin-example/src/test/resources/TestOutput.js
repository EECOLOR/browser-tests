define(function() {

  function postMsg(url, msg, stack) {
    post(url, {
      message : msg,
      stack : stack
    });
  }

  /*
   * The server is quite strict, we need to specify json as a content type
   */
  function post(url, data) {

    var info = {
      type : 'POST',
      url : url,
      contentType : 'application/json'
    };

    if (data) {
      info['data'] = JSON.stringify(data);
    }

    $.ajax(info);
  }

  return {
    done: function() {
      post('/event/done');
    },
    log : {
      info : function(message) {
        postMsg('/log/info', message);
      },
      error : function(message) {
        postMsg('/log/error', message);
      }
    },
    failure : function(message) {
      postMsg('/event/failure', message);
    },
    error : function(message, stack) {
      postMsg('/event/error', message, stack);
    },
    succeeded : function(message) {
      postMsg('/event/succeeded', message);
    }
  };
});
