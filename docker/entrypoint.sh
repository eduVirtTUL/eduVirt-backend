#!/bin/sh
#Check if any files with .cer extension are found
if test -n "$(find /eduVirt/certs -maxdepth 1 -name '*.cer' -print -quit)"; then
    for cert in /eduVirt/certs/*.cer; do
        keytool -import -alias "$(basename "$cert" .cer)" -file "$cert" -trustcacerts -cacerts -storepass changeit -noprompt
    done
else
  echo "No .cer files found in the specified directory. Skipping adding certificates"
fi

java -jar /eduVirt/eduVirt.jar