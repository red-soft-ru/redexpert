import re
import bs4
import requests
import sys
import urllib.request

args = sys.argv

DBMS = args[1]
ARCH = args[2]

if DBMS == "rdb50" or DBMS == "rdb30":
    response = requests.get(f"http://builds.red-soft.biz/release_hub/{DBMS}/")
    soup = bs4.BeautifulSoup(response.content, "html.parser")
    res = soup.find_all("a", href=re.compile(r"/release_hub/.+SNAPSHOT.+"))

    response = requests.get("http://builds.red-soft.biz" + res[0].attrs["href"])
    soup = bs4.BeautifulSoup(response.content, "html.parser")
    res = soup.find_all("a", href=re.compile(r".+windows-"+ ARCH + r".+exe"))

    url = "http://builds.red-soft.biz" + res[0].attrs["href"]
    
else:
    if ARCH == "x86_64":
        ARCH = "x64"
    if ARCH == "x86" and DBMS == "Firebird-3.0":
        ARCH = "Win32"

    firebird_releases_url = "https://api.github.com/repos/FirebirdSQL/firebird/releases"
    response = requests.get(firebird_releases_url)
    releases = response.json()
    url = ""
    print(DBMS + ARCH)
    for release in releases:
        for asset in release["assets"]:
            name = asset["name"]
            pattern = re.compile(f"{DBMS}" + r".+" + f"{ARCH}.exe")
            if pattern.match(name):
                url += asset["browser_download_url"]
                break
        if url != "":
            break

urllib.request.urlretrieve(url, 'installer.exe')