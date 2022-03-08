![example workflow](https://github.com/reactific/riddl/actions/workflows/<WORKFLOW_FILE>/badge.svg)


# riddl

## Introduction
RIDDL, the Reactive Interface to Domain Definition Language, is a tool for
eliminating the boilerplate from reactive systems designed with Domain
Driven Design (DDD).   
It uses a DDD inspired specification language to allow subject matter experts 
and developers alike to work at a higher level of specification than they would
if they were coding directly in a programming language. It aims to relieve 
developers of the burden of maintaining infrastructural code through evolution 
of the domain abstractions.

## Usage
To get the most recent options, run `riddlc --help`. As of version 0.1.1, that 
will print out:
```text
RIDDL Compiler (c) 2019 Yoppworks Inc. All rights reserved. 
Version:  0.1.1
Usage: riddlc [parse|prettify|validate|translate] [options]

  -h, --help
  -v, --verbose
  -q, --quiet
  -w, --suppress-warnings
  -m, --suppress-missing-warnings
  -s, --suppress-style-warnings
  -t, --show-times
Command: parse [options]
Parse the input for syntactic compliance with riddl language
  -i, --input-file <value>
                           required riddl input file to compile
Command: prettify [options]
Parse the input and print it out in prettified style
  -i, --input-file <value>
                           required riddl input file to compile
Command: validate [options]

  -i, --input-file <value>
                           required riddl input file to compile
Command: translate [options]
translate riddl as specified in configuration file 
  -i, --input-file <value>
                           required riddl input file to compile
  -c, --configuration-file <value>
                           configuration that specifies how to do the translation
``` 
## Goals
This project is currently nascent. It doesn't do anything yet, but eventually
we hope it will do all the following things:

* Generate Swagger (OpenAPI) YAML files containing API documentation for 
 REST APIs
* Generate Akka Serverless based microservices to implement bounded contexts
* Generate Akka/HTTP server stubs
* Generate Akka/HTTP client library
* Generate Kafka server stubs
* Generate Kafka client library
* Generate graphQL based on domain model  
* Supporting a SaaS system for the generation of the above items working
 something like https://www.websequencediagrams.com/ by allowing direct
  typing and immediate feedback   
* Serve as the de facto (or real) standard for defining business domains and
  reactive systems.
* Be designed to be used with event storming
* Designed for a fully reactive implementation with messaging between
  contexts
* Support pluggable code generators for targeting different execution
 environments.
* Potential executors:  Akka Data Pipelines, Akka Serverless, Akka/Scala
* Incorporate the best interface language ideas from CORBA, Reactive
 Architecture, DDD, REST, DCOM, gRPC, etc. 
* Support for Read Projections and Read Models (plugins for databases)
* Support for graphQL and gRPC

## Dependencies

This codebase targets Java 17 and Scala 2.13.7 with -XSource:3 in preparation for 
Scala 3.0 code conversion. Moving to Scala 3 requires all dependencies to make 
the same transition:
* fastparse uses macros and is waiting for bugs in scala 3 to be fixed
* pureconfig is nearly ready for scala 3

## Generating Documentation
Documentation is now generated by sbt-site and its HugoPlugin for sbt.  However,
to read and write documentation it is generally better to work directly with hugo.
You need to make sure Hugo is installed:
```shell
brew install hugo  
```
Then run the hugo server:
```shell
cd doc/src/hugo
hugo server --disableFastRender -D
```
The hugo server command will ensure that writes to the source files will cause
the server to eject built pages from memory cache and rebuild them from the
source the next time they are requested. To view the site in your browser:
```shell
open http://localhost:1313/
```

## Ways To Test RIDDL
### ScalaTest
There are many test points already defined in language/src/tests/scala using
ScalaTest. In general, any change in language should be done in TDD style with
tests cases written before code to make that test case pass.  This is how the
parser and validator were created. That tradition needs to continue. 

### "Check" Tests
In `language/src/test/input` there are a variety of tests with `.check` files
that have the same basename as the `.riddl` file. The `.check` files have 
the error output that is expected from validating the correctly parsing `.
riddl` file. This way we can identify changes in error and messages. These 
tests are performed by the `CheckMessagesTest.scala` test suite which will
read all the riddl files in test/input/check and check them for validity and
compare the output messages to what's in the `.check` file. If there is no
corresponding `.check` file then the `.riddl` file is expected to validate 
cleanly with no errors and no warnings.

This is where most regression tests should be added. The input should be 
whittled down to the smallest set of definitions that cause a problem to 
occur, and then it should succeed after the regression is fixed.  

### "Examples" Tests
In `examples/src/test/scala` there is a `CheckExamplesSpec.scala` which runs 
the parser and validator on the examples in `examples/src/riddl`. Each 
sub-directory there is a separate example. They are expected to parse and 
validate cleanly without issue 
