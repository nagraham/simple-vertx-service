#!/usr/bin/env bash

# wipe out windows return characters
find src/ pom.xml -type f | xargs sed -i -e 's/\r//g'
