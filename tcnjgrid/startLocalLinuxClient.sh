#!/bin/bash

# Check number of arguments [hostname] [serverPortNumber] [sshTunnelPortNumber] [javaClassDirectory] [clientKey] [clientPassword]
if [ $# = 6 ]
then
	hostname=$1;			# Hostname of server
	portNum=$2;				# Server port number
	tunnelPort=$3;		# Port number to use for SSH tunnel
	javaClassDir=$4;	# Root directory for the java class files
	clientKey=$5;			# Client map key
	clientPswd=$6;		# Client map password
	
	# Go to java class directory
	#cd $javaClassDir;
	
	# Create ssh tunnel
	#  -N causes it to not execute a command on the server
	#  -o 'StrictHostKeyChecking=no' Prevents messages asking about the host key.
	echo "Opening SSH tunnel."
	ssh -N -o 'StrictHostKeyChecking=no' -L $tunnelPort:localhost:$portNum -L `expr $tunnelPort + 1`:localhost:`expr $portNum + 1` "$hostname" &
	
	# Get process id of ssh tunnel
	sshPid=$!;
	
	# Run the client
	echo "Starting client program."
	echo 1 | java edu/tcnj/TGrid/Main -client -t $tunnelPort -s localhost -u default -p default -clientKey $clientKey -clientPswd $clientPswd > `hostname`.log;
	
	# Kill the shh tunnel
	echo "Closing SSH tunnel."
	kill $sshPid;
fi
