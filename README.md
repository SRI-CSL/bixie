Bixie - Inconsistent Code Detection for Java
=====
[![Build Status](https://travis-ci.org/martinschaef/bixie.png)](https://travis-ci.org/martinschaef/bixie)
[![Coverity Scan](https://scan.coverity.com/projects/5463/badge.svg)](https://scan.coverity.com/projects/5463)
[![Coverage Status](https://coveralls.io/repos/martinschaef/bixie/badge.svg?branch=master)](https://coveralls.io/r/martinschaef/bixie?branch=master) 


Check our **[Website](http://martinschaef.github.io/bixie/)** or the **[Online Demo](http://csl.sri.com/projects/bixie/)**.

Stable releases and experimental setups to repeat the experiments from our papers are in the [Release](https://github.com/martinschaef/bixie/releases) section. 

We recently changed our build system to [Gradle](https://gradle.org/).  After cloning the project, type:

    ./gradlew check

to compile and run the unit tests. If you don't have gradle installed, used the gradle wrappers `gradlew` or `gradlew.bat`. If you haven't used gradle before, you should look run `gradle tasks` to see what you can do with it. E.g., `gradle eclipse` builds an Eclipse project for Bixie.

Visit our **[Website](http://martinschaef.github.io/bixie/)** for a tutorial on how to use Bixie on your Java code.

Bixe uses [Jar2Bpl](https://github.com/martinschaef/jar2bpl) to turn Java into Boogie.
