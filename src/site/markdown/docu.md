## Layers
The library consist of the following layers.

	                     .----------------------.
	                     |   Meta Model Layer   | 
	Topology | Meta-   /\|                      |
	Layer    | Model    |+----------------------+
	Callback\/          ||                      | DomanModelClient/Server
	                     |    Topology Layer    |<------------- 
	                     |                      | UserCallbackClient/Server
	Message  | Network /\|                      |-------------->  O                  
	Transfer | To       |+----------------------+                \|/
	Client/ \/ Topology ||                      | <Implementat-> / \
	Server     Client/   |    Network Layer     | <ion specific> User
	           Server    |                      |<--------------
	                     |                      |-------------->
	                     '----------------------'

The names next to the arrows are the names of Java classes and interfaces that are used for communication between the layers or the user of the library.
