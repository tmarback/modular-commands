import os
import requests
import sys

number = sys.argv[1] # Milestone number

repo = os.environ['GITHUB_REPOSITORY'] # As in, user/repo
token = os.environ['GITHUB_TOKEN'] # Github API token

print( f"Fetching open issues of milestone {number} of repo '{repo}'", file = sys.stderr )

url = f'https://api.github.com/repos/{repo}/milestones/{number}'
headers = {
    'Accept': 'application/vnd.github.v3+json',
    'Authorization': f'token {token}'
}

r = requests.get( url, headers = headers )
if r.status_code != 200:
    raise Exception( f"HTTP request failed: code {r.status_code}" )
r = r.json()

open_issues = r['open_issues']
print( f"Open issues: {open_issues}", file = sys.stderr )

print( open_issues )