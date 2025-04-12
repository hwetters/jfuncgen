#!/bin/sh
PROJNAME="JFuncGen"
MAINCLASS="se.wetterstrom.jfuncgen.JFuncGen"
PROJDIR="$HOME/git/jfuncgen"
JAVA_HOME="/opt/jdk-23"
M2REPO="$HOME/.m2/repository"

export JAVA_HOME

error()
{
  echo "$*" >&2
  zenity --error --text="$*" --title='$PROJNAME Failed!'
  exit 1
}

cd "$PROJDIR" || error "Failed to move to dir $PROJDIR"
[ -f "pom.xml" ] || error "No pom.xml file"

VER=`sed -n 's/.*[<]version[>]\([0-9][.][-0-9a-z.A-Z]*\)[<][/]version[>].*/\1/p' pom.xml | head -1`
PROJ_JAR="$PROJDIR/target/jfuncgen-$VER.jar"

if [ ! -f  "$PROJ_JAR" ]; then
  echo "[build]"
  mvn package -DskipTests || error "Failed to build"
fi

CP=`mvn dependency:build-classpath -Dmdep.includeScope=runtime -Dmdep.outputFile=/dev/stdout -q`
[ -n "$CP" ] || error "Failed to get dependencies"

if [ -r "$PROJ_JAR" ]; then
  [ -x "$JAVA_HOME/bin/java" ] || error "No JVM in $JAVA_HOME"
  echo "[run]"
  exec "$JAVA_HOME/bin/java" -Xmx4096m -classpath $PROJ_JAR:$CP $MAINCLASS $*
else
  error "Failed to find jar $PROJ_JAR"
fi
