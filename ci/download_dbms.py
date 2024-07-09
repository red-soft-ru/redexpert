import re
import requests
import sys
import urllib.request

args = sys.argv

DBMS = args[1]
ARCH = args[2]
OS = args[3]


print("Download DBMS")

if "rdb" in DBMS:
    os = OS
    bin = "bin" if os == "linux" else "exe"

    if DBMS == "rdb30":
        last_stable_version = "3.0.15"

    if DBMS == "rdb50":
        last_stable_version = "5.0.0-rc.2"       

    url = f"http://builds.red-soft.biz/release_hub/{DBMS}/{last_stable_version}/download/red-database:{os}-x86_64-enterprise:{last_stable_version}:{bin}"

else:
    if ARCH == "x86_64":
        ARCH = "x64"
    if ARCH == "x86" and DBMS == "Firebird-3.0":
        ARCH = "Win32"

    firebird_releases_url = "https://api.github.com/repos/FirebirdSQL/firebird/releases"
    response = requests.get(firebird_releases_url, verify=False)
    releases = response.json()
    url = ""
    for release in releases:
        for asset in release["assets"]:
            name = asset["name"]
            pattern = re.compile(f"{DBMS}" + r".+" + f"{ARCH}.exe")
            if pattern.match(name):
                url += asset["browser_download_url"]
                break
        if url != "":
            break
    
urllib.request.urlretrieve(url, f'installer.{bin}')