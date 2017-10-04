import PersistenceTestActor._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.persistence.PersistentActor

object PersistenceTestActor {
  case object SomeMessage    // Just something to send to the actor. Only for logging.
  case object PersistedEvent // Some event that will cause a call to persist.
  case object OuterEvent     // An event that will be passed to the outer method, after the initial call to persist.
  case object InnerEvent     // An event that will be passed to the inner method, within the outer method.

  type PersistentActorMethod = PersistentActor => Any => (Any => Unit) => Unit
}

class PersistenceTestActor(outerMethod: PersistentActorMethod,
                           innerMethod: PersistentActorMethod) extends PersistentActor {

  def persistenceId = "Test"                     // Not important
  def receiveRecover: Receive = { case _ => () } // Not important

  def receiveCommand: Receive = LoggingReceive {
    case SomeMessage =>                          // This is just to trigger receive logging

    case PersistedEvent =>
      persist(PersistedEvent)(_ => ())

      outerMethod(this)(OuterEvent)(_ =>
        innerMethod(this)(InnerEvent)(_ => ())
      )
  }
}

object Playground {
  def main(args: Array[String]): Unit = {
    val persist:      PersistentActorMethod = _.persist
    val persistAsync: PersistentActorMethod = _.persistAsync
    val deferAsync:   PersistentActorMethod = _.deferAsync

    def createActor(outerMethod: PersistentActorMethod, innerMethod: PersistentActorMethod): ActorRef =
      ActorSystem("Test").actorOf(Props(new PersistenceTestActor(outerMethod, innerMethod)))

    /**
     * Test cases
     * In the error case, we observe the following behaviour:
     *
     * Successfully receive the first 5 `SomeMessage`s
     * Successfully receive the `PersistedEvent`
     * Successfully receive a `SomeMessage`
     * Receive no further `SomeMessage`s
     */

    val actor = createActor(outerMethod = deferAsync, innerMethod = persist)   // Result: Error case
//    val actor = createActor(outerMethod = persistAsync, innerMethod = persist) // Result: Error case

//    val actor = createActor(outerMethod = deferAsync, innerMethod = deferAsync)   // Result: Expected behaviour
//    val actor = createActor(outerMethod = deferAsync, innerMethod = persistAsync) // Result: Expected behaviour

//    val actor = createActor(outerMethod = persist, innerMethod = deferAsync)   // Result: Expected behaviour
//    val actor = createActor(outerMethod = persist, innerMethod = persist)      // Result: Expected behaviour
//    val actor = createActor(outerMethod = persist, innerMethod = persistAsync) // Result: Expected behaviour

//    val actor = createActor(outerMethod = persistAsync, innerMethod = deferAsync)   // Result: Expected behaviour
//    val actor = createActor(outerMethod = persistAsync, innerMethod = persistAsync) // Result: Expected behaviour

    /* Perform the test*/

    // Test if we can receive messages
    (1 to 5).foreach { _ =>
      Thread.sleep(500L)
      actor ! SomeMessage
    }

    // Send in the problematic event
    Thread.sleep(500L)
    actor ! PersistedEvent

    // Test if we still can receive messages
    (1 to 5).foreach { _ =>
      Thread.sleep(500L)
      actor ! SomeMessage
    }
  }
}