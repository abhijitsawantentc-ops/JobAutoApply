@echo off
title JobAutoApply Pro
echo Starting JobAutoApply Desktop...
start "" "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --app="file:///%~dp0desktop\index.html" --window-size=1280,820
exit
