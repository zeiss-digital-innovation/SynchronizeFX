## Basic Concepts

This library divides classes that make up the domain model into two categories: simple classes and observable classes.

### Observable Classes
These are classes that contain fields of types that can be used as [Property][propertyJDoc].
Fields of the Type [IntegerProperty][intPropJDoc], [StringProperty][stringPropJDoc] or [ListProperty][listPropJDoc] fulfill this requirement for example.
[ObservableList][observableListJDoc] for example doesn't fulfill this requirement.
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
That means that changes on properties of them are not synchronized between peers but if they are set as values
for property fields in observable objects they are transfered in one piece to other peers.
In most domain models this are for example [Integer][intJDoc], [Double][doubleJDoc] or [String][stringJDoc].

[intJDoc]: http://docs.oracle.com/javase/7/docs/api/java/lang/Integer.html "Javadoc for Integer"
[doubleJDoc]: http://docs.oracle.com/javase/7/docs/api/java/lang/Double.html "Javadoc for Double"
[stringJDoc]: http://docs.oracle.com/javase/7/docs/api/java/lang/String.html "Javadoc for String"

## Requirements On The Domain Model
All objects of the instance of the domain model must be accessible through a single root object.
The root object must be an instance of an Observable Class.

All Observable Classes in the domain model must have a public no-argument constructor.
All Property fields of them must be initialized right after the object has bean instantiated.
E.g.  initializing  properties the following way will do the trick.

    private LongProperty money = new SimpleLongProperty();

For Simple Classes, all wrapper objects for primitive Java types (e.g. Integer) and [String][stringJDoc] are supported out of the box.
Other Simple Classes that are used in the model (e.g. [Date][dateJDoc] if it\'s used) have to be registered with KryoNetMessageHandler and a custom serializer has to be provided.

[dateJDoc]: http://docs.oracle.com/javase/7/docs/api/java/util/Date.html "Javadoc for Date"

## Starting The Server
Let\'s assume that the root object of your domain model instance is _root_ and that it fulfils  the requirements described above.
You can than make this instance available in the network with the following code.

    import de.saxsys.synchronizefx.core.clientserver.DomainModelServer;
    import de.saxsys.synchronizefx.core.clientserver.UserCallbackServer;
    import de.saxsys.synchronizefx.kryo.KryoNetServer;
    
    private final static int PORT = 5000;
    private SomeDomainClass root = getSomeHow();
    private DomainModelServerServer server;
    
    ...
    
    //Use kryonet for the transfer of the messages the library produces.
    KryoNetServer kryoServer = new KryoNetServer(PORT);
    
    //If you use simple classes other than Integer, String and co. you have to register them.
    //For DateTime of JodaTime this could look like this. Serializer have to be registered in
    //the same order (and the same way) in the client and in the server. 
    kryoServer.registerSerializableClass(DateTime.class, new Serializer<DateTime>() {
        @Override
        public void write(final Kryo kryo, final Output output, final DateTime object) {
            output.writeLong(object.getMillis());
        }

        @Override
        public DateTime create(final Kryo kryo, final Input input, final Class<DateTime> type) {
            return new DateTime(input.readLong());
        }
    });
    
    try {
        //Makes the root object available in the network.
        server = new DomainModelServerServer(root, kryoServer, new UserCallbackServer() {
            @Override
    	    void onError(SynchronizeFXException error) {
    		    //Put your code to handle errors that occur after the successful startup
    		    //here.
    	    }
        });
    } catch (SynchronizeFXException e) {
    	//put your code that handles errors that occur while trying to start
    	//the server here.
    }
    
    ...
    
    //To shut down the server when you are done, use the following.
    server.shutdown();


## Starting The Client
To access an object that was made available as described above, the following code can be used.

    import de.saxsys.synchronizefx.core.clientserver.DomainModelClient;
    import de.saxsys.synchronizefx.core.clientserver.UserCallbackClient;
    import de.saxsys.synchronizefx.kryo.KryoNetClient;
    
    //can be an IP address or host name
    private final static String SERVER = "localhost";
    private final static int PORT = 5000;
    
    private DomainModelClient client;
    private SomeDomainClass root;
    
    ...

    //Use kryonet for the transfer of the messages the library produces.
    KryoNetClient kryoClient = new KryoNetClient(PORT, SERVER);
    
    //serializer have to be registered the in same way and the same order as on the server.
    kryoClient.registerSerializableClass(DateTime.class, new Serializer<DateTime>() { ...
    
    try {
        //Connect to the server
        client = new DomainModelClient(kryoClient,
            new UserCallbackClient() {
              @Override
              public void modelReady(final Object model) {
                  root = (SomeDomainClass) model;
                  //Put your code that uses the remote model here.
              }
              
              @Override
    	      void onError(SynchronizeFXException error) {
    		      //Put your code to handle errors that occur after the successful
    		      //connection here.
    	      }
        });
    } catch (SynchronizeFXException e) {
    	//put your code that handles errors that occur while trying to connect
    	//to the server here.
    }
    
    ...
    
    //To terminate the network connection use the following.
    client.disconnect();

