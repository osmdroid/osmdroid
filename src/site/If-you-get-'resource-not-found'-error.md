
This is because osmdroid uses a number of graphics (Android drawables) that represent things like current device location, zoom in/out buttons, etc. These are not included with osmdroid because it's distributed as a JAR file (versions =< 4.3). You have two options:

1. Implement your own version of "ResourceProxy"
2. Pull in the osmdroid example application's drawable files into your own application.

This process is detailed here [How-to-use-the-osmdroid-library#create-a-custom-resource-proxy](How-to-use-the-osmdroid-library#create-a-custom-resource-proxy)
