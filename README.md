# swingsane-package
Packaging scripts for [SwingSane](https://github.com/rquast/swingsane).

## Requirements

1. SwingSane source code.
1. Maven 2.
2. JDK 7 or higher.
3. Apache Ant.
4. Nullsoft Scriptable Install System (NSIS) - [http://nsis.sourceforge.net/Download](http://nsis.sourceforge.net/Download)
5. Java JRE (8u31) distribution files for Windows, Mac and Linux - [http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

## Packaging Steps

1. Install NSIS
2. Copy the JRE distribution files to the jre directory in the project, or symlink them there.
3. Install SwingSane to your local repository from the SwingSane source code:
<code>mvn clean install</code>
4. Package SwingSane with swingsane-package from the swingsane-package base directory:
<code>mvn clean package</code>
5. The distribution files will end up in basedir / target.
