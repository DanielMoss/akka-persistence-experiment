# akka-persistence-experiment

Whilst experimenting with akka I stumbled into some unexpected behaviour, where it seemed some actors stopped receiving messages. I eventually narrowed the problem down to a nested persist call, and I've created this project to explore the problem a bit more.

The exact behaviour I am seeing is as follows:
* Send an actor some messages that it processes normally.
* Send the actor a message which results in a call to a persist, which is nested inside of a deferAsync call.
* Send the actor a message. It will process this message normally.
* Send the actor further messages. The actor will receive none of these messages, but does not seem to have terminated either.

After testing, this seems to happen if you have a call to persist within a call to either deferAsync or persistAsync. Under the [akka documentation section on nested persist calls](https://doc.akka.io/docs/akka/2.5.6/scala/persistence.html#nested-persist-calls), it mentions that you may nest mixed persist and persistAsync calls, though it does warn against this. Perhaps I have misunderstood the intention of the documentation. However it seems particularly strange to me that the actor will gladly handle one and only one more message after the nested calls without issue.

Quick links: <br/>
[application.conf](src/main/resources/application.conf)  <br/>
[PersistenceTestActor.scala](src/main/scala/PersistenceTestActor.scala)

I created an issue on the akka project for this, which you may find [here](https://github.com/akka/akka/issues/23781).
