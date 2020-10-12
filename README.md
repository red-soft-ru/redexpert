![image](red_expert.png)

RedExpert 
============


RedExpert is based on [Execute Query](http://executequery.org/index.php) and works on Java. The main features are:

* Concurrent work with multiple databases
* Development and executing any SQL queries supported by DBMS in SQL editor. Showing result of selective queries.
* SQL query formatting
* Autocompletions of commands
* Designing database structure in visual constructor of ER-diagrams. Supporting of the Reverse Engineering and exporting the diagrams in different formats.

In additional to that in RedExpert implemented:

* Supports all versions of RedDatabase and Firebird "out of the box"
* Creating and altering any database object with visual editor
* Tracing any actions with database in real time by using trace manager
* User manager
* User and roles privileges manager
* Support of any kind of authentication

Unfortunately [documentation](http://reddatabase.ru/documentation/) is now available only in Russian. Feel free to ask us about English version. It will force us to translate.

## Acknowledgements

Cross platform installer was built with [InstallBuilder](https://installbuilder.bitrock.com/) provided by [BitRock](https://bitrock.com/).

[![image](installer/logos/installersby_tiny.png)](https://installbuilder.bitrock.com/)

## Download RedExpert

http://reddatabase.ru/downloads/redexpert/


## Building RedExpert from source

RedExpert requires at least a Java 8 JDK installed and maven

To build a project you need create a jar file:

```sh
$ mvn package
```

To run RedExpert go to target directory and run:

```sh
$ java -jar RedExpert.jar
```

## Feedback

Feedback is very welcome and encouraged. Please use  the feedback dialog within the application
itself at Help | Feedback. 

If submitting a bug, please include any exception stack traces and other 
relevant information so that the issue can be more promptly resolved (ie. 
database, driver, OS, Java version etc).

Your email address is important (though optional). Bug reports are often 
received with information that needs to be clarified. Your name and contact 
details are held with the strictest confidence. Its also much easier to service
any submission if you can be easily raeched.

Please do not hesitate to submit any comments, bugs or feature requests. We
respond to ALL submissions.

## License

RedExpert is available completely free of charge and will remain so under 
the GNU Public License - http://www.gnu.org/copyleft/gpl.html

Other relevant license files for respective libraries are incldued in this 
directory as well as within the deployed application path. 

```
 Copyright (C) 2002-2019 Takis Diakoumis
 Copyright (C) 2018-2019 RED SOFT

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
