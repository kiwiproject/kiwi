#!/usr/bin/env bash
# Deploy maven artifact in current directory into Maven central repository using
# maven-release-plugin goals (https://maven.apache.org/maven-release/maven-release-plugin/)

function print_usage() {
  echo "Usage: $0 -h -t"
  echo
  echo '-h: print this help'
  echo '-s: skip confirmation prompt'
  echo '-t: run tests (they will run twice, once during release:prepare and a second time during release:perform)'
}

# set default values
confirm_release=1
run_tests=0

# process arguments
while getopts 'hst' opt; do
  case "$opt" in
    h)
      print_usage
      exit 0
      ;;

    s)
      confirm_release=0
      ;;

    t)
      run_tests=1
      ;;

    *)
      print_usage
      exit 1
      ;;
  esac
done

# ensure the current branch is master or main
echo "Checking branch is 'master' or 'main'"
current_branch=$(git rev-parse --abbrev-ref HEAD)
if [ "$current_branch" != 'master' ] && [ "$current_branch" != 'main' ]; then
  echo 'WARNING: Must be on the master or main branch to release'
  exit 1
fi

# fetch latest information from remote repository and ensure there are no remote changes
echo 'Checking repository is up to date'
git fetch
status=$(git status)
if [[ "$status" == *"Your branch is up to date"* ]]; then
  echo 'Repository is up to date'
else
  echo 'WARNING: Repository is not up to date. Run git pull --rebase and then re-run this script.'
  exit 1
fi

# ensure there are no local modifications
if [[ "$status" == *"Changes not staged for commit"* ]]; then
  echo 'WARNING: Cannot release because there are local modifications'
  exit 1
fi

# confirm release unless configured to skip confirmation
if [[ "$confirm_release" -eq 1 ]]; then
  read -r -p 'Really deploy to maven central repository (yes/no)? '
  confirmation="${REPLY}"
else
  confirmation='yes'
fi

# perform release, or exit if release not confirmed
if [[ "$confirmation" == 'yes' ]]; then
  if [[ "$run_tests" -eq 0 ]]; then
    echo 'Tests are skipped during this release'
    extra_args=(-Darguments=-DskipTests)
  else
    echo 'Tests will be run during this release'
    extra_args=()
  fi

  echo "Running Maven to perform release (logging to console and maven-central-deploy.log)"
  mvn "${extra_args[@]}" release:clean release:prepare release:perform -e | tee maven-central-deploy.log
else
  echo 'Exit without deploy'
fi
