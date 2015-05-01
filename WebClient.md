# Introduction #

Many users will find it more convenient to view data from your MoniCA server on a web page. The open-monica distribution comes with a set of Javascript libraries to make it easy to build web pages that can display real-time MoniCA data. Alternatively, you can use the pre-built MoniCA frontpage.

The normal operation of a web page using this library would be as follows:
  * Setup the `monicaServer` object, and have it return the list of all points available on the server.
  * Give the server a list of points and time-series that you are interested in, and direct it to call one or more functions when each point's value is updated, or when the time-series has a new point added.
  * Each point will request an update after the amount of time its description states that it will get a new value. The server itself will only query MoniCA after its internal timer expires (thereby setting a minimum time between updates), and if one or more points has requested an update.
  * In each of the callback functions that the server calls, you will get a reference to the point or time-series that has been updated, and you can query its state.

The server also allows you to query the alarm points on the server, but this works in a slightly different way:
  * You tell the `monicaServer` how often to check for alarms. To begin with, the server will not know about any alarm points, since MoniCA usually only reports about the alarms that are currently active.
  * Each time the server queries for the active alarms and finds an alarm it has not previously seen before, it adds it to the list of known alarms, and automatically propagates information about the callbacks set for all the alarms to it.
  * Since the server will only hear about alarms that are currently active at each query, before each query it sets all known alarms to inactive internally.
  * After each query returns, all points that have changed state will fire its callbacks, passing its reference. Each alarm can have its own set of callbacks.
  * In each of the callback functions that the server calls, you will get a reference to the alarm that has been updated, and you can query its state.

If you prefer though, you can make the server obtain a list of all alarms that MoniCA knows about at the start and return a full list of their references. This will allow you to more easily set different callbacks for each alarm, and more predictably design a page using all the alarms. You can then leave the server to query alarms as stated above, or make it query for all alarms at each update. If you choose the latter, you can also make the server call a single function after MoniCA returns with a list of all the alarm references.

Each point, time-series and alarm is itself a Javascript object with some public methods.
  * For points, you can query the current state and set the value (for settable points).
  * For time-series, you can query the entire series, or just the latest state.
  * For alarms, you can query the current state or acknowledge or shelve it.


---


# Installation #

## File Locations ##

### Required Files ###

On your webserver, you will first need to install the Perl client so that it can be called by the server-side MoniCA query script.

You should then copy the `/clientlibs/web/cgi-bin/monicainterface_json.pl` file to your webserver's `cgi-bin` directory.

You have two options for the Javascript style: a "traditional" option based on the Dojo 1.6 toolkit, or an Asynchronous Module Definition (AMD) option based on Dojo 1.7. The programming style required for each implementation is quite different, so the rest of this document will give examples for each.

#### AMD Javascript ####

To use the AMD library, I recommend the following procedure:
  * On your webserver, in the top-level HTML directory, create a `jslib` directory.
  * In this directory, make a `dojo` directory. Into this directory, extract the Dojo 1.9 toolkit (version 1.9.0 can be downloaded [here](http://download.dojotoolkit.org/release-1.9.0/dojo-release-1.9.0.tar.gz)).
  * Rename the directory (in this case `dojo-release-1.9.0`) to `1.9.0`.
  * Make a file called `configure.js` in the `jslib/dojo` directory that will be used by your web apps to correctly identify where other libraries are installed. It should look like:
```
var dojoConfig = {
  baseUrl: "/jslib/",
  tlmSiblingOfDojo: false,
  packages: [
    { name: "dojo", location: "dojo/1.9.0/dojo" },
    { name: "dijit", location: "dojo/1.9.0/dijit" },
    { name: "dojox", location: "dojo/1.9.0/dojox" },
    { name: "atnf", location: "atnf" }
  ]
};
```

Now you will need to download the AMD version of the [ATNF Javascript library](http://code.google.com/p/atcacode/source/browse/#svn%2Ftrunk%2FatnfLibrary%2Fclient-side%2Fatnf) and install it into the `jslib` directory.

Once you have done this, copy the `clientlibs/web/htdocs/atnf/monica.js` from this code-base into the `jslib/atnf` directory.

If you would like to use the Javascript machinery to control points on the MoniCA server, or interact with the alarm functionality, and you require encrypted communication, you will also need to download the `biginteger` library from [here](https://github.com/silentmatt/javascript-biginteger), and put it under `jslib/biginteger`.

#### Traditional Javascript ####

Copy the `/clientlibs/web/htdocs/monica_web_v2.src.js` to your desired directory on your webserver.

Optionally, you can install the Dojo toolkit onto your webserver, from http://dojotoolkit.org/. The open-monica Javascript library is built upon version 1.6 of the Dojo toolkit, and does not use the AMD functionality of version 1.7. Since version 1.7 is backwards compatible however, you can still install it.

### Standard MoniCA HTML front page ###

The included MoniCA frontpage depends on the following tools in addition to those listed above:
  * the [Highcharts plotting library](http://www.highcharts.com/)
  * the [jQuery toolkit](http://jquery.com/) (required by Highcharts)

You can download the latest Highcharts plotting library from [their homepage.](http://www.highcharts.com) Ideally, create a directory `jslib` in the root HTML directory of your HTTP server, and ensure that it contains a link to, or direct copy of, the `highcharts.js` file, such that the address `http://www.yourserver.com/jslib/highcharts.js` would find the script.

Then copy the files `/clientlibs/web/htdocs/monica_front.*` to your desired location on the webserver. You will then need to do some configuration in the `monica_front.js` file.

As this version of the MoniCA library is no longer actively developed, it does not support encrypted communication. To enable this feature, please migrate to the AMD style library.


---


# Programming #

## Web page setup ##

Any webpage that you would like to use this library with will need to include the library files. Assuming you wish to program in HTML5, you might want to use the following templates for your page.

### AMD Style ###

In this style, you just need to configure Dojo 1.7 correctly to include both the ATNF and MoniCA Javascript libraries. It makes for a very simple template:

```
<!DOCTYPE html>
<html>
  <head>
    <title>MoniCA test page</title>
    <script src="/jslib/dojo/configure.js"></script>
    <script data-dojo-config="async: true"
	    src="/jslib/dojo/1.9.0/dojo/dojo.js"></script>
    <script src="/jslib/biginteger/biginteger.js"></script>
  </head>
  <body>

  </body>
</html>
```

### Traditional Style ###

In this style, you will need to explicitly include the MoniCA Javascript library, along with Dojo.

```
<!DOCTYPE html>
<html>
  <head>
    <title>MoniCA test page</title>
    <script src="http://ajax.googleapis.com/ajax/libs/dojo/1.6.0/dojo/dojo.xd.js">
      djConfig={
      parseOnLoad: true,
      isDebug: true,
      baseUrl: "./",
      };
    </script>
    <script src="/jslib/monica_web_v2.src.js"></script>
  </head>
  <body>

  </body>
</html>
```

## API ##

This section describes the MoniCA Javascript API, which is located in the file `monica.js` (AMD-style) or `monica_web_v2.src.js` (traditional style).

The API makes available four methods or objects.


---


### Method/object `monicaServer` ###

#### Constructor ####

##### AMD Style #####

```
require(["atnf/monica", "dojo/domReady!"], function(monica) {
  var server = monica.server(spec);
});
```

##### Traditional Style #####

```
var server = monicaServer(spec);
```

The constructor function takes one argument, which is optional.
`spec` is an object that configures the server. It can have the following properties:
  * `serverName`: the hostname of the MoniCA server (default `monhost-nar`).
  * `protocol`: the protocol to use for connections to the webserver (default `http`).
  * `webserverName`: the hostname of the webserver (default `www.narrabri.atnf.csiro.au`).
  * `webserverPath`: the location of the JSON MoniCA query script (default `cgi-bin/obstools/web_monica/monicainterface_json.pl`).
  * `updateInterval`: the minimum time between queries to the MoniCA server, in milliseconds (default `10000`).
  * `errorCallback`: a function to call whenever an error communicating with the server is encountered.
  * `autoDescriptions`: a boolean value to indicate whether the server should automatically get descriptions for points as they are added (default `false`).
  * `alarmPollPeriod`: the time between queries to the MoniCA server to poll which alarm states are active, in milliseconds (default `0`, ie. do not poll).
  * `alarmCallbacks`: an array of function calls to be made whenever an alarm condition is encountered.
  * `allAlarmCallbacks`: an array of function calls to be made at each poll of the alarms; these functions should accept as an argument an array of all the alarms known to the server.
  * `alarmAuthData`: an object, with the properties `user` and `pass` that should be used to provide the authentication parameters to acknowledge or shelve alarms on the MoniCA server (default: `{ user: '', pass: ''}`). This was a convenience for the old MoniCA that did not support encryption, but should never be used now, since it will usually require the hardcoding of plaintext passwords in the calling Javascript.
  * `requireEncryption`: a Boolean indication as to whether automatic RSA encryption should be performed when sending usernames and passwords to the MoniCA server. If omitted, this defaults to `true`. When set to `true`, all further communications with the MoniCA server will transparently encrypt the username and passwords that will pass over the network.

#### Public Methods ####

##### connect #####

```
var connectionDeferred = server.connect();
```

Before you can start obtaining MoniCA data from a server, you must tell the `monicaServer` object to connect with this method. This connection gives the Javascript instance information about the points available on the MoniCA server, and its persistent RSA key.

This method returns a Dojo `Deferred` object that will be resolved when the connection is established. You should then point the `Deferred` to a function you want to execute once the connection is established, eg:

```
var connectionEstablished = function(serverObj) {
  // This routine sets up all the MoniCA points that will appear on
  // this page.
  console.log(serverObj.server); // The name of the server that has connected.
}

connectionDeferred.then(connectionEstablished);
```

The connection can be simplified for brevity:
```
server.connect().then(connectionEstablished);
```

Use of the Dojo `Deferred` object to detect when the connection is established is the preferred method. However, you may also choose instead to use the Dojo pub/sub interface. When the MoniCA connection is established, the `monicaServer` object will publish the connection to the `connection` channel. All you need to do is setup a listener for this channel.

```
dojo.subscribe('connection', connectionEstablished);
```

The same information is passed to the function regardless of which method is chosen. The difference is that if you set up multiple `monicaServer` objects, the `connection` subscription will be triggered for every server, whereas the you will get a separate `Deferred` for each server that can be directed individually to different functions.

For example, using the `connection` channel:
```
var narrabriConnection = function() {
  // Do things for a connection to the Narrabri MoniCA server.
}

var parkesConnection = function() {
  // Do things for a connection to the Parkes MoniCA server.
}

var connectionEstablished = function(serverObj) {
  // Which server has connected?
  if (serverObj.server === 'monhost-nar') {
    narrabriConnection();
  } else if (serverObj.server === 'monhost-pks') {
    parkesConnection();
  }
}

var narMonica = monicaServer({ serverName: 'monhost-nar' });
var pksMonica = monicaServer({ serverName: 'monhost-pks' });

// The subscription listener should be setup before the connection
// method is called so we don't miss the signal.
dojo.subscribe('connection', connectionEstablished);

narMonica.connect();
pksMonica.connect();
```

Whereas, using Dojo `Deferred` objects:

```
var narrabriConnection = function(serverObj) {
  // Do things for a connection to the Narrabri MoniCA server.
}

var parkesConnection = function(serverObj) {
  // Do things for a connection to the Parkes MoniCA server.
}

var narMonica = monicaServer({ serverName: 'monhost-nar' });
var pksMonica = monicaServer({ serverName: 'monhost-pks' });

narMonica.connect().then(narrabriConnection);
pksMonica.connect().then(parkesConnection);
```

##### pointsList #####

```
var availablePoints = server.pointsList();
```

This method returns an `Array` containing a `String` for each MoniCA point name available on the server you have connected to.

Example:
```
var connectionEstablished = function(serverObj) {
  // Print out each point name to the web page.
  var pointNames = server.pointsList();
  for (var i = 0; i < pointNames.length; i++) {
    dojo.place('<span>point ' + (i + 1) + ' = ' +
      pointNames[i] + '</span>', dojo.body());
  }
}

var server = monicaServer({ serverName: 'monhost-nar' });
server.connect().then(connectionEstablished);
```

##### isPoint #####

```
var pointAvailable = server.isPoint(pointName);
```

This method takes a `String` `pointName` and returns `true` if `pointName` is a point available on the server, and `false` otherwise.

##### getServerName #####

```
var serverName = server.getServerName();
```

This method returns a `String` representation of the name of the server that is connected.

Example:
```
var connectionEstablished = function() {
  // Print out the server name to the web page, which should be 'monhost-nar'
  dojo.place('<span> server = ' +
    server.getServerName() + '</span>', dojo.body());
}

var server = monicaServer({ serverName: 'monhost-nar' });
server.connect().then(connectionEstablished);
```

##### addPoints #####

```
var pointReferences = server.addPoints(pointsList);
```

This method is used to tell the server to keep one or more MoniCA points up-to-date. For each point passed to this method that exists on the server, a `monicaPoint` object that can be used to query that point's details and value. If the point you ask for is not known to the MoniCA server, you will get a `null` reference returned; ensure that you check for such `null` values before trying to use the reference.

Example:
```
var connectionEstablished = function() {
  var pointsRequired = [ 'ca01.misc.station', 'ca02.misc.station' ];
  var pointReferences = server.addPoints(pointsRequired);
}
```

##### removePoints #####

```
server.removePoints(referencesList);
```

This method is used to tell the server to forget about the point references listed in the array `referencesList`. Each element in the array should be a reference to a `monicaPoint` object that you got from a call to `addPoints` on the same server.


##### getPoints #####

```
var pointReferences = server.getPoints(pointsList);
```

This method is used to get references to the `monicaPoint` objects for points that have previously been added with the `addPoints` method. The method can be passed either a single `String` for `pointsList`, or an array of `String`s. Each `String` should be the name of a MoniCA point that you want the reference for.

The return value for this method is as follows:
  * If the method was passed a single `String` - being the name of a MoniCA point - and the `monicaServer` object has only a single `monicaPoint` object related to that point, it will return a single `monicaPoint` reference.
  * If the method was passed a single `String` - being the name of a MoniCA point - and the `monicaServer` object has more than one `monicaPoint` object related to that point (ie. both a point-value and a time-series object), it will return an array of `monicaPoint` references.
  * If the method was passed an array of `String`s - each being the name of a MoniCA point - each element of the return array may be either a single `monicaPoint` reference or an array of `monicaPoint` references, based on the two rules above.
  * For any point that isn't being handled by the `monicaServer` object, a `null` value will be returned.

##### addTimeSeries #####

```
var seriesReference = server.addTimeSeries(seriesOptions);
```

This method makes the server keep track of a number of values for a particular point. The configuration of the time-series is passed through the `seriesOptions` object, that has the properties:
  * `pointName`: the name of the MoniCA point.
  * `timeSeriesOptions`: an object with the properties:
    * `startTime`: the time to start the series from, in the format `yyyy-mm-dd:HH:MM:SS`, or `-1` to make the series finish at the current time (the default).
    * `spanTime`: the time to cover with this series, in minutes (default 60).
    * `maxPoints`: the maximum number of points to keep from the time range (default 500).

This method returns a `monicaPoint` object that can be used to query the series' details and values.

Example:
```
// Setup to get the temperature on the site for the last 12 hours.
// Since temperature varies pretty slowly, we only need 100 points here.
var seriesOptions = {
  pointName: 'site.environment.weather.Temperature',
  timeSeriesOptions: {
    startTime: -1,
    spanTime: 720,
    maxPoints: 100
  }
}

var seriesReference = server.addTimeSeries(seriesOptions);
```

##### addHistoryTable #####

```
var seriesReference = server.addHistoryTable(seriesOptions);
```

This method makes the server keep track of a number of values for a particular point. The configuration of the history table is passed through the `seriesOptions` object, that has the properties:
  * `pointName`: the name of the MoniCA point.
  * `timeSeriesOptions`: an object with the properties:
    * `startTime`: the time to start the series from, in the format `yyyy-mm-dd:HH:MM:SS`, or `-1` to make the series finish at the current time (the default).
    * `spanTime`: the time to cover with this series, in minutes (default 60).
    * `maxPoints`: the maximum number of points to keep from the time range (default 500).

This method returns a `monicaPoint` object that can be used to query the series' details and values.

A history table is different to a time-series only in that the values of two consecutive elements in the array cannot be the same in a history table. The machinery that
ensures this is in the Javascript library, not on the server.

Example:
```
// Setup to get the state of CA01 for the last 12 hours.
// Since temperature varies pretty slowly, we only need 100 points here.
var seriesOptions = {
  pointName: 'ca01.servo.State',
  timeSeriesOptions: {
    startTime: -1,
    spanTime: 720,
    maxPoints: 500
  }
}

var seriesReference = server.addHistoryTable(seriesOptions);
```

##### getDescriptions #####

```
server.getDescriptions(pointReference);
```

This method instructs the server to query MoniCA for the description for the point described by the `pointReference` object, which will have been returned from a call to `addPoints`, `getPoints` or `addTimeSeries`. If you want to get the server to get descriptions for all points that have been added but do not yet have descriptions, call this method without an argument.

This method is required and must be called for each added point and time-series, because the points will not update without a description. Alternatively, you may wish to set the `autoDescriptions` parameter to `true` in the call that instantiates the MoniCA server object (this will still require you to call this method once however).

Continuing on from our previous `addPoints` example:
```
var connectionEstablished = function() {
  var pointsRequired = [ 'ca01.misc.station', 'ca02.misc.station' ];
  var pointReferences = server.addPoints(pointsRequired);
  server.getDescriptions();
}
```

If you want to do something when the server has finished retrieving the descriptions, you may subscribe to the `description` channel. Again extending the example:
```
var descriptionsGathered = function(serverInfo) {
  // The serverInfo object we get here has one property:
  //   server: the name of the server.
  // And now we can do things knowing points on this server have
  // some info for their descriptions.
  console.log('Descriptions have been gathered.');
}

dojo.subscribe('description', descriptionsGathered);

var connectionEstablished = function() {
  var pointsRequired = [ 'ca01.misc.station', 'ca02.misc.station' ];
  var pointReferences = server.addPoints(pointsRequired);
  server.getDescriptions();
}
```


##### requestUpdate #####

```
server.requestUpdate(pointReferences);
```

This method instructs the server to query MoniCA for a new value for each point described by the `pointReferences` array that is passed to it. The server adds the points to its internal list of points requiring updates, but will not actually query MoniCA for these values until its next normal update time, or until a user calls the `immediateUpdate` method.

Users should not normally have to call this method, as the `monicaPoint` objects themselves will call it when they need to.

##### immediateUpdate #####

```
server.immediateUpdate();
```

This method makes the server immediately make a query to MoniCA to get values for all points that have requested an update.

Users should not normally have to call this method.

##### startUpdating #####

```
server.startUpdating();
```

This method instructs the server to begin a timer that will expire every `spec.updateInterval` milliseconds. Each time the timer expires, the server will query MoniCA for new values for each point that has requested an update. If no points have requested an update, no action will be taken by the server.

Users are advised to call this method as soon as the server resolves its loading `Deferred` or has published to the `connection` channel. For example:

```
var connectionEstablished = function(serverObj) {
  // We can safely start the server updating now.
  narMonica.startUpdating();
  // Do things for a connection to the MoniCA server.
}

var narMonica = monicaServer({ serverName: 'monhost-nar' });

narMonica.connect().then(connectionEstablished);
```

##### stopUpdating #####

```
server.stopUpdating();
```

This method stops the server's updating timer, thereby stopping the server from getting any new values for the points that it handles. All the points can still be queried and will return the last value it obtained from MoniCA.

##### getLoadingDeferred #####

```
var connectionDeferred = server.getLoadingDeferred();
```

This method allows you to get the Dojo `Deferred` object that will get resolved when the server has gotten the list of points from MoniCA. The normal way of getting this `Deferred` is from the `connect` method, but this method returns exactly the same value, if required.

##### setPointValue #####

```
var setPromise = server.setPointValue(setDetails);
```

This method allows you to set a MoniCA point's value on the server, in order to make the server do something. It takes a single object `setDetails` which has the following properties:
  * `point`: the name of the point to set.
  * `value`: the value to set the point to.
  * `type`: the type of the value, which is one of the types that the `set` method on the ASCII interface supports (http://code.google.com/p/open-monica/wiki/ClientASCII).
  * `user`: the username required to set the point. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: the password required to set the point. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.

This method returns a Dojo Deferred promise that will resolve when the call to the server has completed.

You should never need to call this method correctly. You should instead use the `setValue` method on the points themselves, and allow the point to call this method internally.

##### acknowledgeAlarm #####

```
var acknowledgePromise = server.acknowledgeAlarm(ackDetails);
```

This method allows you to acknowledge an alarm on the MoniCA server. It takes a single object `ackDetails` which has the following properties:
  * `point`: the name of the alarm to acknowledge.
  * `value`: one of `'true'` or `'false'` to acknowledge or unacknowledge the alarm respectively.
  * `user`: the username required to acknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: the password required to acknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.

This method returns a Dojo Deferred promise that will resolve when the call to the server has completed.

You should never need to call this method correctly. You should instead use one of the `acknowledge`, `unacknowledge` or `autoAcknowledge` methods on the alarms themselves, and allow the alarm to call this method internally.

##### shelveAlarm #####

```
var shelvePromise = server.shelveAlarm(shelveDetails);
```

This method allows you to shelve an alarm on the MoniCA server. It takes a single object `shelveDetails` which has the following properties:
  * `point`: the name of the alarm to shelve.
  * `value`: one of `'true'` or `'false'` to shelve or unshelve the alarm respectively.
  * `user`: the username required to shelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: the password required to shelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.

This method returns a Dojo Deferred promise that will resolve when the call to the server has completed.

You should never need to call this method correctly. You should instead use one of the `shelve`, `unshelve` or `autoShelve` methods on the alarms themselves, and allow the alarm to call this method internally.

##### startAlarmPolling #####

```
server.startAlarmPolling(period);
```

This method starts the periodic polling of the MoniCA server for alarms. It takes a single optional argument, that being the polling period in milliseconds.

This method should be called after the server has connected. For example (this is an AMD example):

```
require(["atnf/monica", "dojo/domReady!"], function(monica) {
  var alarmUpdater = function(alarmRef) {
    // Do something each time an alarm is updated.
  };

  var connectionEstablished = function(serverObj) {
    // We can safely start the server updating now.
    narMonica.startUpdating();
    // And start updating the alarm conditions.
    narMonica.startAlarmPolling();
    // Do things for a connection to the MoniCA server.
  };

  var narMonica = monica.server({
    serverName: 'monhost-nar',
    alarmPollPeriod: 10000,
    alarmCallbacks: [ alarmUpdater ],
    autoDescriptions: true
  });

  narMonica.connect().then(connectionEstablished);
});
```

This example shows how you can set the alarm callback routine and period in the initial call to the `server` method. It is highly recommended that you set the `autoDescriptions` parameter to `true` in the `server` instantiation call if you plan on using the alarm polling functions.

##### stopAlarmPolling #####

```
server.stopAlarmPolling();
```

This method stops the server object from polling the MoniCA server for any further alarms.

##### immediateAlarmPoll #####

```
sever.immediateAlarmPoll();
```

This method makes the server immediately query MoniCA for all points that are alarmed, regardless of how long it has been since the previous alarm check.

##### pollAllAlarms #####

```
var alarmsPromise = server.pollAllAlarms();
```

This method can be used to both get a list of all the alarms that MoniCA knows about, even if they are currently not active, or simply to check the state of all the alarms immediately. It returns a Dojo `Deferred` that will resolve when the list of all alarms returns, and after all the alarm callbacks are made. The function given to the `Deferred` should accept an array of `monicaAlarm` objects as its only argument.

For example (AMD style):
```
require(["atnf/monica", "dojo/domReady!"], function(monica) {
  var printAllAlarmNames = function(alarmList) {
    for (var i = 0; i < alarmList.length; i++) {
      console.log(alarmList[i].getState().pointName);
    }
  };

  var connectionEstablished = function(serverObj) {
    // We can safely start the server updating now.
    narMonica.startUpdating();
    // Get a list of all the alarms that may become active.
    narMonica.pollAllAlarms().then(printAllAlarmNames);
    // And start updating the alarm conditions.
    narMonica.startAlarmPolling();
    // Do things for a connection to the MoniCA server.
  };

  var narMonica = monica.server({
    serverName: 'monhost-nar',
    alarmPollPeriod: 10000,
    alarmCallbacks: [ alarmUpdater ],
    autoDescriptions: true
  });

  narMonica.connect().then(connectionEstablished);
});
```

##### getAlarms #####

```
var alarmRef = server.getAlarms(alarmNames);
```

This method allows the user to get a set of zero or more `monicaAlarm` objects. The `alarmNames` variable that you pass to this method can be a `String` if you want only a single alarm, or an `Array` of `String`s if you want more alarms. What is returned is either a single `monicaAlarm` object if you passed a `String`, or an `Array` with the same number of elements as `alarmNames`. For any alarm name that the server does not recognise, `null` will be returned.

For example:
```
var alarmCheck = function() {
  // This routine gets called periodically without knowing what the
  // state of the server is.
  // We want to check the state of a number of alarms.
  var reqAlarms = [ 'weather.alarm1', 'weather.alarm2' ];
  var alarmRefs = server.getAlarms(reqAlarms);
  for (var i = 0; i < alarmRefs.length; i++) {
    if (alarmRefs[i] !== null &&
        alarmRefs[i].getState().isAlarmed) {
      // Do something if we have an active alarm.
    }
  }
}
```

##### getAllAlarms #####

```
var alarmRefs = server.getAllAlarms();
```

This method returns an array of all the `monicaAlarm` references that it knows about. If a poll of all alarms has been made prior to this call, then this will be a list of all the available MoniCA alarms, otherwise there is no guarantee this list is complete.

##### addAlarmCallback #####

```
server.addAlarmCallback(callbackFn, updateAll);
```

This method adds a callback function to the list of functions that each alarm should call when it gets updated. It takes two arguments, the first of which is the function to call, and the second is either `true` or `false`; if `true`, then all alarms that are known about will have this function added to their list, otherwise only alarms that we find out about later will have this function added to their list.

##### addAllAlarmCallback #####

```
server.addAllAlarmCallback(callbackFn);
```

This method adds a callback function to the list of functions that the server will call each time it polls the server for its alarm states. It takes one argument, being the function to call. This function should accept a single argument, which will be an array of all the `monicaAlarm` references.

##### updateAlarmAuthData #####

```
server.updateAlarmAuthData(authData, updateAll);
```

This method allows the user to change the authentication username and password that will allow for acknowledgement or shelving of MoniCA alarms. It takes two arguments. The first argument is an object with the properties:
  * `user`: the username, as a plain text `String`.
  * `pass`: the password, as a plain text `String`.
The second argument is either `true` or `false`. If `true`, then all alarms that are known about will have their authentication information updated with this new username and password, otherwise only alarms that we find out about later will have this authentication information set.

This is now the preferred way of supplying username and password information to the Javascript library, as it can take these values from user input in a text box at run time. This is far more secure than hard-coding usernames and passwords into the server details object. When the Javascript library has been instructed to do transparent RSA encryption, this authentication data will be automatically encrypted before sending it over the network, with no further actions from the programmer required.


---


### Method/object `monicaPoint` ###

#### Constructor ####

##### AMD Style #####

```
require(["atnf/monica", "dojo/domReady!"], function(monica) {
  var pointReference = monica.point(spec, my);
});
```

##### Traditional Style #####

```
var pointReference = monicaPoint(spec, my);
```

The constructor function takes two arguments, both of which are optional.

  * `spec` is an object that configures the point. It can have the following properties:
    * `pointName`: the MoniCA name for this point, as a `String`.
    * `isTimeSeries`: a `Boolean` indicating whether this point represents a time-series.
    * `timeSeriesOptions`: an object that specifies the options used to gather the time-series. Its properties are:
      * `startTime`: the time to start the series from, in the format `yyyy-mm-dd:HH:MM:SS`, or `-1` to make the series finish at the current time (the default).
      * `spanTime`: the time to cover with this series, in minutes (default 60).
      * `maxPoints`: the maximum number of points to keep from the time range (default 500).
  * `my` is any object that you would like the `monicaPoint` to have access to.

The user can make a new `monicaPoint` if they desire, but the constructor is usually only called by the `monicaServer` object when the user requests a new point or time-series. The `monicaPoint` will not work correctly unless the `my` argument is specified as the `monicaServer` object that will be responsible for updating it.

#### Public Methods ####

##### getPointDetails #####

```
var pointDetails = pointReference.getPointDetails();
```

This method gets the MoniCA name for the `pointReference` object, along with the description of the point and its units. This information is returned as an object with the properties:
  * `name`: the MoniCA point name.
  * `description`: the description for this point, from MoniCA.
  * `units`: the units for the values for this point, from MoniCA.
  * `serverName`: the name of the MoniCA server that this point has come from.

Example:
```
var pointDetails = pointReference.getPointDetails();
console.log('the point ' + pointDetails.name + ' is described ' +
  'as "' + pointDetails.description + '", with units ' +
  pointDetails.units);
```

##### hasDescription #####

```
var descriptionAvailable = pointReference.hasDescription();
```

This method returns a `Boolean` indication of whether MoniCA has supplied a description for this point. This is primarily used by the `monicaServer` object, but can be called by the user if desired.

##### setPointDetails #####

```
pointReference.setPointDetails(pointDetails);
```

This method is used to assign a description and units to the point, as well as the time between updates. These details are passed to this method through the `pointDetails` object which has the properties:
  * `description`: A plain-English description for what this point represents.
  * `units`: The units that the point values are in.
  * `updateTime`: The point will request an update every `updateTime` seconds.

This method is usually called by the `monicaServer` object after communicating with MoniCA after the user has executed the `getDescriptions` method. However, the user can override any of the MoniCA specifications for these details, but only if they call this method before executing `getDescriptions`, as this method will not override any of these details that have previously been set. You do not need to specify all of the details in a call to this method.

Calling this method also begins the update process, as long as the `updateTime` has been specified. Each `monicaPoint` has its own timer, and will request an update when it expires, after `updateTime` seconds.

For example, to set only the `updateTime` to 1 minute, and allow MoniCA to fill in the rest of the details:
```
var connectionEstablished = function() {
  // Add a point.
  var pointReference = server.addPoints([ 'site.environment.weather.Temperature' ]);
  pointReference[0].setPointDetails({
    'updateTime': 60
  });
  server.getDescriptions();
}

var server = monicaServer();
server.connect().then(connectionEstablished);
```

##### resetPointDetails #####

```
pointReference.resetPointDetails();
```

This method allows the user to reset the point details that they may have set, and ask MoniCA to set them again.

##### updateValue #####

```
pointReference.updateValue(newValues);
```

This method is used to set the values for the MoniCA point, via the `newValues` object that is passed to it. This object has the properties:
  * `value`: The new value for this point.
  * `time`: The time for this new value. This time can be in any representation, but by default the MoniCA JSON returns the time for single point values as 'yyyy-mm-dd\_HH:MM:SS' and for time-series values as the Unix time in milliseconds.
  * `errorState`: A `Boolean` indication of whether the state is in error.

Both the `value` and `time` properties must be present in the object for the value to be updated. This method is called by the `monicaServer` object responsible for the point, and is used for both single-valued points and to update time-series with new values, after it has been initialised with the `setTimeSeriesValues` method.

##### setTimeSeriesValues #####

```
pointReference.setTimeSeriesValues(seriesValues);
```

This method is used to initialise a time-series point, via the `seriesValues` object that is passed to it. This object has the properties:
  * `data`: An array of value objects or arrays. For value objects, the properties are:
    * `value`: The new value for this point.
    * `time`: The time for this new value. This time can be in any representation, but by default the MoniCA JSON returns the time for single point values as 'yyyy-mm-dd\_HH:MM:SS' and for time-series values as the Unix time in milliseconds.
    * `errorState`: A `Boolean` indication of whether the state is in error.
  * For value arrays, there must be at least 2 elements, the first being equivalent to the `time` property, and the second being the `value` property. The `errorState` property can be included as the optional third array element.

This method is called by the `monicaServer` object responsible for the point, and will not do anything unless the point is designated as a time-series point.

##### getTimeSeries #####

```
var seriesValues = pointReference.getTimeSeries(options);
```

This method returns all the values for the point as an `Array` of value objects, each of which has the elements:
  * `value`: The value for this point.
  * `time`: The time for this value. This time can be in any representation, but by default the MoniCA JSON returns the time for single point values as 'yyyy-mm-dd\_HH:MM:SS' and for time-series values as the Unix time in milliseconds.
  * `errorState`: A `Boolean` indication of whether the state is in error.

If the MoniCA server is solely responsible for keeping the time-series up-to-date, the `seriesValues` `Array` is guaranteed to be in time order.

The `options` object can be used to alter the values coming back from this method. It currently supports the following properties:
  * `arrayHighcharts`: A `Boolean` indicating whether `seriesValues` should be returned as an `Array` of `Array`s in the format expected by the Highcharts Javascript plotting library. In this format, each element of the `seriesValues` `Array` is a two-element `Array` with the first element being the `time` and the second being the `value`. By default, if this method is called without specifying this property in the `options` object (or if `options` is not passed), this value is set to `true`.
  * `arrayDojo`: A `Boolean` indicating whether `seriesValues` should be returned as an `Array` of objects in the format expected by the Dojox charting library. In this format, each element of the `seriesValues` `Array` is an object with two properties:
    * `x`: the `time`.
    * `y`: the `value`.
> By default, if this method is called without specifying this property in the `options` object (or if `options` is not passed), this value is set to `false`. If you want to set this value to `true`, you must also set `arrayHighcharts` to `false`.
  * `valueAsDecimalDegrees`: Many of the MoniCA points that have units of 'degrees' have their value expressed as a `String` with elements for degrees, minutes and seconds. If this property is specified as `true`, any such value is converted into a decimal number in degrees.
  * `timeAsSeconds`: A `Boolean` indicating whether any `time` returned by this method should be as Unix time in seconds. By default, this is `false`.
  * `timeAsDateObject`: A `Boolean` indicating whether any `time` returned by this method should be as a Javascript `Date`. By default, this is `false`.
  * `referenceValue`: This property can be set to a numeric value that will be subtracted from any `value` being returned by this method. By default, this is 0.
  * `referenceTime`: This property can be set to a numeric value that will be subtracted from any `time` being returned by this method. By default this is 0. Be careful that you set this property in milliseconds by default, or in seconds if you have set `timeAsSeconds` as `true`.
  * `timeRange`: This object can be passed to limit the range of times that will be returned by this method. It has two optional properties:
    * `min`: The minimum time that will be returned. This should be a number, as milliseconds by default, or in seconds if you have set `timeAsSeconds` as `true`.
    * `max`: The maximum time that will be returned. This should be a number, as milliseconds by default, or in seconds if you have set `timeAsSeconds` as `true`.

##### getHistoryTable #####

```
var seriesValues = pointReference.getHistoryTable(options);
```

This routine simply calls the getTimeSeries method internally, so please refer to that method's options for usage.

##### latestValue #####

```
var value = pointReference.latestValue(options);
```

This method returns the only value from a normal point, or the latest value from a time-series point. If the returned value is an object, its properties are:
  * `value`: The value for this point.
  * `time`: The time for this value. This time can be in any representation, but by default the MoniCA JSON returns the time for single point values as 'yyyy-mm-dd\_HH:MM:SS' and for time-series values as the Unix time in milliseconds.
  * `errorState`: A `Boolean` indication of whether the state is in error.

The `options` object can be used to alter the values coming back from this method. It currently supports the following properties:
  * `arrayHighcharts`: A `Boolean` indicating whether `value` should be returned as an `Array` in the format expected by the Highcharts Javascript plotting library. In this format, `value` is a two-element `Array` with the first element being the `time` and the second being the `value`. By default, if this method is called without specifying this property in the `options` object (or if `options` is not passed), and the point is designated as time-series, this value is set to `true`.
  * `arrayDojo`: A `Boolean` indicating whether `seriesValues` should be returned as an `Array` of objects in the format expected by the Dojox charting library. In this format, each element of the `seriesValues` `Array` is an object with two properties:
    * `x`: the `time`.
    * `y`: the `value`.
> By default, if this method is called without specifying this property in the `options` object (or if `options` is not passed), this value is set to `false`. If you want to set this value to `true`, you must also set `arrayHighcharts` to `false`.
  * `valueAsDecimalDegrees`: Many of the MoniCA points that have units of 'degrees' have their value expressed as a `String` with elements for degrees, minutes and seconds. If this property is specified as `true`, any such value is converted into a decimal number in degrees.
  * `timeAsSeconds`: A `Boolean` indicating whether any `time` returned by this method should be as Unix time in seconds. By default, this is `false`.
  * `timeAsDateObject`: A `Boolean` indicating whether any `time` returned by this method should be as a Javascript `Date`. By default, this is `false`.
  * `referenceValue`: This property can be set to a numeric value that will be subtracted from any `value` being returned by this method. By default, this is 0.
  * `referenceTime`: This property can be set to a numeric value that will be subtracted from any `time` being returned by this method. By default this is 0. Be careful that you set this property in milliseconds by default, or in seconds if you have set `timeAsSeconds` as `true`.

##### addCallback #####

```
pointReference.addCallback(functionReference);
```

Whenever a point is updated, either by the `monicaServer` or by a user through the `updateValue` or `setTimeSeriesValues` methods, any number of callback functions can be triggered by the `monicaPoint`. This method allows the user to add a function to the list of callbacks made by the `monicaPoint`.

This callback function is passed the `pointReference` object so that it can act upon it.

Example:
```
var callbackFn = function(pointReference) {
  // We get the latest value, and the point name.
  var value = pointReference.latestValue();
  var details = pointReference.getPointDetails();
  // And log it to the console.
  console.log('The point ' + details.name + ' has the value ' +
    value.value + ' at ' + value.time);
}

var connectionEstablished = function() {
  // Add a point.
  var pointReference = server.addPoints([ 'site.environment.weather.Temperature' ]);
  // Could be null if the server doesn't recognise the point; check for this.
  if (pointReference[0]) {
    pointReference[0].setDetails({
      'updateTime': 60
    });
    pointReference[0].addCallback(callbackFn);
  }
  server.getDescriptions();
}

var server = monicaServer();
server.connect().then(connectionEstablished);
```

##### timeSeriesOptions #####

```
var options = pointReference.timeSeriesOptions(newOptions);
```

This method allows you to set or get the options controlling the time-series. If this method is called without arguments, it returns the current options object, which has the parameters:
  * `startTime`: the time to start the series from, in the format `yyyy-mm-dd:HH:MM:SS`, or `-1` if the series finishes at the current time.
  * `spanTime`: the time covered by this series, in minutes.
  * `maxPoints`: the maximum number of points to keep from the time range.

If you wish to change the time-series options, pass an object to this method, with one or more of the options object parameters as listed above. In this case, the `options` return value will be the new options object after your changes have been made.

##### isTimeSeries #####

```
var timeSeriesFlag = pointReference.isTimeSeries();
```

This method returns a `Boolean` indicating whether the point represented by the `pointReference` object is a time-series.

##### timeSeriesInitialised #####

```
var timeSeriesStarted = pointReference.timeSeriesInitialised();
```

This method returns a `Boolean` indicating whether the point represented by the `pointReference` object is a time-series that has been initialised with data satisfying its time-series options.

##### timeRepresentation #####

```
var timeStyle = pointReference.timeRepresentation();
```

This method returns a `String` indicating the representation of the time that will be returned when you get this point's value. It will be one of:
  * `string`: This means the time will be in the format `yyyy-mm-dd_HH:MM:SS`.
  * `unixms`: This means the time will be an integer number, being the Unix time in milliseconds.

##### stopUpdating #####

```
pointReference.stopUpdating();
```

This method instructs this point to stop requesting updates from its `monicaServer`.

##### startUpdating #####

```
pointReference.startUpdating();
```

This method instructs the point to start requesting updates from its `monicaServer`. The frequency of updates is controlled via the `setPointDetails` method.

##### setValue #####

```
var setPromise = pointReference.setValue(details);
```

This method is used to set the value of a MoniCA control point. It takes an object as its argument, that must have the following properties:
  * `value`: The value to set the control point to.
  * `user`: The username required to control the point. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required to control the point. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
Depending on the type of value that the point has, you may also have to set another property:
  * `type`: The type of the value, which is one of the types that the `set` method on the ASCII interface supports (http://code.google.com/p/open-monica/wiki/ClientASCII). If the point value is a `String` (str) or `Boolean` (bool) this method will automatically be able to identify the `type`.

Note that to even get access to this method, you will need to have added the point to the server.

For example (AMD style):
```
require(["atnf/monica", "dojo/on", "dojo/domReady!"], function(monica, on) {
  var setPointRef = null;

  // Change the control point's value when a button is pushed.
  on('set_button', 'click', function(evtObj) {
    setPointRef.setValue({
      'user': 'theUserName',
      'pass': 'myPassword',
      'value': 1,
      'type': 'int'
    });
  });

  var connectionEstablished = function(serverObj) {
    // We can safely start the server updating now.
    narMonica.startUpdating();
    // Add the point that we can set.
    var pointRefs = narMonica.addPoints['site.temp.setter'];
    setPointRef = pointRefs[0];
    narMonica.getDescriptions();
  };

  var narMonica = monica.server({
    serverName: 'monhost-nar'
  });

  narMonica.connect().then(connectionEstablished);
});
```

##### isAlarm #####

```
var pointIsAlarm = pointReference.isAlarm(isAlarm);
```

This method can be called in two different ways.
  * If no argument is passed to this method, it will return a `Boolean` indicating whether this point is associated with an MoniCA alarm.
  * If a `Boolean` argument is passed to this method, the point will either think it is associated with a MoniCA alarm (if `true` is passed), or not. It will then return this same value.

Users should not usually call this method with an argument, as the alarm points will automatically set this flag as they are discovered.


---


### Method/object `monicaPointValue` ###

#### Constructor ####

##### AMD Style #####

```
require(["atnf/monica", "dojo/domReady!"], function(monica) {
  var pointValue = monica.pointValue(spec, my);
});
```

##### Traditional Style #####

```
var pointValue = monicaPointValue(spec, my);
```


The constructor function takes two arguments, both of which are optional.

  * `spec` is an object that configures the point's initial value. It can have the following properties:
    * `initialValue`: This is either an object or an `Array`. If it is an object, it should have the parameters:
      * `value`: The new value for this point.
      * `time`: The time for this new value. This time can be in any representation, but by default the MoniCA JSON returns the time for single point values as 'yyyy-mm-dd\_HH:MM:SS' and for time-series values as the Unix time in milliseconds.
      * `errorState`: A `Boolean` indication of whether the state is in error.
    * If `initialValue` is an `Array`, it should have elements `[time, value, errorState]`. The `errorState` element is optional.
  * `my` is any object that you would like the `monicaPoint` to have access to.

The user can make a new `monicaPointValue` if they desire, but the constructor is usually only called by the `monicaPoint` object when the user requests a new point or time-series. The `monicaPointValue` will not work correctly unless the `my` argument is specified as the `monicaPoint` object that will be responsible for updating it.

#### Public Methods ####

##### setValue #####

```
pointValue.setValue(newValue);
```

This method is used to set the point's value. It can be passed either an object or an `Array`. A `newValue` object has the parameters:
  * `value`: The new value for this point.
  * `time`: The time for this new value. This time can be in any representation, but by default the MoniCA JSON returns the time for single point values as 'yyyy-mm-dd\_HH:MM:SS' and for time-series values as the Unix time in milliseconds.
  * `errorState`: A `Boolean` indication of whether the state is in error.
Each of these parameters is optional.

A `newValue` `Array` has the elements `[time, value, errorState]`. The `errorState` element is optional.

Since the `monicaPointValue` objects are not usually made available to user functions, you won't normally need to call this method. To set the value of a control point, use the `setValue` method on a `monicaPoint` object.

##### getValue #####

```
var value = pointValue.getValue(options);
```

This method returns the value for this `monicaPointValue`. The format of the returned `value` depends on the `options` object that can be passed to this method, and has the parameters:
  * `arrayHighcharts`: A `Boolean` indicating whether `value` should be returned as an `Array` in the format expected by the Highcharts Javascript plotting library. In this format, `value` is a two-element `Array` with the first element being the `time` and the second being the `value`.
  * `arrayDojo`: A `Boolean` indicating whether `seriesValues` should be returned as an `Array` of objects in the format expected by the Dojox charting library. In this format, each element of the `seriesValues` `Array` is an object with two properties:
    * `x`: the `time`.
    * `y`: the `value`.
> By default, if this method is called without specifying this property in the `options` object (or if `options` is not passed), this value is set to `false`. If you want to set this value to `true`, you must also set `arrayHighcharts` to `false`.
  * `valueAsDecimalDegrees`: Many of the MoniCA points that have units of 'degrees' have their value expressed as a `String` with elements for degrees, minutes and seconds. If this property is specified as `true`, any such value is converted into a decimal number in degrees.
  * `timeAsSeconds`: A `Boolean` indicating whether any `time` returned by this method should be as Unix time in seconds. By default, this is `false`.
  * `timeAsDateObject`: A `Boolean` indicating whether any `time` returned by this method should be as a Javascript `Date`. By default, this is `false`.
  * `referenceValue`: This property can be set to a numeric value that will be subtracted from any `value` being returned by this method. By default, this is 0.
  * `referenceTime`: This property can be set to a numeric value that will be subtracted from any `time` being returned by this method. By default this is 0. Be careful that you set this property in milliseconds by default, or in seconds if you have set `timeAsSeconds` as `true`.

If the `arrayHighcharts` parameter is not explicitly set to `true` in the `options` object, the returned `value` will be an object with the parameters:
  * `value`: The new value for this point.
  * `time`: The time for this value.
  * `errorState`: A `Boolean` indication of whether the state is in error.

Since the `monicaPointValue` objects are not usually made available to user functions, you won't normally need to call this method. To get the value of a point, use one of the `latestValue` or `getTimeSeries` methods on a `monicaPoint` object.



---


### Object `monicaAlarm` ###

#### Constructor ####

##### AMD Style #####

```
require(["atnf/monica", "dojo/domReady!"], function(monica) {
  var alarmPoint = monica.alarm(spec, my);
});
```

##### Traditional Style #####

```
var alarmPoint = monicaAlarm(spec, my);
```

The constructor function takes two mandatory arguments.

  * `spec` is an object that configures the alarm and its initial state. It must have the following properties (as described on [the ASCII interface page](http://code.google.com/p/open-monica/wiki/ClientASCII)):
    * `pointName`: The name of the MoniCA point that the alarm is represented by.
    * `priority`: The priority assigned to the alarm.
    * `isAlarmed`: A `Boolean` indicating the current state of the alarm.
    * `acknowledged`: A `Boolean` indicating whether this alarm is currently acknowledged.
    * `acknowledgedBy`: If the alarm is currently acknowledged, the username used to acknowledge it.
    * `acknowledgedAt`: If the alarm is currently acknowledged, the time that it was acknowledged at.
    * `shelved`: A `Boolean` indicating whether this alarm is currently shelved.
    * `shelvedBy`: The username that was used to last shelve or unshelve this alarm.
    * `shelvedAt`: The time that this alarm was last shelved or unshelved.
    * `guidance`: A `String` that may contain information about how to deal with this alarm.
  * `my` has to be a reference to the `monicaServer` object it can use to communicate with MoniCA.

The user can make a new `monicaAlarm` if they desire, but the constructor is usually only called by the `monicaServer` object when sees a new alarm on MoniCA. The `monicaAlarm` will not work correctly unless the `my` argument is specified as the `monicaServer` object that will be responsible for communicating with MoniCA.

#### Public Methods ####

##### getState #####

```
var currentState = alarmPoint.getState();
```

This method gets the current state of this alarm. It returns an object with the properties:
  * `pointName`: The name of the MoniCA point that the alarm is represented by.
  * `priority`: The priority assigned to the alarm.
  * `isAlarmed`: A `Boolean` indicating the current state of the alarm.
  * `acknowledged`: A `Boolean` indicating whether this alarm is currently acknowledged.
  * `acknowledgedBy`: If the alarm is currently acknowledged, the username used to acknowledge it.
  * `acknowledgedAt`: If the alarm is currently acknowledged, the time that it was acknowledged at.
  * `shelved`: A `Boolean` indicating whether this alarm is currently shelved.
  * `shelvedBy`: The username that was used to last shelve or unshelve this alarm.
  * `shelvedAt`: The time that this alarm was last shelved or unshelved.
  * `guidance`: A `String` that may contain information about how to deal with this alarm.

##### updateState #####

```
alarmPoint.updateState(newState);
```

This method updates the internal state of this alarm, based on the mandatory argument, an object with the properties:
  * `pointName`: The name of the MoniCA point that the alarm is represented by.
  * `priority`: The priority assigned to the alarm.
  * `isAlarmed`: A `Boolean` indicating the current state of the alarm.
  * `acknowledged`: A `Boolean` indicating whether this alarm is currently acknowledged.
  * `acknowledgedBy`: If the alarm is currently acknowledged, the username used to acknowledge it.
  * `acknowledgedAt`: If the alarm is currently acknowledged, the time that it was acknowledged at.
  * `shelved`: A `Boolean` indicating whether this alarm is currently shelved.
  * `shelvedBy`: The username that was used to last shelve or unshelve this alarm.
  * `shelvedAt`: The time that this alarm was last shelved or unshelved.

Each of these properties is optional, except for `pointName` which must be present and set to the same name as this alarm represents. Any other properties in the `newState` object will be copied to the internal state. This method also fires any callbacks once it update the value.

The user will not normally call this method, as the `monicaServer` will call it when the MoniCA alarms are queried.

##### fireCallbacks #####

```
alarmPoint.fireCallbacks();
```

This method causes the `monicaAlarm` object to fire all its callbacks.

##### acknowledge #####

```
alarmPoint.acknowledge(authData);
```

This method acknowledges the alarm on MoniCA. It takes one optional argument, which is an object that has the information required to authenticate with MoniCA; it has the properties:
  * `user`: The username required to acknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required to acknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
If this argument is not provided, the user name and password set during the `monicaServer` instantiation, or a later call to its `updateAlarmAuthData` method, or a call to this object's `setAuthData` method. If this argument is provided, it will not overwrite this previously-set authentication information.

There is no feedback from this method. You will need to wait until the server polls again for the alarm states before seeing the acknowledgement.

##### unacknowledge #####

```
alarmPoint.unacknowledge(authData);
```

This method unacknowledges the alarm on MoniCA. It takes one optional argument, which is an object that has the information required to authenticate with MoniCA; it has the properties:
  * `user`: The username required to unacknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required to unacknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
If this argument is not provided, the user name and password set during the `monicaServer` instantiation, or a later call to its `updateAlarmAuthData` method, or a call to this object's `setAuthData` method. If this argument is provided, it will not overwrite this previously-set authentication information.

There is no feedback from this method. You will need to wait until the server polls again for the alarm states before seeing the unacknowledgement.

##### autoAcknowledge #####

```
alarmPoint.autoAcknowledge(authData);
```

Depending on this point's current acknowledgement state, this method either acknowledges or unacknowledges the alarm on MoniCA. It takes one optional argument, which is an object that has the information required to authenticate with MoniCA; it has the properties:
  * `user`: The username required to (un)acknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required to (un)acknowledge the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
If this argument is not provided, the user name and password set during the `monicaServer` instantiation, or a later call to its `updateAlarmAuthData` method, or a call to this object's `setAuthData` method. If this argument is provided, it will not overwrite this previously-set authentication information.

There is no feedback from this method. You will need to wait until the server polls again for the alarm states before seeing the (un)acknowledgement.

##### shelve #####

```
alarmPoint.shelve(authData);
```

This method shelves the alarm on MoniCA. It takes one optional argument, which is an object that has the information required to authenticate with MoniCA; it has the properties:
  * `user`: The username required to shelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required to shelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
If this argument is not provided, the user name and password set during the `monicaServer` instantiation, or a later call to its `updateAlarmAuthData` method, or a call to this object's `setAuthData` method. If this argument is provided, it will not overwrite this previously-set authentication information.

There is no feedback from this method. You will need to wait until the server polls again for the alarm states before seeing the shelve.

##### unshelve #####

```
alarmPoint.unshelve(authData);
```

This method unshelves the alarm on MoniCA. It takes one optional argument, which is an object that has the information required to authenticate with MoniCA; it has the properties:
  * `user`: The username required to unshelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required to unshelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
If this argument is not provided, the user name and password set during the `monicaServer` instantiation, or a later call to its `updateAlarmAuthData` method, or a call to this object's `setAuthData` method. If this argument is provided, it will not overwrite this previously-set authentication information.

There is no feedback from this method. You will need to wait until the server polls again for the alarm states before seeing the unshelve.

##### autoShelve #####

```
alarmPoint.autoShelve(authData);
```

Depending on this point's current shelve state, this method either shelves or unshelves the alarm on MoniCA. It takes one optional argument, which is an object that has the information required to authenticate with MoniCA; it has the properties:
  * `user`: The username required to (un)shelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required to (un)shelve the alarm. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
If this argument is not provided, the user name and password set during the `monicaServer` instantiation, or a later call to its `updateAlarmAuthData` method, or a call to this object's `setAuthData` method. If this argument is provided, it will not overwrite this previously-set authentication information.

There is no feedback from this method. You will need to wait until the server polls again for the alarm states before seeing the (un)shelve.

##### setAuthData #####

```
var alarmRef = alarmPoint.setAuthData(authData);
```

This method sets the authentication data that this alarm requires to acknowledge or shelve itself. It takes one argument, an object with the properties:
  * `user`: The username required. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.
  * `pass`: The password required. This should be in plain text, and this routine will transparently encrypt it before sending it over the network.

It returns the alarm point reference so that you can method chain after this call.

##### addCallback #####

```
alarmPoint.addCallback(callbackFn);
```

This method can be used to add a new function to the list of callbacks that this alarm object will make when its state is updated. The single mandatory argument should be the function that should be added to the callback list.

##### getPoint #####

```
var pointRef = alarmPoint.getPoint();
```

This method returns the `monicaPoint` associated with this alarm point.

##### alarmOff #####

```
alarmPoint.alarmOff();
```

This method resets the alarm state to `false`. This is not usually called by the user; the `monicaServer` object will call it just before it polls MoniCA for all the active alarms. Because such a poll will only return alarms that have a `true` alarm state, it is not actually possible to transition an alarm to `false` without this call.



---


The traditional Javascript API also extends the `String` type with a method `sexagesimalToDecimal`. If the `String` it acts on has the regular expression format `/^[\+\-]*\d+\?\d+\'\d+\"\.\d*$/` (the format MoniCA returns for an angle), or `/^[\+\-]*\d+\:\d+\:\d+$/` (sexagesimal format), this method will return a number that is the decimal equivalent of the `String`, otherwise it will return the `String` unchanged.

Example 1:
```
var decimalDegrees = '-12:34:56'.sexagesimalToDecimal();
console.log(decimalDegrees); // This will be -12.58222.
```

Example 2:
```
var noChange = 'Hello'.sexagesimalToDecimal();
console.log(noChange); // This will still be 'Hello'.
```

The AMD Javascript API does not have this extension, but rather uses the methods found in the ATNF Javascript Library.