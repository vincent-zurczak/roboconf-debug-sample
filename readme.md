# Roboconf Debug Sample

This sample shows how to run the Deployment Manager as RESTful web services
from Eclipse and how you can programmatically deploy an application.

This is really convenient to test and debug administration actions and the messaging in Roboconf.  
The web server can be reached at http://localhost:9023  
To use it with the web administration in development mode, add...

```js
'use strict';

angular.module('roboconf.preferences')
.constant('ROBOCONF_SERVER_URL', 'http://localhost:9023/roboconf-dm');
```

... in the **target/dev.config/roboconf.dev.configuration.js** file of the web administration.
