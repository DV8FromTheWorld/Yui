#!/bin/bash
branch=$(git rev-parse --abbrev-ref HEAD)

buildResult=$(mvn install -q -DskipTests=true)

mkdir dl
cd dl
wget https://drone.io/github.com/DV8FromTheWorld/Yui/files/release/Yui-Latest.jar
wget https://drone.io/github.com/DV8FromTheWorld/Yui/files/release/build-date-latest.txt
wget https://drone.io/github.com/DV8FromTheWorld/Yui/files/release/Yui-Recommended.jar
wget https://drone.io/github.com/DV8FromTheWorld/Yui/files/release/build-date-recommended.txt
cd ..

mkdir release
cd release
if [[ $buildResult == *"[ERROR]"* ]]; then
    echo "there was an error!"
    echo -e "The last build failed, however the previous build's files are still available. The error message is below.\n$buildResult" > BUILD_FAILED_REASON.txt
    mv ../dl/* .
    exit 0
fi

if [[ $branch == "master" ]]; then
    mv ../target/Yui-LATEST.jar Yui-Recommended.jar
    mv ../target/classes/build-date.txt build-date-recommended.txt
    mv ../dl/Yui-Latest.jar Yui-Latest.jar
    mv ../dl/build-date-latest.txt build-date-latest.txt
elif [[ $branch == "develop" ]]; then
    mv ../target/Yui-LATEST.jar Yui-Latest.jar
    mv ../target/classes/build-date.txt build-date-latest.txt
    mv ../dl/Yui-Recommended.jar Yui-Recommended.jar
    mv ../dl/build-date-recommended.txt build-date-recommended.txt
else
    echo "We cannot handle the provide branch: $branch" > BUILD_FAILED_REASON.txt
    mv ../dl/* .
fi
