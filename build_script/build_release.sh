#!/bin/bash

BUILD_NUMBER=$1
BUILD_NAME=graylog2-radio-$BUILD_NUMBER
BUILD_DIR=builds/$BUILD_NAME
BUILD_DATE=`date`
LOGFILE=`pwd`/logs/$BUILD_NAME

# Check if required version parameter is given
if [ -z $BUILD_NUMBER ]; then
  echo "ERROR: Missing parameter. (build number)"
  exit 1
fi

# Create directories
mkdir -p logs
mkdir -p builds
mkdir -p $BUILD_DIR

# Create logfile
touch $LOGFILE
date >> $LOGFILE

echo "BUILDING $BUILD_NAME"

# Add build date to release.
echo $BUILD_DATE > $BUILD_DIR/build_date

echo "Copying files ..."

# Copy files.
cp -R ../target/graylog2-radio.jar ../README.md ../COPYING $BUILD_DIR

# Copy example config files
cp ../misc/graylog2-radio.conf.example $BUILD_DIR/graylog2-radio.conf.example
cp ../misc/graylog2-radio-inputs.conf.example $BUILD_DIR/graylog2-radio-inputs.conf.example

# Copy control script
cp -R copy/bin $BUILD_DIR

mkdir -p $BUILD_DIR/log

cd builds/

# tar it
echo "Building Tarball ..."
gnutar cfz $BUILD_NAME.tar.gz $BUILD_NAME
rm -rf ./$BUILD_NAME

echo "DONE! Created graylog2-radio release $BUILD_NAME on $BUILD_DATE"
