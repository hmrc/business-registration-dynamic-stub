# business-registration-dynamic-stub
=============

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)


# business-registration-dynamic-stub Overview - A stub Service
--------------
business-registration-dynamic-stub is a stub service that simulates the DES endpoint for company registration, 
company incorpation and identify verification


Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Run the application

To run the application execute
(values for the keys can be found in the service manager config repository):

```
sbt 'run 9642 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes' 
```

## Test the application

To test the application execute:

```
sbt test it/test
```

* To run unit and integration tests respectively
    - `test`
    - `it/test`

## Run acceptance tests
* [Acceptance tests](https://github.com/hmrc/company-registration-acceptance-tests)

License
---

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator
[JSON]: http://json.org/