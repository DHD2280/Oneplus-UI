#!/bin/bash

NEWVERNAME=${GITHUB_REF_NAME}

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
cat releaseChangeLog >> release.msg
echo 'RMessage<<EOF' >> $GITHUB_ENV
cat release.msg >> $GITHUB_ENV
echo 'EOF' >> $GITHUB_ENV