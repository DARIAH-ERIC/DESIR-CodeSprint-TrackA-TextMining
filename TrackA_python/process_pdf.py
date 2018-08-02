import re
import os
import argparse
import pycurl
import io
import argparse
import fnmatch
import random
import pandas as pd
import numpy as np
from collections import Counter
import matplotlib.pyplot as plt
from scipy.interpolate import spline

parser = argparse.ArgumentParser(description='Process some pdfs.')

parser.add_argument('-input', metavar='N', type=str,
                    help='folder with pdf')
parser.add_argument('-output', type=str,
                    help='folder to write tei-xml')

args = parser.parse_args()


def get_pdf_paths(infolder,end):
	files = list()
	for root, dirnames, filenames in os.walk(infolder):
        	for filename in fnmatch.filter(filenames, "*."+end):
            		files.append(os.path.join(root, filename))

	files.sort()
	return files

def get_grobid_output(filepath):
	filepath = filepath.encode("utf-8")
	c = pycurl.Curl()
	buffer = io.BytesIO()
	c.setopt(c.VERBOSE, True)
	c.setopt(c.URL, 'http://localhost:8070/api/processFulltextDocument')
	c.setopt(c.HTTPPOST, [('input', (c.FORM_FILE, filepath)),('consolidateCitations',"1")])
	#c.setopt(c.HTTPPOST, [('consolidateCitations', 1)])
	c.setopt(c.WRITEFUNCTION, buffer.write)
	c.perform()
	c.close()


	body = buffer.getvalue()
	return body


def write_xml(data,outpath):
	outpath = re.sub("\.pdf",".xml",outpath)
	with open(outpath,"w") as file:
		file.write(data.decode("utf-8"))

def clean(text):
    text = re.sub("<[^>]+>","",text)
    text = re.sub("^\s+|\s+$","",text)
    return text


def parse_tei(filename):
    data = open(filename)
    refs = []
    ref = []
    start = False
    title = ""
    name = ""
    date = ""
    names = []
    year = ""
    art_name = re.sub(".*\/","",filename)
    old_ref = {"title": "", "name": "", "date": "", "filename": art_name,"year_pub": ""}
    for line in data:
        if re.match(".*<date",line) and start == False:
            
            year = re.sub(".*when\=\"","",line)
            year = re.sub("\".*","",year)[0:4]
           
		



        if start == False and re.match(".*<back",line):
            start = True
        if start == False:
            continue
            
        if re.match(".*<biblStruct",line):
            
            if title == "":
                continue
            ref = {"title": title, "name": names, "date": date, "filename": art_name,"year_pub": year}   
            
            if ref["title"] == old_ref["title"]:
            
            
                continue
            else:
                refs.append(ref)
                olf_ref = ref
                names = []
        if re.match(".*<title.*type=\"main\"",line):
            title = clean(line)
        if re.match(".*<persName",line):
            forename = re.sub("<\/forename.*","",line)
            forename = clean(forename)
            
            surname = re.sub(".*<surname","",line)
            surname = clean(surname)
            surname = re.sub(">",". ",surname)
            name = forename+surname
            names.append(name)
            
        if re.match(".*<date",line):
            date = re.sub(".*when\=\"","",line)
            date = re.sub("\".*","",date)
            date = date[0:4]
    
    return refs

def random_color():
    rgbl=[255,0,0]
    random.shuffle(rgbl)
    return tuple(rgbl)

def plot_citation_history(frame):

	frame["year_pub"] = frame["year_pub"].apply(lambda x: x[0:3]+"0")
	frame["date_norm"] = frame["date"].apply(lambda x: x[0:3]+"0")

	print(frame)
	year_dict = {}
	counter_filter = {}
	for y in np.unique(list(frame["year_pub"])):
		
		counter=Counter(frame[frame["year_pub"] == y]["date"])
		counter_filter = {}
		for k in counter.keys():
			try:
				if int(k) <= int(y) and int(k) > 1400:
					counter_filter.update({int(k):counter[k]})
			except:
				continue
        
		year_dict.update({y: counter_filter})

	f, ax = plt.subplots(figsize=(24,8))
	
	for key in year_dict.keys():
		print(key)
		if key == "			0":
			break
		c=np.random.rand(3,)
		vals = year_dict[key]
		years = list(vals.keys())
		counts = vals.values()
		pframe = pd.DataFrame(years)
		pframe["counts"] = counts
		pframe["years"] = years
		pframe = pframe.sort_values(by="years")
 
		ax.plot(pframe["years"], pframe["counts"])
		ax.fill_between(pframe["years"], 0, pframe["counts"], alpha=0.3, color = c)                       
		try:
			plt.axvline(x=int(key), color=c, lw=5)
		except:
			pass
		plt.savefig("viz.png")


files = get_pdf_paths(args.input,"pdf")
refs_all = []

for filename in files:
	print("processing: "+filename)
	#xml = get_grobid_output(filename)
	#write_xml(xml,re.sub(".*\/",args.output+"/",filename))

files = get_pdf_paths(args.output,"xml")

for filename in files:
	refs = parse_tei(filename)
	refs_all.append(refs)


refs_all = [x for y in refs_all for x in y]
frame = pd.DataFrame(refs_all).drop_duplicates("title")

frame.to_json("example.json")
plot_citation_history(frame)

