from django.conf.urls import patterns, include, url
from django.contrib import admin

urlpatterns = patterns('companies.views',
	url(r'^$', 'index'),
	url(r'^(?P<company_id>\d+)/$', 'detail'),
	url(r'^(?P<company_id>\d+)/employee_json/$', 'employee_json'),
	url(r'^(?P<company_id>\d+)/feed_json/$', 'feed_json'),
	url(r'^(?P<company_id>\d+)/config/$','config'),
	url(r'^(?P<company_id>\d+)/contact/$','contact_us'),
)