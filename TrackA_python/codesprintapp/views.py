from flask import Flask
from flask import request, session, render_template, json, Response, jsonify, make_response, send_file, redirect, url_for
import requests

app = Flask(__name__)

@app.route('/')

def index():
	return render_template('process_fulltext.html')


@app.route('/process_fulltext', methods = ['GET', 'POST'])

def process_fulltext():
	upload = request.files.get('file', '').read() #puts the uploaded file (in the request)
	url = 'http://localhost:8070/api/processFulltextDocument'
	files = dict(input=upload, teiCoordinates="biblStruct")
	r = requests.post(url, files=files)
	return render_template('process_fulltext.html', r=r.text)
	

if __name__ == '__main__':
	app.run(debug=True)