#!/bin/bash
tail -n100 /var/log/mine/info.log|grep "当前价格"  > /dev/null
if [ $? -eq 0 ]; then
    echo "Found!"
else
    echo "Not found!"
fi