#!/bin/bash

COMMAND_PATH=`echo ${0} | sed -e "s/\(.*\)\/.*$/\1/g"`
cd ${COMMAND_PATH}

# if need to include a jre in future...
# JAVA_HOME=SWINGSANE_HOME/jre

if [ -z $JAVA_HOME ]; then
  JAVA_COMMAND=`which java`
  if [ "$?" = "1" ]; then
    echo "No executable java found. Please set JAVA_HOME variable";
    exit;
  fi
else
  JAVA_COMMAND=$JAVA_HOME/bin/java
fi
if [ ! -x $JAVA_COMMAND ]; then
  echo "$JAVA_COMMAND is not executable. Please check the permissions."
  exit
fi
$JAVA_COMMAND -jar -Xmx1024m SWINGSANE_HOME/lib/SWINGSANE_JAR "$1"
