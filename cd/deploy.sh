#! /usr/bin/env bash

#if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  # First, import necessary certs to sign release artifacts
  openssl aes-256-cbc -K $encrypted_9718d3d48d9b_key -iv $encrypted_9718d3d48d9b_iv -in ./codesigning.asc.enc -out ./codesigning.asc -d
  gpg --fast-import ./codesigning.asc
  rm cd/codesigning.asc

  # run the deploy phase and sign any artifacts
  mvn deploy -P sign,build-src-and-docs --settings cd/mvnsettings.xml
#fi
