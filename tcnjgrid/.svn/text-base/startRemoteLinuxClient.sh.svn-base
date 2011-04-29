#!/bin/bash

# Check number of arguments [clientHostname] [serverPortNumber] [clientSshTunnelPortNumber] [javaClassDirectory] [clientKey] [clientPassword]
if [ $# = 6 ]
then
	hostname=$1;			# Hostname of client to connect
	portNum=$2;				# Server port number
	tunnelPort=$3;		# Port number to use for SSH tunnel
	javaClassDir=$4;	# Root directory for the java class files
	clientKey=$5;			# Client map key
	clientPswd=$6;		# Client map password
	
	# Create ssh tunnel
	#  -o 'StrictHostKeyChecking=no' Prevents messages asking about the host key.
	ssh -o 'StrictHostKeyChecking=no' "$hostname" "cd '$javaClassDir'; './startLocalLinuxClient.sh' '`hostname`' '$portNum' '$tunnelPort' '$javaClassDir' $clientKey $clientPswd;" > /dev/null &
	
	# Echo process id of ssh tunnel
	echo $!;
	exit 0;
fi

exit 1;
