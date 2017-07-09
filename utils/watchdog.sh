#!/bin/bash

# Set up script variables
PID_FILE=/var/run/ilivalidator/ilivalidator.pid
HTTP_URL=http://127.0.0.1:8888/ilivalidator
START_SCRIPT=/etc/init.d/ilivalidator
PID=`cat $PID_FILE`

# Function to kill and restart application server
function ilivalidator_restart() {
  $START_SCRIPT stop
  sleep 5 
  kill -9 $PID
  $START_SCRIPT start
}

if [ -d /proc/$PID ]
  then
    # App server is running - kill and restart it if there is no response.
    wget $HTTP_URL -T 1 --timeout=20 -O /dev/null &> /dev/null
    if [ $? -ne "0" ]
      then
      echo Restarting ilivalidator because $HTTP_URL does not respond, pid $PID
      ilivalidator_restart
    fi
else
  # App server process is not running - restart it
  echo Restarting ilivalidator because pid $PID is dead.
  ilivalidator_restart
fi
