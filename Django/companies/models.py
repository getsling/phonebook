from django.db import models

class Company(models.Model):
	name = models.CharField(max_length=200)
	address = models.CharField(max_length=200)
	last_modified = models.DateTimeField()
	support_email = models.EmailField(max_length=254)
	def __unicode__(self):
		return self.name

class Employee(models.Model):
	company = models.ForeignKey(Company)
	name = models.CharField(max_length=200)
	email = models.CharField(max_length=200)
	imageurl = models.URLField()
	mobilephone = models.CharField(max_length=50)
	workphone = models.CharField(max_length=50) 
	def __unicode__(self):
		return self.name

class Image(models.Model):
	employee = models.ForeignKey(Employee)
	image = models.ImageField(upload_to='images')
	def __unicode__(self):
		return self.employee.name

class Feed(models.Model):
	FEED_CHOICES = (
        ('TW', 'Twitter'),
        ('RS', 'RSS'),
        ('TU', 'Tumblr'),
    )
	company = models.ForeignKey(Company)
	url = models.CharField(max_length=400)
	feedType = models.CharField(max_length=2, choices=FEED_CHOICES)
	def __unicode__(self):
		return self.feedType
