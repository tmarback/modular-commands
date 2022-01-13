import os
import requests
import sys

url = sys.argv[1] # Project url

token = os.environ['GITHUB_TOKEN'] # Github API token

print( f"Fetching name of project at '{url}'", file = sys.stderr )

headers = {
    'Accept': 'application/vnd.github.inertia-preview+json',
    'Authorization': f'token {token}'
}

r = requests.get( url, headers = headers )
if r.status_code != 200:
    raise Exception( f"HTTP request failed: code {r.status_code}" )
r = r.json()

name = r['name']
print( f"Project name: {name}", file = sys.stderr )

print( name )