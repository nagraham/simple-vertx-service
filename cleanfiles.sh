#!/usr/bin/env bash

# wipe out windows return characters
find src/ pom.xml README.md -type f | xargs sed -i -e 's/\r//g'
