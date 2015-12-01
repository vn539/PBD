#from requests import *
from django.shortcuts import render_to_response, redirect
from django.http import HttpResponse, HttpResponseRedirect
from django.template import RequestContext
from django import forms
from datetime import datetime
import ConfigParser
import subprocess
from os import remove
import locale
import csv
import sys
import requests
import json


def executesparkqueries(request):

	if request.method == 'GET':
		return render_to_response('ExecuteSparkQueries.html', {'NavMenu' : 'ExecuteSparkQueries'},context_instance=RequestContext(request))

	if request.method == 'POST':
		print request.POST
		queryOption = request.POST.__getitem__('queryOption')
		print ('queryOption: ' + queryOption)

		print "Ready to execute Spark Java program"
		process = subprocess.Popen("./runsparkquery.sh " + queryOption, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

		#wait for the process to terminate
		for line in process.stdout: process(line)
		errcode = process.returncode

		#print "errcode: "
		#print errcode

		# Parse weather data
		dataFile = open('/home/hdcuser/Desktop/test.txt')	       
		bStartRead = 0
		wds = []
		wd = None

		for line in dataFile:
			splitData = line.split('>')
			wd = WeatherData()

			if queryOption == '1':
				wd.lang = splitData[0]
				wd.count = splitData[1]

			if queryOption == '2':
				splitLatLng = splitData[0].split(', ')
				wd.lat = splitLatLng[0]
				wd.lng = splitLatLng[1]
				wd.screenname = splitData[1]
				wd.condition = splitData[2]

			if queryOption == '3':
				wd.condition = splitData[0]
				wd.timezone = splitData[1]
				wd.count = splitData[2]

			if queryOption == '4':
				wd.text = splitData[0]

			if queryOption == '5':
				wd.screenname = splitData[0]
				wd.count = splitData[1]

			if queryOption == '6':
				wd.country = splitData[0]
				wd.source = splitData[1]
				wd.count = splitData[2]


			if queryOption == '7':
				wd.screenname = splitData[0]
				wd.text = splitData[1]

			if queryOption == '8':
				wd.count = splitData[0]

			wds.append(wd)


		dataFile.close()


		print "Done executing Spark Java program"
		return render_to_response('ExecuteSparkQueriesResult.html', {'queryOption' : queryOption, 'wds' : wds},context_instance=RequestContext(request))



class WeatherData:
  def __init__(self):
    self.count = None
    self.lang = None
    self.location = None
    self.condition = None
    self.timezone = None
    self.text = None
    self.screenname = None
    self.country = None
    self.source = None
    self.lat = None
    self.lng = None


