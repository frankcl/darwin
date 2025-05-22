#!/bin/bash

cd `dirname $0`

mkdir log

exec java -Xlog:gc=info:file=./log/gc.log:uptimemillis,pid:filecount=5,filesize=1m -XX:+UseG1GC \
    -Xms100m -Xmx500m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./log/heap.dump \
    -cp "../jars/*" -Dlog4j.configuration="file:../conf/log4j.properties" xin.manong.darwin.spider.Application > stderr.out 2>&1 & disown