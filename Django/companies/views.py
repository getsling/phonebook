from django.http import HttpResponse, HttpResponseNotFound
from companies.models import Company, Employee, Feed
from django.shortcuts import render_to_response, get_object_or_404
from django.core import serializers
from django.core.mail import send_mail

import json
import csv
from datetime import datetime

def index(request):
	all_companies = Company.objects.all()
	return render_to_response('companies/index.html', {'companies': all_companies})

def detail(request, company_id):
	c = get_object_or_404(Company, pk=company_id)
	return render_to_response('companies/detail.html', {'company':c})

def employee_json(request, company_id):
	
	company = get_object_or_404(Company, pk=company_id)
	lastmodified = company.last_modified
	meta = request.META['HTTP_LAST_MODIFIED']
	print(meta)
	#if lastmodified > request.META.lastmodified:
	data = serializers.serialize("json", Employee.objects.filter(company_id=company_id))

	return HttpResponse(data, content_type='application/json')
	#return HttpResponseNotModified

def feed_json(request, company_id):
	data = serializers.serialize("json", Feed.objects.filter(company_id=company_id))
	return HttpResponse(data, content_type='application/json')

def config(request,company_id):
	data = {}
	
	feeds = Feed.objects.filter(company_id=company_id)
	for feed in feeds:
		data[feed.feedType] = feed.url
	return HttpResponse(json.dumps(data), content_type='application/json')

def contact_us(request,company_id):
	if request.method == 'POST':
		c = get_object_or_404(Company, pk=company_id)
		if c.support_email is not None:
			send_mail('Support request from Kompany', request.POST.get('message'), request.POST.get('from'),[c.support_email], fail_silently=True)
			return HttpResponse("Success")
	return HttpResponseNotFound("Go away")


def bulk_upload(request,company_id):
	if request.method == 'POST':
		c = get_object_or_404(Company, pk=company_id)		
		f = request.FILES['employees']
		if f is not None:
			
			timestamp = datetime.now().strftime("%d%m%y-%H%M")

			uploaded = open(os.path.join(settings.MEDIA_ROOT,"upload_%d_%s.csv"%(company_id,timestamp)), 'wrb+')

        	for chunk in f.chunks():
   		        uploaded.write(chunk)

			employeeReader = csv.reader(uploaded)
			if employeeReader is not None:
				existing = Employees.objects.all()
				employeeWriter = csv.writer(open(os.path.join(settings.MEDIA_ROOT,"backup_%d_%s.csv"%(company_id,timestamp)), 'wb+'))
				for employee in existing:
					employeeWriter.writeRow([employee.name, employee.email, employee.mobilephone, employee.workphone, employee.imageurl])				
				employees = [Employee(company=company_id,name=row[0],email=row[1],mobilephone=row[2],workphone=row[3],imageurl=row[4]) for row in employeeReader]
				existing.delete()
				Employees.bulk_create(employees)
				#TODO stick this in the admin tree
				#	redirect somewhere sensible
				#	post a message
				return HttpResponseRedirect('/')


	return HttpResponseNotFound("Go away")

