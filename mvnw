#!/bin/sh
# Maven wrapper script for Hospital Management System
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
DIR=`dirname "$PRG"`

LOCAL_REPO="$DIR/.m2/repository"
mkdir -p "$LOCAL_REPO"

if command -v mvn >/dev/null 2>&1; then
    exec mvn -Dmaven.repo.local="$LOCAL_REPO" "$@"
elif [ -x "$DIR/.maven/bin/mvn" ]; then
    exec "$DIR/.maven/bin/mvn" -Dmaven.repo.local="$LOCAL_REPO" "$@"
else
    echo "Maven not found. Please install Maven or check .maven/bin/mvn"
    exit 1
fi
