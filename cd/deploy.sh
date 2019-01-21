#! /usr/bin/env bash

# TODO comment this back in
#if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  CERT_DIR="${TRAVIS_BUILD_DIR}/cd/codesigning.asc"
  echo "Set certificate directory to ${CERT_DIR}"

  # This block unencrypts GPG signing keys and imports them into the build environment so they
  # can be used to sign artifacts produced by this build.

  # If any of these commands fail, exit so we get quick feedback from the build.
  set -e

  # The cert is stored as base64 to prevent interpretation issues in bash, so decode it and store
  # it to the cert directory.
  echo "${GPG_CERT_BASE64}" | base64 --decode -i - > "${CERT_DIR}"
  echo "Saved GPG cert to file"

  # Remove the unencrypted cert file inside a trap. This way, we'll always remove the cert even if
  # if the script fails.
  trap "rm ${CERT_DIR}; echo 'Removed cert file'; " EXIT

  # import these certs into GPG
  gpg --fast-import "${CERT_DIR}"
  echo "Imported certs into GPG"

  # Run the deploy phase (which will sign any artifacts and publish to Sonatype).
  # The build will use the GPG certs we imported above
  mvn deploy -P sign,build-src-and-docs -DskipTests=true --settings "${TRAVIS_BUILD_DIR}/cd/mvnsettings.xml"
#fi 

