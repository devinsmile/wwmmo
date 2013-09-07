#!/bin/bash
#
# Builds the .proto files and generates the required java/python code.
#

java -jar ../libs/wire-compiler-1.0.2-SNAPSHOT-jar-with-dependencies.jar \
  --proto_path=. --java_out=../src *.proto
