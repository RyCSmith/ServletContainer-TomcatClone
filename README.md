# ServletContainer-TomcatClone

Servlet container and static content web server. Run from HttpServer file with arguments [port number, path to /www folder, path to web.xml]. Loads servlets from web.xml and responds to the corresponding URL mappings. Serves static contents in byte form for files including, .jpg, .png, .json, .pdf, etc.

Server implements it's own Thread Pool and Synchronized Queue. 

*Servlets and Ant build script were provided by CIS555 at University of Pennsylvania.
