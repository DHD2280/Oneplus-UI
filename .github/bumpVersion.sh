#!/bin/bash

NEWVERCODE=$(($(cat app/build.gradle.kts | grep versionCode | tr -s ' ' | cut -d " " -f 4 | tr -d '\r')+1))
NEWVERNAME=${GITHUB_REF_NAME}

# sample
sed -i 's/versionCode.*/versionCode = '$NEWVERCODE'/' app/build.gradle.kts
sed -i 's/versionName =.*/versionName = "'$NEWVERNAME'"/' app/build.gradle.kts
# lib
sed -i 's/version =.*/version = "'$NEWVERNAME'"/' oneplusui/build.gradle.kts
sed -i 's/com.github.DHD2280:Oneplus-UI:.*/com.github.DHD2280:Oneplus-UI:'$NEWVERNAME'/' README.md

# module changelog
echo "**$NEWVERNAME**  " > newChangeLog.md
cat releaseChangeLog.md >> newChangeLog.md
echo "  " >> newChangeLog.md
cat CHANGELOG.md >> newChangeLog.md
mv  newChangeLog.md CHANGELOG.md

# release message
echo "**$NEWVERNAME**  " > release.msg
echo "  " >> release.msg
echo "*Changelog:*  " >> release.msg
cat releaseChangeLog.md >> release.msg
echo 'RMessage<<EOF' >> $GITHUB_ENV
cat release.msg >> $GITHUB_ENV
echo 'EOF' >> $GITHUB_ENV