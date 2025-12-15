import requests

def post_json(url: str, payload: dict, timeout=30):
    resp = requests.post(url, json=payload, timeout=timeout)
    resp.raise_for_status()
    return resp.json()