# ServletContainer-TomcatClone

Servlet container and static content web server. Run from HttpServer file with arguments [port number, path to /www folder, path to web.xml]. Loads servlets from web.xml and responds to the corresponding URL mappings. Serves static contents in byte form for files including, .jpg, .png, .json, .pdf, etc.

Additional Info:
-Server implements it's own Thread Pool and Synchronized Queue. Configured to start with 100 threads which can be adjusted.

-Server honors if-modified and if-unmodified requests and attempt to parse all 3 date formats specified in https://www.jmarshall.com/easy/http/.

-Server identifies cases and generates response codes pertaining to the following cases: 200, 304, 400, 403, 404, 412, and 500. 

-/control lists all the threads and their states. If threads are active it lists what URLs they are serving. It also provides a shutdown link.

-/shutdown safely stops all threads and shuts down the server. Each thread finishes processing its request before exiting. Main loops on a standard timeout and it may take ~5 for the command to finish, this can also be delayed by intense thread activity.

*Servlets and Ant build script were provided by CIS555 at University of Pennsylvania.
