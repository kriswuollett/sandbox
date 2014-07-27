#!/usr/bin/python
#
# Copyright (c) 2014, Kristopher Wuollett 
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the conditions are met in
# the license specified in the provided COPYING file.
#
# dupes_filter.py is simple script that can be used to reduce the
# duplicate number of rows in tab-separated standard input which has
# been presorted on the 3rd column.  Useful for consolidating notes
# that were duplicated across multiple sources and then merged.

prevDate = set() 
prevTags = set() 
prevText = None

def setJoin(a_set, a_char):
	return str(a_char).join(str(x) for x in a_set)

def writeOutput(dates, tags, text):
	print('{0}\t{1}\t{2}'.format(setJoin(dates, ' '), setJoin(tags, ' '), text))

for line in sys.stdin:
	line = line.split('\t', 3)
	date = line[0]
	tags = line[1]
	text = line[2]	

	if prevText == None:
		prevText = text

	if text == prevText:
		prevDate.add(date)
		for tag in tags.split(' '):
			prevTags.add(tag)
	else:
		writeOutput(prevDate, prevTags, prevText)
		prevDate = set()
		prevTags = set()
		prevDate.add(date)
		for tag in tags.split(' '):
			prevTags.add(tag)
		prevText = text

if prevText != None:
	writeOutput(prevDate, prevTags, prevText)
