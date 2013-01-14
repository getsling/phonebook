from companies.models import Company,Employee,Feed,Image
from django.contrib import admin

class EmployeeInline(admin.TabularInline):
	model = Employee
	extra = 3

class CompanyAdmin(admin.ModelAdmin):
	inlines = [EmployeeInline]



admin.site.register(Company, CompanyAdmin)
admin.site.register(Feed)
admin.site.register(Image)
admin.site.register(Employee)