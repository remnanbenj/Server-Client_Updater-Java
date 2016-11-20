#Description:
	This is an updating system for an application. Keep your users
	up to date with your application
	
# How to run:
	There are 2 seperate Eclipse projects. One for the server and one
	for the client.
	Load the server project into Eclipse. From here you can run it.
	Once the server has started you can load and run the client project. 
	
What you can do:
	The server has a zipped file in the same directory with a number.
	E.g. "1.2.3.zip". The number representing the version number. The 
	higher number will always be chosen to send to the client.
	
	When the client is launched it tries to connect to the server. Once
	a connection is established the server sends its highest version number 
	over. If its higher than the current version number (or no version number)
	then you will be prompted to update. 
	This update can be stopped and the client can be closed and reopened without
	losing the amount downloaded.