# Overview
The application is described in [this specification](./BoundingBox.md).

# Building and Running
* `sbt assembly` will provide a JAR with main method manifest that can be directly run.
* The JAR can be run, for instance `java -jar backend/target/scala-2.12/boundingbox-assembly-1.0.0-SNAPSHOT.jar < backend/src/test/data/sample2.txt`

# Branches
The webapp has a nice page on this, but basically:
* The `initial` branch was the work on the puzzler itself.
  * `kd-tree` is a sub-tree that was aborted
* `streams` is the initial Akka Streams implementation once the puzzler was satisfactorily solved.
* `master` (previously called `ui`) is the integration of an Angular UI to the `streams` work.
