#!/bin/bash
cut -d ' ' -f 2-3 --output-delimiter '      ' | cut --complement -c 11-14 | cut --complement -d '?' -f 2- 
