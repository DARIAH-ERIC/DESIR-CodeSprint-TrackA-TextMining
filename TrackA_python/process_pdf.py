import re
import os
import argparse
import pycurl
import io
import argparse
import fnmatch
parser = argparse.ArgumentParser(description='Process some pdfs.')

parser.add_argument('-input', metavar='N', type=str,
                    help='folder with pdf')
parser.add_argument('-output', type=str,
                    help='folder to write tei-xml')

args = parser.parse_args()


def get_pdf_paths(infolder):
	files = list()
	for root, dirnames, filenames in os.walk(infolder):
        	for filename in fnmatch.filter(filenames, '*.pdf'):
            		files.append(os.path.join(root, filename))

	files.sort()
	return files

def get_grobid_output(filepath):

	c = pycurl.Curl()
	buffer = io.BytesIO()
	c.setopt(c.VERBOSE, True)
	c.setopt(c.URL, 'http://localhost:8070/api/processFulltextDocument')
	c.setopt(c.HTTPPOST, [
	    ('input', (
		c.FORM_FILE, filepath,
	    )),
	])
	c.setopt(c.WRITEFUNCTION, buffer.write)
	c.perform()
	c.close()


	body = buffer.getvalue()
	return body


def write_xml(data,outpath):
	outpath = re.sub("\.pdf",".xml",outpath)
	with open(outpath,"w") as file:
		file.write(data.decode("utf-8"))



files = get_pdf_paths(args.input)
for filename in files:
	print(filename)
	xml = get_grobid_output(filename)

	write_xml(xml,re.sub(".*\/",args.output+"/",filename))





