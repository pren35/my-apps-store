# My apps store

Your own F-Droid app store. Every day it looks at all your public GitHub repos, takes the
APK from each repo's latest release, and lists them here. A new app appears automatically
once its repo has a release with an `.apk` file.

**Store address (add this once in F-Droid):**
`https://pren35.github.io/my-apps-store/repo`

Nothing installs by itself: F-Droid shows new versions, and you tap **Update** to install them.
Keep F-Droid's *Settings → Automatically install updates* switched **off** to keep it that way.

## Files
- `.github/workflows/update-store.yml` – runs daily at 10:47 UTC, or by hand:
  Actions → Update app store → Run workflow.
- `scripts/update-store.sh`, `scripts/fill-metadata.py` – collect the APKs and build the store.
- The store is published on GitHub Pages (Settings → Pages → Source: **GitHub Actions**).
  F-Droid can't follow GitHub's download redirects, so release files can't be used directly.

## Secret (Settings → Secrets and variables → Actions)
- `STORE_KEY` – the whole contents of `store-key.txt` (signs the store's app list, so F-Droid
  can tell it really comes from you). Keep that file safe; if it changes, F-Droid needs the
  store re-added.

## Notes
- Private repos and archived repos are skipped. A repo without an `.apk` in its latest release is skipped.
- The app's own signing key (from each app's repo) is what Android checks for updates; this store
  never re-signs apps.
