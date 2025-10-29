#!/bin/bash

cd `dirname $0`

mkdir log

exec java -Xlog:gc=info:file=./log/gc.log:uptimemillis,pid:filecount=5,filesize=1m -XX:+UseG1GC \
    -Xms300m -Xmx600m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./log/heap.dump \
    -cp "../lib/*" -Dlog4j.configuration="file:../conf/log4j.properties" \
    -Djava.security.manager -Djava.security.policy="file:../conf/darwin.policy" \
    -Dpolyglot.engine.WarnInterpreterOnly=false \
    xin.manong.darwin.web.Application > stderr.out 2>&1 & disown