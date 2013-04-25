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
	                     |  .---------------.   |-------------->
	                     |  | Serialisation |   |
	                     |  '---------------'   |
	                     '----------------------'

The names next to the arrows are the names of Java classes and interfaces that are used for communication between the layers or the user of the library.

## thread-safety when changes appear while the domain model is walked through
When a new peer connects it is normally required to "walk" through the domain model of the user via reflection. 
This way the messages are created this new peer needs to reproduce the current state of the domain model.
In the client/server topology layer implementation this is the case when a new client connects to the server.  
In this case it would be helpful to lock the entire domain model so that no changes can occur while this walking process is active.
Unfortunately this is not possible if the user should not be forced to `synchronize() {}` every access to his domain model.
Another alternative would be to do the changes only in the JavaFX thread.
On the client side this is done anyway because this is required by GUI elements.
On the server side however the JavaFX thread is may not even running.

The first opportunity for SynchronizeFX to realize that the user has done a change is when its listeners which are registered on each property are executed.
Unfortunately at this point the changes already happened.
If the walking process iterates through an list at this moment and the change was the removal of an element of this list, an ConcurrentModificationException can occur.

The solution to this problem that is implemented in SynchronizeFX is to accept that ConcurrentModificationException can happen and just restart the walking process from the root object in this case.
The restart is also done when a part of the domain model was changed that has already been walked over. 
If the change happened in a part of the model that would be walked over later anyway there is no need to abort.

When a change listener realizes a change it informs the property walker that is has to check if a restart is required.
It than blocks the user process in which it is executed until the property walker has finished.
In worst case every user thread that modifies the domain model has to be blocked this way.
When this happened the property walker can terminate so it can be guaranteed that there is no endless loop of property walking restarts.