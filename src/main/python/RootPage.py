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

def checkIfCountry(location):
    if 'sense' in location and 'fineSense' in location['sense']:
        if 'country' in location['sense']['fineSense']:
            return True

    return False

@route('/process', method="POST")
def process():

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
