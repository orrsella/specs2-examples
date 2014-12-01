package com.example

import org.specs2.mutable.{After, SpecificationWithJUnit}
import org.specs2.specification.Scope

/**
 * 1. The `Test` suffix is the name is important because of the maven plugin that runs the tests
 *
 * 2. We don’t extend `org.specs2.mutable.Specification` because it won’t be run by maven. `SpecificationWithJUnit`
 *    extends `Specification` and adds a `JUnitRunner`
 *
 * 3. `org.specs2.mutable.Specification` is a unit specification that uses `should/in` blocks which build `Fragments` (by
 *    adding them to a mutable protected variable, hence the package name. This doesn’t mean that your code is/needs
 *    to be mutable!)
 *
 *    https://github.com/orrsella/scala-maven-template
 *    http://etorreborre.github.io/specs2/
 */
class MyClassTest extends SpecificationWithJUnit {

  /**
   * 1. The `in` function creates an `Example` which returns a `Result`
   * 2. The `should` function creates a group of `Example`s
   * 3. An `Example` is a piece of text followed by anything which can be converted to an `org.specs2.execute.Result`
   * 4. A `Result` can be: `success`, `failure`, `pending`, a matcher result, etc.
   * 5. An `Example` usually contains/ends with expectations (assertions)
   */
  "My awesome class" should {
    "do something in" in {
      success
    }

    "do something else" in {
      pending
    }

    "be awesome" in {
      val myClass = new MyClass()
      myClass.isAwesome must beTrue
    }
  }

  /**
   * Examples can be complex and require elaborate set-up of data to:
   * - create inter-related domain objects
   * - put the environment (database, filesystem, external system) in the appropriate state
   *
   * And there are usually 3 difficulties in doing that:
   * - Variables isolation: making sure that each example can be executed with its own data without being impacted by the undesired side-effects of other examples
   * - Before/After code: running code before or after every example without repeating that code in the body of each example
   * - Global setup/teardown code: setting some state when this could take lots of resources, so you need to do it just once before anything runs
   *
   * 1. Isolation can be achieved by creating a new trait or a case class to open a new `Scope` with fresh variables
   * 2. `Context` can be named whatever we want
   * 3. Each example using Context gets a new instance of Context, and so will have a brand new myClass variable. Even
   *    if this data is mutated by an example, other examples will be isolated from these changes
   *
   * Scopes are a way to create a "fresh" object and associated variables for each example being executed. The advantages are that:
   * - those classes can be reused and extended (we can use inheritance to describe more and more specific contexts)
   * - the execution behavior only relies on language constructs
   */
  trait Context extends Scope {
    val myClass = new MyClass
  }

  trait ContextWithHelloMessage extends Context {
    val helloMessage = myClass.hello
  }

  "Test with variable isolation and setup code" should {
    "create a new Context (Scope) for each example" in new Context {
      myClass.prime must_== 11
    }

    "create a new RichContext for this example" in new ContextWithHelloMessage {
      helloMessage must_== "Hello world"
    }
  }

  /**
   * If you want to run some code before or after each example, the Before and After traits are there to help
   * you (they both extend the Scope trait). Before is unnecessary most of the time because it can be achieved
   * with lazy vals. In this case, the clean-up code defined in the after method will be executed **after each example**.
   */
  trait ContextWithCleanup extends After {
    val userId = 42
    val userEmail = "foo@bar.com"

    override def after: Any = {
      // do some cleanup code, like delete from db
    }
  }

  /**
   * Expectations/Matchers
   */
  trait RichContext extends Scope {
    val count: Int = 1
    val address = "40 Hanamal St., Tel Aviv, Israel"
    val name = Some("John")
    val employees = Seq.empty[String]
    val months = Map(1 -> "January", 2 -> "February", 3 -> "March", 4 -> "April")
    def explode(): Unit = throw new Exception("Kaboom!")
  }

  "Matchers example" in new RichContext {

    // equality
    count must_== 1
    count must beEqualTo(1)
    count mustEqual 1

    // equality with type-safety
    val x: Int = 1
    count === x

    // negation
    count must_!= 2
    count must not be equalTo(2)

    // string
    address must endWith("Israel")
    address must contain("Tel Aviv")
    address must haveLength(32)
    address must beEqualTo("40 hanamal st., tel aviv, ISRAEL").ignoreCase

    // numeric
    address.length must be_>=(20)
    address.length must beGreaterThan(10)
    address.length must beBetween(0, 100)

    // option
    name must beSome("John")
    name must not(beNone)

    // collections
    employees must beEmpty
    employees must not(contain("Jane"))

    // map + collections
    months must havePair(1 -> "January")
    months must not(haveKey(12))
    months.keys must haveSize(4)
    months.values must contain("March")

    // exception
    explode must throwAn[Exception]
  }
}
