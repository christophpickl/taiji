#!/bin/zsh

echo "Building website ..."
echo "[36m./gradlew clean build[0m"
echo
./gradlew clean build || exit 1

echo
echo "Deploying website ..."
echo "[36m./gradlew deploy[0m"
echo
./gradlew deploy || exit 1

echo
echo "[32mDeploy success 🙌🥳[0m"
echo "Check the website here:"
echo "http://localhost/~cpickl/taijiwiki/"

echo
echo "Post deploy check ..."
echo "[36m./gradlew -q linkChecker[0m"
echo
./gradlew -q linkChecker || exit 1

echo "All done and successful ✅"
exit 0
