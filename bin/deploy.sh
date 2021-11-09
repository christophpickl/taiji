#!/bin/zsh

ARG=$1
case $ARG in
  "local") TARGET="local"
  ;;
  "remote") TARGET="remote"
  ;;
  *)
    echo "Wrong/no argument passed. Valid arguments: 'local' or 'remote'"
    exit 1
esac

TARGET_PARAM="-Ptarget=$TARGET"

echo "Building website ..."
echo "[36m./gradlew clean build $TARGET_PARAM[0m"
echo
./gradlew clean build $TARGET_PARAM || exit 1

echo
echo "Deploying website ..."
echo "[36m./gradlew deploy $TARGET_PARAM[0m"
echo
./gradlew deploy $TARGET_PARAM || exit 1

echo
echo "[32mDeploy success 🙌🥳[0m"
echo "Check the website here:"
echo "http://psywiki.scienceontheweb.net/"

echo
echo "Post deploy check ..."
echo "[36m./gradlew -q linkChecker $TARGET_PARAM[0m"
echo
./gradlew -q linkChecker $TARGET_PARAM || exit 1

echo "All done and successful ✅"
exit 0
