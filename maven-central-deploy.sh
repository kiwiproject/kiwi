#!/usr/bin/env bash
# Deploy maven artifact in current directory into Maven central repository
# using maven-release-plugin goals

read -r -p "Really deploy to maven central repository  (yes/no)? "

if [[ "$REPLY" == "yes" ]]; then
  mvn release:clean release:prepare release:perform -e | tee maven-central-deploy.log
else
  echo 'Exit without deploy'
fi
