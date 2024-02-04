@echo off
set CPATH=lib\@batchJarName@.jar;@classpath@
java -classpath %CPATH% se.wetterstrom.jfuncgen.JFuncGen %1
