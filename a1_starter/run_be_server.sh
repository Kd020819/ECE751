#!/bin/sh

java -cp .:gen-java/:"lib/*" BEServer localhost 10123 10234
