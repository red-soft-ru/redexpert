# ExecuteQuery

Execute Query is an operating system independent database utility written entirely in Java.

Execute Query is available completely free of charge and will remain so under the GNU Public License.

### Version
4.3

### Requirements

For build of ExecuteQuery need the following tools:

* Java 1.7
* Apache Maven 3.3.1

Before the build of project you must set JAVA_HOME and M2_HOME.
Installation of the Maven is available here https://maven.apache.org/install.html

### Build and run

To build a ExecuteQuery:

```sh
$ mvn clean compile
```

To create a jar file:

```sh
$ mvn package
```

To run ExecuteQuery go to target directory and run:

```sh
$ java -jar ExecuteQuery-4.3.jar
```