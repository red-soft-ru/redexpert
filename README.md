ExecuteQuery
============

ExecuteQuery is a database query and introspection utility. Written entirely in 
Java, ExecuteQuery uses the flexibility and power and provided by Java Database 
Connectivity API (JDBC), to provide a simple and consistent way to interact with 
almost any database from simple queries to table creation and import/export of 
an entire schema/catalog's data.

ExecuteQuery was started many (many!) years ago to solve the basic problem at 
the time of jumping between different database applications and having to learn
different tool sets and techniques for interaction and development. Beginning as
not much more than a simple query and result application, ExecuteQuery slowly 
evolved to include a number of useful features supporting software developers 
and database administrators. 

Further information on ExecuteQuery and its features can be found at 
http://executequery.org with screenshots at http://executequery.org/screenshots

## Running ExecuteQuery

ExecuteQuery requires a minimum of Java 7 installed.

Current version build and installable packages (as well as bundled source 
archives) can be downloaded from http://executequery.org/download

ExecuteQuery is available in the following package formats:

* OS independant installable JAR file
* ZIP archive
* DEB package
* Gzip archive
* Gzip source archive

## Building ExecuteQuery from source

ExecuteQuery requires at least a Java 7 JDK installed and the current build uses
ant which can be downloaded from http://ant.apache.org

Run the build by simply executing `ant` from the source directory. 

A build should only take seconds and the resulting `eq.jar` file will be found 
in the current directory ready to use.

Start ExecuteQuery by either using the relevant start script for your OS - 
`eq.sh` or `eq.exe`. Mac users are probably best installing from the JAR 
installer and then incorporating any snapshot changes from this source after as 
this will provide the correct structure within `/Applications` including the 
correct `icns` icon file for ExecuteQuery.

Alternatively, simply start as follows:

```
  $ java -jar eq.jar
```

### Alternative Maven Build

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

## Feedback

Feedback is very welcome and encouraged. Please use either the form at 
http://executequery.org/feedback or the feedback dialog within the application
itself at Help | Feedback. 

We have set up ExecuteQuery on Google Groups to help consolidate queries and 
issues as well as provide an indexed support forum and mailing list. Please 
visit us at http://groups.google.com/group/executequery

If submitting a bug, please include any exception stack traces and other 
relevant information so that the issue can be more promptly resolved (ie. 
database, driver, OS, Java version etc).

Your email address is important (though optional). Bug reports are often 
received with information that needs to be clarified. Your name and contact 
details are held with the strictest confidence. Its also much easier to service
any submission if you can be easily raeched.

Please do not hesitate to submit any comments, bugs or feature requests. I 
respond to ALL submissions.

## License

Execute Query is available completely free of charge and will remain so under 
the GNU Public License - http://www.gnu.org/copyleft/gpl.html

Other relevant license files for respective libraries are incldued in this 
directory as well as within the deployed application path. 

```

 Copyright (C) 2002-2015 Takis Diakoumis

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.

```