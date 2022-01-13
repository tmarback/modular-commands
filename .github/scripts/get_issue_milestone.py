import os
import requests
import sys

url = sys.argv[1] # Issue url

token = os.environ['GITHUB_TOKEN'] # Github API token

print( f"Fetching milestone of issue at '{url}'", file = sys.stderr )

headers = {
    'Accept': 'application/vnd.github.inertia-preview+json',
    'Authorization': f'token {token}'
}

r = requests.get( url, headers = headers )
if r.status_code != 200:
    raise Exception( f"HTTP request failed: code {r.status_code}" )
r = r.json()

if r['milestone'] is None:
    print( "Issue not currently associated with a milestone.", file = sys.stderr )
else:
    number = r['milestone']['number']
    name = r['milestone']['title']
    print( f"Current Milestone: {number} ({name})", file = sys.stderr )

    print( number )