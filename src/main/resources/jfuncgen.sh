#!/bin/sh
CPATH=lib/@batchJarName@.jar:@classpath@
exec java -classpath $CPATH se.wetterstrom.jfuncgen.JFuncGen $*
