#!/usr/bin/env python
# Given a word count file, generate an ordered histogram (assume the word list is ordered in assending order)
import csv
from pylab import *
import re
def ziph(wordcountf,imagef):
	wordlines = []
	stringnumber = re.compile("(.*),([0-9]+)$")
	with open(wordcountf,"rb") as wordcountfile:
		lines = wordcountfile.readlines()
		for line in lines:
			try:
				wordlines += [stringnumber.match(line.strip()).groups()]
			except:
				print "Error"
	yarr = []
	index = 0
	modN = 100
	for x in wordlines:
		try:
			if(index % modN == 0):
				# if(modN > 101): modN -= 1
				yarr += [float(x[1])]
			index+=1
		except:
			print x
	xarr = array(range(len(yarr)))
	yarr = array(yarr)
	xlabel("Subsampled words (every 100th word)")
	ylabel("Log occurences")
	# xarr = [log(x) for x in xarr]
	yarr = [log(y) for y in yarr]
	plot(xarr,yarr)
	savefig(imagef)

if __name__ == '__main__':
	import sys
	ziph(sys.argv[1],sys.argv[2])