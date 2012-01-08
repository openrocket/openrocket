#!/bin/bash

#
# Perform all tests for the translation files.
#
# Usage:
#    ./scripts/checkTranslations.sh
#


# Test that keys used in Java files are present in English messages
find src/ -name "*.java" -exec ./scripts/verifyTranslationKeys.pl l10n/messages.properties {} +

