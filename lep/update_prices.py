import requests
import os
os.chdir(os.path.dirname(__file__))

API_URL = "http://localhost:1851/api/prices/update_prices"
API_KEY = "SuperSecretKey"

with open("prices.yml", "r") as file:
    prices_data = file.read()

params = {"api_key": API_KEY}
headers = {"Content-Type": "application/x-yaml"}

put_request = requests.put(API_URL, params=params, data=prices_data, headers=headers)

print(put_request.status_code)
