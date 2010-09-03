#!/bin/bash
htp @ || exit 1
scp html/*.html  plaa,openrocket@web.sourceforge.net:htdocs/
