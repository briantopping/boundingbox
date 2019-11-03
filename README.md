# Overview
The application is described in [this specification](./BoundingBox.md).

# Building and Running
* `sbt assembly` will provide a JAR with main method manifest that can be directly run.
* The JAR can be run, for instance `java -jar backend/target/scala-2.12/boundingbox-assembly-1.0.0-SNAPSHOT.jar < backend/src/test/data/sample2.txt`
* Running it with the `-i` flag will start a web UI, `-f` will allow a file to be used for input instead of `stdin`. At this writing, the web UI is mostly working, but being integrated so multiple clients of the server will see real-time updates of each other's changes using persistent sockets.

# Branches
The webapp has a nice page on this, but basically:
* The `initial` branch was the work on the puzzler itself.
  * `kd-tree` is a sub-tree that was aborted
* `streams` is the initial Akka Streams implementation once the puzzler was satisfactorily solved.
* `master` (previously called `ui`) is the integration of an Angular UI to the `streams` work.
