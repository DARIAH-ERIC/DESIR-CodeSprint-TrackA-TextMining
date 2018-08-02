import hashlib
import xml.etree.ElementTree as ET
from sys import argv

import requests
from bottle import route, request, run, static_file, response

# Configuration
from nerd.nerd import NerdClient

nerdClient = NerdClient()

@route('/<filename:path>')
def server_static(filename):
    return static_file(filename, root='webapp')

@route('/process', method="POST")
def process(file):

    print(file)

    return {'result': 'bao'}

if len(argv) == 3:
    port = argv[2]
    host = argv[1]
elif len(argv) == 2:
    port = argv[1]
    host = '0.0.0.0'
else:
    print("Not enough parameters")
    exit(-1)

run(host=host, port=port, debug=True)
