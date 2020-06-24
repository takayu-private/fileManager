#@echo off

set file=%1

mkdir Done
cscript strTurn.vbs %file%
