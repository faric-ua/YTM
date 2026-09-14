# YTM Importer

Minimal Android app for importing track lists into YouTube / YouTube Music.

**CSV/TXT → search → review matches → create private playlist → open in YouTube Music.**

No ads. No analytics. No custom backend. No subscription.

## MVP features

- TuneMyMusic-style CSV import (`Track name`, `Artist name`, `Playlist name`)
- plain TXT import (`Artist - Track`)
- Google OAuth authorization
- YouTube Data API search
- automatic match scoring for artist/title/remix/edit terms
- manual candidate selection
- manual YouTube / YouTube Music URL override
- private playlist creation
- ordered playlist insertion
- open created playlist in YouTube Music
- copy manual replacement log for TikTok comments

## Security

Real signing material is intentionally excluded from Git:

- `ytm-importer-release.jks`
- `release-signing.properties`

For CI signing, use GitHub Actions Secrets. See `README_UA.md` and `OAUTH_SETUP.txt`.
