BASE_URL = "http://mannvit.is/"
BASE_PATH = "htmlfiles/"
STAFF_URL = "%sMannvit/Starfsmenn/" % (BASE_URL,)
DBFILE = "mannvit_staff.sqlite"

from lxml import etree
import urllib2, codecs, os.path, sqlite3

def page_to_tree(url,filename):
	parser = etree.HTMLParser(recover=True)
	if not os.path.isdir(BASE_PATH):
		os.makedirs(BASE_PATH)
	full_filename = '%s%s' % (BASE_PATH,filename)
	#print "FF",full_filename
	if not os.path.isfile(full_filename):
		raw_html = urllib2.urlopen(url).read()
		print "Downloading %s..." % (url,)
		f = open(full_filename,'w')
		f.write(raw_html)
		f.close()
	f = codecs.open(full_filename, encoding='UTF-8')
	raw_html = f.read()
	f.close()

	return etree.fromstring(raw_html, parser)

def main():
	main_filename = 'mannvit_staff.html'
	tree = page_to_tree(STAFF_URL, main_filename)
	workplaces = {}
	divisions = {}

	staff_div = tree.find('.//div[@class="staff_search"]/div[@class="list"]')
	employees = []
	for row in staff_div.findall('.//tr'):
		cols = row.findall('./td')
		if len(cols) == 3:
			stafflink = '%s%s' % (BASE_URL,cols[0].find('./a').get('href'))
			staff_id = int(stafflink.split('/')[-1])
			staff_filename = 'staff_%d.html' % (staff_id,)

			employee_tree = page_to_tree(stafflink, staff_filename)
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
						#<div><label>title</label>value</div>
						itemtype = subitem.text.strip()
						itemvalue = value
					else:
						if item.tag == 'label':
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
				elif itemtype.startswith('Beinn'):
					employee['phone'] = itemvalue
				elif itemtype.startswith('GSM'):
					employee['mobile'] = itemvalue
				elif itemtype.startswith('Netfa'):
					employee['email'] = itemvalue
				else:
					#print "NOT USED:",itemtype,itemvalue
					pass

			employees.append(employee)
		else:
			print "NOT 3 COLS!"
	conn = sqlite3.connect(DBFILE)
	c = conn.cursor()
	c.execute('''DROP TABLE IF EXISTS division''')
	c.execute('''DROP TABLE IF EXISTS employee''')
	c.execute('''DROP TABLE IF EXISTS workplace''')
	c.execute('''DROP TABLE IF EXISTS android_metadata''')
	c.execute('''DROP VIEW IF EXISTS employeeInfo''')

	c.execute("""CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT 'en_US')""")
	c.execute("""CREATE TABLE division(_id INTEGER PRIMARY KEY, name VARCHAR(256))""")
	c.execute("""CREATE TABLE employee(_id INTEGER PRIMARY KEY, name VARCHAR(256), title VARCHAR(256), phone VARCHAR(64), mobile VARCHAR(64), email VARCHAR(256), workplace_id INTEGER, division_id INTEGER,FOREIGN KEY (workplace_id) REFERENCES workplace(_id), FOREIGN KEY (division_id) REFERENCES division(_id))""")
	c.execute("""CREATE INDEX nIndex ON employee(name)""")
	c.execute("""CREATE TABLE workplace(_id INTEGER PRIMARY KEY, address VARCHAR(256))""")
	c.execute("""CREATE VIEW employeeInfo AS SELECT e._id AS _id, e.name AS employee, e.title AS title, e.phone AS phone, e.mobile AS mobile, e.email AS email, w.address AS workplace, d.name AS division FROM employee e, workplace w, division d WHERE w._id = e.workplace_id AND d._id = e.division_id""")
	for workplace_name, workplace_id in workplaces.iteritems():
		c.execute("""INSERT INTO workplace (_id,address) VALUES (?,?)""", (workplace_id, workplace_name))

	for employee in employees:
		c.execute("""INSERT INTO employee(_id,name,title,phone,mobile,email,workplace_id,division_id) VALUES (?,?,?,?,?,?,?,?)""", (employee['id'],employee['name'],employee.get('title',''),employee.get('phone',''),employee.get('mobile',''),employee.get('email',''),employee.get('workplace',0),0))

	conn.commit()
	c.execute('VACUUM')
	c.close()
	conn.close()

if __name__ == "__main__":
	main()
