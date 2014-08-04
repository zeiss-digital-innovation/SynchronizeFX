## Layers
The library consist of the following layers.

	                     .----------------------.
	                     |   Meta Model Layer   | 
	Topology | Meta-   /\|                      |
	Layer    | Model    |+----------------------+
	Callback\/          ||                      | DomanModelClient/Server
	                     |    Topology Layer    |<------------- 
	                     |                      | UserCallbackClient/Server
	Command  | Network /\|                      |-------------->  O
	Transfer | To       |+----------------------+                \|/
	Client/ \/ Topology ||                      | <Implementat-> / \
	Server     Client/   |    Network Layer     | <ion specific> User
	           Server    |                      |<--------------
	                     |  .---------------.   |-------------->
	                     |  | Serialisation |   |
	                     |  '---------------'   |
	                     '----------------------'

The names next to the arrows are the names of Java classes and interfaces that are used for communication between the layers or the user of the library.

## Thread safety in SynchronizeFX
When a new peer connects it is normally required to "walk" through the domain model of the user via reflection. 
This way the commands are created this new peer needs to reproduce the current state of the domain model.
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

An assertion is made that only one of the threads the user controls can make a modification to a single Property.
If multiple user threads do that, than it is assumed that the user has forgotten to `synchronize() {}` access to his domain model between his own threads.
With this assumption there can only be one change per Property that can occur while the model walking is in progress.
For MapProperty SetProperty and single value Property fields this single change can just be applied on the new client.
It doesn't matter if it was already applied to the state of the user model that the Property walker walked over.
If you remove or add an Object to a Set a second time, the state of the set doesn't change.
The only problematic Property is the ListProperty as adding an Object to a list a second time does very well change its state.
With the assumption that only one lost change could happen the side that should execute the change can check if the change needs to be applied if it knows the size the list should have after the change is applied.
Therefore the new size of the list is passed with every AddToList and RemoveFromList command.
