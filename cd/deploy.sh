#! /usr/bin/env bash

#if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  # First, import necessary certs to sign release artifacts
  # Note that if any of these commands fail, we exit so we get quick feedback from the build.
  echo `pwd`
  openssl aes-256-cbc -K $encrypted_9718d3d48d9b_key -iv $encrypted_9718d3d48d9b_iv -in $TRAVIS_BUILD_DIR/cd/codesigning.asc.enc -out $TRAVIS_BUILD_DIR/cd/codesigning.asc -d || exit 1;
  gpg --fast-import $TRAVIS_BUILD_DIR/cd/codesigning.asc || exit 1;
  rm cd/codesigning.asc || exit 1;

  # run the deploy phase and sign any artifacts
  mvn deploy -P sign,build-src-and-docs --settings cd/mvnsettings.xml
#fi
