#!/bin/bash

git pull

/root/apache-maven-3.5.4/bin/mvn clean compile install -Dmaven.test.skip=true

cd ..

rm -rf quant-strategy-1.0.0-SNAPSHOT.jar

mv cryptocurrency-quant/quant-strategy/target/quant-strategy-1.0.0-SNAPSHOT.jar ./quant-strategy-1.0.0-SNAPSHOT.jar


PID=$(ps -ef|grep java|grep strategy|awk '{printf $2}')

if [ $? -eq 0 ]; then
    echo "process id:$PID"
else
    echo "process $input1 not exit"
    exit
fi

kill -9 ${PID}

if [ $? -eq 0 ];then
    echo "kill $input1 success"
else
    echo "kill $input1 fail"
fi

nohup java -jar quant-strategy-1.0.0-SNAPSHOT.jar >/dev/null 2>&1 &

echo "------success---------"