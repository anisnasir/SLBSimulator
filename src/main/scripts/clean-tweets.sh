#!/bin/bash
sed 's/[^[:blank:]^[:alnum:]]*//g' | sed 's/ \+ / /g' | tr '[:upper:]' '[:lower:]'
