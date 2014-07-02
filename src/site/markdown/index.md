JavaFX extends the concept of properties of objects known from JavaBeans.
Instead of using plain fields for properties, special wrapper objects are used for them.
This allows the binding of properties in the model to GUI elements.
E.g. if you have a property _money_ in some domain object you can bind it to some GUI-Label which shows this value.
You can then update this property anywhere in the application and all the GUI-Elements bound to this property would be updated automatically.
When a property is updated there is no need to know what is bound to it and it is no necessary to initiate any update process.
A good introduction to JavaFX properties is the article [Using JavaFX Properties and Binding][jfx-properties]

[jfx-properties]: http://docs.oracle.com/javafx/2/binding/jfxpub-binding.htm "Using JavaFX Properties and Binding"

Using only standard JavaFX classes, this concept works only within a single JVM.
The goal of this library is to overcome this limitation.
The main idea is to have a domain model which consists of Java classes containing JavaFX Properties.
A single instance of the domain model is than shared between multiple JVM processes running on different machines.
If a property on any object of the model instance on any machine is changed, all other machines get this change too.
GUI-Elements that are bound to the property on any machine will update them self automatically. 
From the user perspective it looks like the change was done in the local JVM.

Only the root object of the shared model has to be retrieved with some special code.
After that changes on the shared model instance can be done as if it was not shared.
This makes upgrading of existing JavaFX applications very easy.
