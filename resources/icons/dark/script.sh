#!/bin/bash
FILES=./*
for f in $FILES
do
  convert $f +contrast $f
done
