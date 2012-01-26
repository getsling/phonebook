#!/usr/bin/python
BASE_URL = "http://mannvit.is"
STAFF_URL = "%s/Mannvit/Starfsmenn/" % (BASE_URL,)
FILENAME_TEMPLATE= 'mannvit'
MAX_WIDTH = 50
MAX_HEIGHT= 100

from lxml import etree
import urllib2, codecs, os, os.path, sqlite3, re, sys, tempfile, hashlib, json, urllib, Image

REX_FOREIGN = re.compile("(\(?\+\d+\)?)")
REX_NON_NUMERIC= re.compile("([^\d]+)")
REX_IMG = re.compile("url\('(.*)'\)")

def page_to_tree(url,debug=False,basepath=None):
	'''basepath and filename only required when debugging'''
	parser = etree.HTMLParser(recover=True)
	md5_filename = '%s-%s.html' % (FILENAME_TEMPLATE, hashlib.md5(url).hexdigest())
	filepath = '%s' % (tempfile.gettempdir(),)
	if debug and basepath:
		filepath = basepath
		if not os.path.isdir(basepath):
			os.makedirs(basepath)
	full_filename = '%s/%s' % (filepath,md5_filename)

	if not debug or (debug and not os.path.isfile(full_filename)):
		raw_html = urllib2.urlopen(url).read()
		f = open(full_filename,'w')
		f.write(raw_html)
		f.close()
	f = codecs.open(full_filename, encoding='UTF-8')
	raw_html = f.read()
	f.close()
	if not debug:
		os.remove(full_filename)

	return etree.fromstring(raw_html, parser)

def parse_phone(number):
	countrypart = ''
	match = REX_FOREIGN.match(number)
	# throw away multiple numbers
	number = number.split('/')[0]
	number = number.split(',')[0]
	localnum = number
	countrypart = "354"
	if match:
		countrypart = match.groups()[0]
		countrypart = REX_NON_NUMERIC.sub('',countrypart)
		localnum = number.replace(match.groups()[0],'')
		# assume space after country code in phone number
		#TODO: hack, this is pretty hardcoded
	localnum = REX_NON_NUMERIC.sub('',localnum)
	return '+%s %s' % (countrypart,localnum) if len(localnum) > 0 else ''

def print_usage_exit():
	sys.exit('Usage: ./%s --output=OUTPUT_PATH [--gen-thumbs=THUMB_PATH] [--debug --htmlcachepath=DEBUG_BASEPATH]' % (os.path.basename(sys.argv[0]),))

def main():
	basepath = None
	debug = False
	output_path = None
	thumb_path = None
	for arg in sys.argv[1:]:
		value = None
		argsplit = arg.split('=',1)
		parname = argsplit[0]
		if len(argsplit) > 1:
			value = argsplit[1]

		if parname == '--debug':
			debug = True
		elif parname == '--output':
			output_path = value
		elif parname == '--htmlcachepath':
			basepath = value
		elif parname == '--gen-thumbs':
			thumb_path = value
		else:
			print 'Unknown parameter specified: %s' % (parname,)
			print_usage_exit()

	if not output_path or (debug and not basepath):
		print 'Missing output_path or debug set without basepath'
		print_usage_exit()

	employee_data = get_employees(debug,basepath)

	if thumb_path:
		for e in filter(lambda x: 'image_url' in x and x['image_url'].find('.') > -1 and x['id'] == 84, employee_data['employees']):
			print "Processing thumb: %d" % (e['id'],)
			url = e['image_url']
			tmpfile = '%s/%s.%s' % (tempfile.gettempdir(),hashlib.md5(url).hexdigest(),url.rsplit('.',1)[-1])
			urllib.urlretrieve(url,tmpfile)
			try:
				im = Image.open(tmpfile)
				(width, height) = im.size
				ratio = float(width) / height
				new_width,new_height = MAX_WIDTH,MAX_HEIGHT
				if width > MAX_WIDTH:
					width = MAX_WIDTH
					height = width / ratio
				# should be rare since height is set rather high
				if height > MAX_HEIGHT:
					height = MAX_HEIGHT
					width = height * ratio
					
				im_out = im.resize((int(width),int(height)), Image.ANTIALIAS)

				if not os.path.isdir(thumb_path):
					os.makedirs(thumb_path)
				im_out.save('%s/img_%d.jpg' % (thumb_path,e['id'],),quality=90)
			except IOError:
				print "No image found"

	json_filename = '%s/%s.json' % (output_path, FILENAME_TEMPLATE)
	old_json = None
	try:
		old_json = json.loads(open(json_filename,'r').read())
	except (ValueError,IOError):
		pass

	if json.loads(json.dumps(employee_data)) != json.loads(json.dumps(old_json)):
		print "DB changed"
		conn = sqlite3.connect('%s/%s.sqlite' % (output_path,FILENAME_TEMPLATE))
		c = conn.cursor()
		c.execute('''DROP TABLE IF EXISTS division''')
		c.execute('''DROP TABLE IF EXISTS employee''')
		c.execute('''DROP TABLE IF EXISTS workplace''')
		c.execute('''DROP TABLE IF EXISTS android_metadata''')
		c.execute('''DROP VIEW IF EXISTS employeeInfo''')

		c.execute("""CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT 'en_US')""")
		c.execute("""CREATE TABLE division(_id INTEGER PRIMARY KEY, name VARCHAR(256))""")
		c.execute("""CREATE TABLE employee(_id INTEGER PRIMARY KEY, name VARCHAR(256), title VARCHAR(256), phone VARCHAR(64), mobile VARCHAR(64), email VARCHAR(256), image_url VARCHAR(1024), workplace_id INTEGER, division_id INTEGER,FOREIGN KEY (workplace_id) REFERENCES workplace(_id), FOREIGN KEY (division_id) REFERENCES division(_id))""")
		c.execute("""CREATE INDEX nIndex ON employee(name)""")
		c.execute("""CREATE TABLE workplace(_id INTEGER PRIMARY KEY, address VARCHAR(256))""")
		c.execute("""CREATE VIEW employeeInfo AS SELECT e._id AS _id, e.name AS employee, e.title AS title, e.phone AS phone, e.mobile AS mobile, e.email AS email, e.image_url AS image_url, w.address AS workplace, d.name AS division FROM employee e LEFT OUTER JOIN workplace w ON w._id = e.workplace_id LEFT OUTER JOIN division d ON d._id = e.division_id""")
		for workplace_name, workplace_id in employee_data['workplaces'].iteritems():
			c.execute("""INSERT INTO workplace (_id,address) VALUES (?,?)""", (workplace_id, workplace_name))

		for employee in employee_data['employees']:
			c.execute("""INSERT INTO employee(_id,name,title,phone,mobile,email,workplace_id,division_id,image_url) VALUES (?,?,?,?,?,?,?,?,?)""", (employee['id'],employee['name'],employee.get('title',''),employee.get('phone',''),employee.get('mobile',''),employee.get('email',''),employee.get('workplace',0),0,employee.get('image_url','')))
		c.execute('''INSERT INTO "android_metadata" VALUES('is_IS')''')
		conn.commit()
		c.execute('VACUUM')
		c.close()
		conn.close()

		f = open(json_filename,'w')
		f.write(json.dumps(employee_data))
		f.close()
	else:
		print "No DB change"

def get_employees(debug,basepath):
	tree = page_to_tree(STAFF_URL, debug, basepath)
	workplaces = {}
	divisions = {}

	staff_div = tree.find('.//div[@class="staff_search"]/div[@class="list"]')
	employees = []
	all_staff = staff_div.findall('.//tr')
	for staff_i,row in enumerate(all_staff):
		titleover = False
		cols = row.findall('./td')
		if len(cols) == 3:
			print "Fetching employee info (%d/%d)" % (staff_i+1,len(all_staff))
			stafflink = '%s%s' % (BASE_URL,cols[0].find('./a').get('href'))
			staff_id = int(stafflink.split('/')[-1])

			employee_tree = page_to_tree(stafflink, debug, basepath)
			employee_info = employee_tree.find('.//div[@class="starfsmenn_info"]')
			employee_data = employee_info.findall('./*')
			employee = {}
			state = None
			for i, item in enumerate(employee_data):
				value = ''.join(item.xpath('./text()')).strip()
				#print i,item,value,item.tag
				if type(item.tag) != str:
					continue
				if i == 0:
					itemtype = 'name'
					itemvalue = value
				else:
					subitem = item.find('./label')
					if subitem != None:
						titleover = True
						#<div><label>title</label>value</div>
						itemtype = subitem.text.strip()
						itemvalue = value
					else:
						if item.tag == 'label':
							titleover = True
							#<label>title</label><div>value</div>
							state = value
							continue
						elif state:
							itemtype = state
							link = item.find('./a')
							if(link != None):
								itemvalue = link.get('href')
							else:
								itemvalue = value
						else:
							if not titleover and item.tag == 'div':
								titleover = True
								itemtype = 'title'
								itemvalue = value
							else:
								continue
				employee['id'] = staff_id
				if itemtype.startswith('Vinnust'):
					if itemvalue in workplaces:
						workplace_id = workplaces[itemvalue]
					else:
						workplace_id = len(workplaces) + 1
						workplaces[itemvalue] = workplace_id
					employee['workplace'] = workplace_id
				elif itemtype.startswith('name'):
					employee['name'] = itemvalue
				elif itemtype.startswith('title'):
					employee['title'] = itemvalue
				elif itemtype.startswith('Beinn'):
					employee['phone'] = parse_phone(itemvalue)
				elif itemtype.startswith('GSM'):
					employee['mobile'] = parse_phone(itemvalue)
				elif itemtype.startswith('Netfa'):
					employee['email'] = itemvalue
				else:
					pass

			div_style = employee_info.get('style')
			if div_style:
				img_matches = REX_IMG.findall(div_style)
				if img_matches:
					employee['image_url'] = '%s%s' % (BASE_URL,img_matches[0])
			employees.append(employee)
		else:
			print "NOT 3 COLS!"

	return {'employees':employees,'workplaces':workplaces,'divisions':divisions}
	
if __name__ == "__main__":
	main()
