/* global NATIVE */
function pluginSend(evt, params) {
  "use strict";

  NATIVE.plugins.sendEvent("FreshchatPlugin", evt,
    JSON.stringify(params || {}));
}

function pluginOn(evt, next) {
  "use strict";

  NATIVE.events.registerHandler(evt, next);
}

function invokeCallbacks(list) {
  "use strict";

  var args = Array.prototype.splice.call(arguments, 1),
    i = 0,
    len = list.length,
    next;

  // For each callback,
  for (i = 0; i < len; ++i) {
    next = list[i];

    // If callback was actually specified,
    if (next) {
      // Run it
      next.apply(null, args);
    }
  }
}

exports = new (Class(function () {
  "use strict";

  var unread_cb = [];

  this.init = function() {
    pluginOn("freshchatUnreadCount", function(evt) {
      invokeCallbacks(unread_cb, evt);
    });
  };

  this.setName = function (first_name, last_name) {
    pluginSend("setName", {
      first_name: first_name,
      last_name: last_name
    });
  };

  this.setEmail = function (email) {
    pluginSend("setEmail", {email: email});
  };

  this.setExternalId = function (id) {
    pluginSend("setExternalId", {id: id});
  };

  this.addMetaData = function (name, value) {
    if (typeof value !== "string") {
      value = JSON.stringify(value);
    }
    pluginSend("addMetaData", {field_name: name, value: value});
  };

  this.clearUserData = function () {
    pluginSend("clearUserData", {});
  };

  this.showConversations = function () {
    pluginSend("showConversations", {});
  };

  this.showFAQs = function () {
    pluginSend("showFAQs", {});
  };

  this.getUnreadCountAsync = function (cb) {
    unread_cb.push(cb);
    pluginSend("getUnreadCountAsync", {});
  };

}))();
