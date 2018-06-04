package simulator.traits

import org.scalatest.FlatSpec

class LoggableSpec extends FlatSpec {

  "camel2Underscore" should "leave single word alone" in {
    assert(Loggable.camel2Underscore("price") == "price")
  }

  it should "leave two underscored words alone" in {
    assert(Loggable.camel2Underscore("buyer_id") == "buyer_id")
  }

  it should "underscore a two words in camel case" in {
    assert(Loggable.camel2Underscore("buyerId") == "buyer_id")
  }

  it should "underscore many camel cased words" in {
    assert(Loggable.camel2Underscore("thisIsACamelCaseString") == "this_is_a_camel_case_string")
  }
}
