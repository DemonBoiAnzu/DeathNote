version=$(grep -Po '<version>\K[^<]*' pom.xml)
readarray -t versionsplit <<<"$version"
mv "DeathNoteInternals/target/DeathNote-$versionsplit.jar" "server/plugins"