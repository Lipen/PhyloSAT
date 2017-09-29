#!/bin/bash

function antlr() {
    java -jar lib/antlr-4.7-complete.jar $*
}

set -v
antlr -o gen/beepp/parser -package beepp.parser -listener -no-visitor -lib src/beepp/parser $(realpath src/beepp/parser/BEEppLexer.g4)
antlr -o gen/beepp/parser -package beepp.parser -listener -no-visitor -lib src/beepp/parser $(realpath src/beepp/parser/BEEppParser.g4) $(realpath src/beepp/parser/BEEppLexer.g4)
ant -f phylosat.xml build.all.artifacts
