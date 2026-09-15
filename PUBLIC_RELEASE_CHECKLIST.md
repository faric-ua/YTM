# YTM Importer — Public Release Checklist

This checklist is for giving the APK to people outside the developer/test group.

## App

- [x] First-run quick start.
- [x] In-app privacy explanation.
- [x] Public-facing About screen.
- [x] Main screen reduced to a clear 4-step flow.
- [x] Advanced tools moved behind `Ще` / `Сервіс`.
- [x] No ads / built-in analytics / own application server.
- [x] Signed APK verification and SHA-256.

## Google OAuth / YouTube Data API

Before broad public distribution:

- [ ] Google Cloud OAuth audience is configured for the intended external users.
- [ ] OAuth app is moved from Testing to Production when ready.
- [ ] All requested scopes are declared on the consent screen.
- [ ] Scope use has been submitted for verification when Google requires it.
- [ ] Prepare a scope justification explaining why playlist creation/addition
      needs the requested YouTube scope.
- [ ] Prepare the verification demo video showing sign-in/consent and the
      feature that uses the scope.
- [ ] Confirm support/developer contact details are current.
- [ ] Publish a public privacy-policy URL and use it in the OAuth app branding.
- [ ] Test sign-in using a Google account that is NOT one of the developer's
      existing test accounts.

Official references:

- https://developers.google.com/youtube/v3/guides/auth/installed-apps
- https://developers.google.com/identity/protocols/oauth2/production-readiness/policy-compliance
- https://support.google.com/cloud/answer/13461325
- https://support.google.com/cloud/answer/15549945

## Distribution test

- [ ] Install on a second person's Android phone.
- [ ] Install without Termux / developer tools.
- [ ] Google OAuth works for that person's account.
- [ ] Import sample TXT.
- [ ] Search tracks.
- [ ] Create Private test playlist.
- [ ] Open result in YouTube Music.
- [ ] Export a YTM Project.
- [ ] Relaunch app and verify History remains.

## Next UX stages

v1.1.0 is the public UX foundation, not the final UI redesign.

Next:
- separate History screen;
- separate Import/Review screen;
- Material 3 components;
- responsive layout / larger text;
- accessibility;
- consistent empty/loading/error states.

## v1.2.0 navigation progress

- [x] Dedicated History screen.
- [x] Searchable History list.
- [x] Normal Back navigation.
- [ ] Dedicated Data / Backup screen.
- [ ] Dedicated Import / Review screen.

## v1.2.1 navigation progress

- [x] Dedicated History screen.
- [x] Dedicated Data / Backup / Restore screen.
- [ ] Dedicated Pending Queue screen.
- [ ] Dedicated Import / Review screen.

## v1.2.2 navigation progress

- [x] Dedicated History screen.
- [x] Dedicated Data / Backup / Restore screen.
- [x] Dedicated Pending Queue screen.
- [ ] Dedicated Import / Review screen.

## v1.3.0 navigation progress

- [x] Dedicated Import screen.
- [x] Dedicated Review screen.
- [x] Persistent unfinished-workspace state.
- [ ] Dedicated destination/create screen.
- [ ] Remove legacy dialog code after full regression.

## v1.3.1 project safety

- [x] Working Project can be saved before History.
- [x] Manual URL selection protected from cache overwrite.
- [x] Current workspace clear action.
- [x] Current workspace included in Full Backup.
- [ ] Phone regression of manual URL + Project re-import.

## v1.4.0 destination progress

- [x] Dedicated destination/create screen.
- [x] New / Existing playlist selection.
- [x] Privacy selection on screen.
- [x] Existing-playlist search.
- [x] Duplicate preview and final choice.
- [ ] Phone regression of create + append + duplicates.
- [ ] Remove legacy destination dialogs after regression.

Open UX item Q-001 from v1.3.2 is intentionally deferred; see `OPEN_QUESTIONS.md`.
