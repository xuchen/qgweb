#!/usr/bin/env zsh

set -eux
TARGET=question-generation.jar

rm -rf bin
mkdir -p bin

javac -cp $(print $(dirname $0)/lib/**/*.jar | tr ' ' :) -d bin src/**/*.java

cd bin
jar xf ../lib/commons-logging.jar
jar xf ../lib/jwnl.jar
jar xf ../lib/junit-3.8.2.jar
jar xf ../lib/stanford-parser-2008-10-26.jar
jar xf ../lib/supersense-tagger.jar
jar xf ../lib/weka-3-6.jar
jar xf ../lib/commons-lang-2.4.jar
jar xf ../lib/arkref.jar
jar xf ../lib/je-4.1.6.jar
jar xf ../lib/prefuse.jar
jar xf ../lib/bing-search-java-sdk.jar
jar xf ../lib/commons-math-2.0.jar
jar xf ../lib/wikinet.jar
jar xf ../lib/gson-1.4.jar
jar xf ../lib/xerces.jar


jar cf $TARGET *
cd ..

mv bin/$TARGET .



