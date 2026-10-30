#!/usr/bin/env bash
# Collects the newest APK from every repo of $OWNER and builds a signed F-Droid repo in ./repo.
# Needs: OWNER, STORE_REPO (this repo, owner/name), STORE_URL, KEY_FILE, KEY_PASS, APKSIGNER, gh logged in.
set -euo pipefail

rm -rf repo metadata tmp archive downloads
mkdir -p repo metadata config downloads
echo '{}' > sources.json

cat > config.yml <<CFG
repo_url: $STORE_URL
repo_name: ${OWNER}'s apps
repo_description: Every Android app published in github.com/${OWNER}. Updates install only when you tap Update.
repo_keyalias: store
keystore: $KEY_FILE
keystorepass: $KEY_PASS
keypass: $KEY_PASS
keydname: CN=${OWNER} app store
apksigner: $APKSIGNER
CFG
chmod 600 config.yml
printf 'My apps:\n  name: My apps\n' > config/categories.yml

gh api --paginate "users/$OWNER/repos?per_page=100&type=owner" \
  -q '.[] | select(.archived | not) | select(.private | not) | [.full_name, .html_url, (.description // "")] | @tsv' |
while IFS=$'\t' read -r full url desc; do
  [ "$full" = "$STORE_REPO" ] && continue
  apks=$(gh release view -R "$full" --json assets -q '.assets[].name | select(endswith(".apk"))' 2>/dev/null || true)
  if [ -z "$apks" ]; then
    echo "  $full: no APK in its latest release, skipped"
    continue
  fi
  dir="downloads/${full#*/}"
  gh release download -R "$full" -p '*.apk' -D "$dir"
  for f in "$dir"/*.apk; do
    name=$(basename "$f")
    [ -e "repo/$name" ] && name="${full#*/}-$name"
    mv "$f" "repo/$name"
    echo "  $full: added $name"
    python3 - "$name" "$full" "$url" "$desc" <<'PY'
import json, sys
name, full, url, desc = sys.argv[1:]
s = json.load(open("sources.json"))
s[name] = {"owner": full.split("/")[0], "url": url, "description": desc}
json.dump(s, open("sources.json", "w"))
PY
  done
done

ls repo/*.apk >/dev/null 2>&1 || { echo "::error::No APKs found in any repo of $OWNER."; exit 1; }

fdroid update --create-metadata
python3 "$(dirname "$0")/fill-metadata.py"
# Forget the first pass so the final index has no "diff" files (the store host is a flat file list).
rm -rf tmp/repo_*.json repo/diff
fdroid update
rm -rf repo/status repo/diff
echo "Apps in the store:"
python3 -c "import json; [print(' -', a.get('name'), a['packageName']) for a in json.load(open('repo/index-v1.json'))['apps']]"
