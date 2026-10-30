# Fills each app's store listing (summary, source link, category) from its GitHub repo.
import json, sys, yaml

sources = json.load(open("sources.json"))          # apk file name -> repo info
index = json.load(open("repo/index-v1.json"))
for pkg, versions in index["packages"].items():
    info = next((sources[v["apkName"]] for v in versions if v["apkName"] in sources), None)
    if not info:
        continue
    path = f"metadata/{pkg}.yml"
    meta = yaml.safe_load(open(path)) or {}
    meta["Categories"] = ["My apps"]
    meta["AuthorName"] = info["owner"]
    meta["SourceCode"] = info["url"]
    meta["IssueTracker"] = info["url"] + "/issues"
    meta["Changelog"] = info["url"] + "/releases"
    if info.get("description"):
        meta["Summary"] = info["description"][:80]
    yaml.safe_dump(meta, open(path, "w"), sort_keys=True, allow_unicode=True)
    print(f"{pkg}: listed from {info['url']}")
