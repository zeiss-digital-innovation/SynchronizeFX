## Basic Concepts

This library divides classes that make up the domain model into two categories: simple classes and observable classes.

### Observable Classes
These are classes that contain fields of types that can be used as [Property][propertyJDoc].
Fields of the Type [IntegerProperty][intPropJDoc], [StringProperty][stringPropJDoc] or [ListProperty][listPropJDoc] fulfill this requirement for example.
[ObservableList][observableListJDoc] for example doesn\'t fulfill this requirement.
On objects of these classes only changes done on Property fields are synchronized between all peers.
All other fields are not.

[propertyJDoc]: http://docs.oracle.com/javafx/2/api/javafx/beans/property/Property.html "Javadoc for the Property interface"
[intPropJDoc]: http://docs.oracle.com/javafx/2/api/javafx/beans/property/IntegerProperty.html "Javadoc for IntegerProperty"
[stringPropJDoc]: http://docs.oracle.com/javafx/2/api/javafx/beans/property/StringProperty.html "Javadoc for StringProperty"
[listPropJDoc]: http://docs.oracle.com/javafx/2/api/javafx/beans/property/ListProperty.html "Javadoc for ListProperty"
[observableListJDoc]: http://docs.oracle.com/javafx/2/api/javafx/collections/ObservableList.html "Javadoc for ObservableList"

### Simple Classes
These are classes that don\'t have any Property fields.
Objects of them are treated atomically.
That means that changes on fields of them are not synchronized between peers but if they are set as values
for property fields in observable objects they are transfered in one piece to other peers.
In most domain models this are for example [Integer][intJDoc], [Double][doubleJDoc] or [String][stringJDoc].

[intJDoc]: http://docs.oracle.com/javase/7/docs/api/java/lang/Integer.html "Javadoc for Integer"
[doubleJDoc]: http://docs.oracle.com/javase/7/docs/api/java/lang/Double.html "Javadoc for Double"
[stringJDoc]: http://docs.oracle.com/javase/7/docs/api/java/lang/String.html "Javadoc for String"

## Requirements on the domain model
All objects of the instance of the domain model must be accessible through a single root object.
The root object must be an instance of an Observable Class.

All Observable Classes in the domain model must have a public no-argument constructor.
All Property fields of them must be initialized right after the object has bean instantiated.
E.g. initializing properties the following way will do fullfill this requirement.

    private LongProperty someProperty = new SimpleLongProperty();

[dateJDoc]: http://docs.oracle.com/javase/7/docs/api/java/util/Date.html "Javadoc for Date"

## Maven Config

To add SynchronizeFX to a Maven based project add the following to your pom.xml

    <dependency>
        <groupId>de.saxsys.synchronizefx</groupId>
        <artifactId>synchronizefx</artifactId>
        <version>${currentVersion}</version>
    </dependency>

You must make sure that you have added an repository which contains this artifact in the \<repositories\> section
or add this artifact to your local repository by invoking _mvn install_ in the root directory of the source tree of this framework. 

## Examples

### Starting the Server
Let\'s assume that the root object of your domain model instance is _root_ and that it fullfils  the requirements described above.
You can than make this instance available in the network with the following code.

    ...
    
    private SomeDomainClass root = getSomeHow();
    private SynchronizeFxServer server;
    
    ...
    
    //setup the SynchronizeFX server
    server = SynchronizeFxBuilder.create().server().model(root).callback(new ServerCallback() {
            @Override
    	    void onError(SynchronizeFXException error) {
    		    //Put your code to handle errors that occur after the successful startup
    		    //here.
    	    }).build();
    //and start it
    server.start();
        
    ...
    
    //To shut down the server when you are done, use the following.
    server.shutdown();


### Starting the Client
To access an object that was made available as described above, the following code can be used.

    ...
    
    //can be an IP address or a host name
    private final static String SERVER = "localhost";
    
    private SynchronizeFxClient client;
    private SomeDomainClass root;
    
    ...

    //setup the SynchronizeFX client
    client = SynchronizeFxBuilder.create().client().address(SERVER).callback(ClientCallback() {
        @Override
        public void modelReady(final Object model) {
            //Put your code that uses the remote model here.
            root = (SomeDomainClass) model;
        }
              
        @Override
    	void onError(SynchronizeFXException error) {
    	    //Put your code to handle errors that occur after the successful
    		//connection here.
    	}
    	
        @Override
        public void onServerDisconnect() {
            //Code that handles the case that the server disconnected.
        }
    ).build();
    client.connect();
        
    ...
    
    //To terminate the network connection use the following.
    client.disconnect();

