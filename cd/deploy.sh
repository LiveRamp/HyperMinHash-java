#! /usr/bin/env bash

# TODO comment this back in
#if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  CERT_DIR="${TRAVIS_BUILD_DIR}/cd/codesigning.asc";

  # This block unencrypts GPG signing keys and imports them into the build environment so they
  # can be used to sign artifacts produced by this build.

  # If any of these commands fail, exit so we get quick feedback from the build.
  set -e

  # The cert is stored as base64 to prevent interpretation issues in bash, so decode it and store
  # it to the cert directory.
  echo "${GPG_CERT_BASE64}" | base64 --decode -i - > "${CERT_DIR}"

  # Remove the unencrypted cert file inside a trap. This way, we'll always remove the cert even if
  # if the script fails.
  trap "rm ${CERT_DIR}" EXIT

  # import these certs into GPG
  gpg --fast-import "${CERT_DIR}"

  # Run the deploy phase (which will sign any artifacts). The build will use the GPG certs we
  # imported above
  mvn deploy -P sign,build-src-and-docs -DskipTests=true --settings cd/mvnsettings.xml
#fi TODO comment back in

