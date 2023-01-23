#!/usr/bin/env bash
# Deploy maven artifact in current directory into Maven central repository
# using maven-release-plugin goals

# check if the current branch is master or main
current_branch=$(git rev-parse --abbrev-ref HEAD)
if [ "$current_branch" != "master" ] && [ "$current_branch" != "main" ]; then
  echo "WARNING: Not on the master or main branch."
  exit 1
fi

# fetch the latest information from the remote repository
git fetch

# check the status of the repository
status=$(git status)

# check if there are any changes
if [[ "$status" == *"Your branch is up to date"* ]]; then
  echo "Repository is up to date."
else
  echo "WARNING: Repository is not up to date."
  exit 1
fi

read -r -p "Really deploy to maven central repository  (yes/no)? "

if [[ "$REPLY" == "yes" ]]; then
  mvn release:clean release:prepare release:perform -e | tee maven-central-deploy.log
else
  echo 'Exit without deploy'
fi
