#!/bin/bash

# This is a very basic invocation script to serve as an example
# It assumes that java and groovy are in your path

export JAVA_OPTS="-XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC -XX:MaxPermSize=16m -Xms32m -Xmx64m"

DIR="$( cd "$( dirname "$0" )"/.. && pwd )"
CURDIR=`pwd`

cd $DIR/src/groovy

CP=$DIR/etc
for FILE in $DIR/lib/*.jar
do
    CP="$CP:$FILE"
done

groovy -classpath $CP com/narcisocerezo/gberry/gberry.groovy

cd $CURDIR
