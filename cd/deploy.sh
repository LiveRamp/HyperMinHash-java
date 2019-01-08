#! /usr/bin/env bash

# TODO comment this back in
#if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then

  # This block unencrypts GPG signing keys and imports them into the build environment so they
  # can be used to sign artifacts produced by this build.
  # Note that if any of these commands fail, we exit so we get quick feedback from the build.

  # First, unencrypt the certs we'll use to sign artifacts
  openssl aes-256-cbc -K $encrypted_9718d3d48d9b_key -iv $encrypted_9718d3d48d9b_iv -in $TRAVIS_BUILD_DIR/cd/codesigning.asc.enc -out $TRAVIS_BUILD_DIR/cd/codesigning.asc -d || exit 1;

  # import these certs into GPG
  gpg --fast-import $TRAVIS_BUILD_DIR/cd/codesigning.asc || exit 1;

  # Remove the unencrypted cert file
  rm cd/codesigning.asc || exit 1;

  # Run the deploy phase (which will sign any artifacts). The build will use the GPG certs we
  # imported above
  mvn deploy -P sign,build-src-and-docs -DskipTests=true --settings cd/mvnsettings.xml
#fi TODO comment back in

