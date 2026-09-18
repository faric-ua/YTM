# YTM Importer v1.4.34 — Back Navigation Alignment + Unified Modal Retest

- versionName: **1.4.34**
- versionCode: **68**
- status: **NOT PHONE-TESTED YET**
- focus: **secondary-screen back-button alignment + BUG-002 representative modal retest**

Real-phone evidence showed that the secondary-screen back control used the typographic character `‹` inside a normal Android `Button`. Android centered the text box, but the glyph itself remained optically off-center because of font metrics.

v1.4.34 replaces that text glyph with one 24dp vector arrow exposed through `UiChrome.backButton(...)`. Import, Data, History, Review, Service, Pending and Destination now use the same control with a 48×48dp touch target.

The v1.4.33 unified stable modal pipeline is carried forward unchanged. Its real-phone representative modal retest is still required before BUG-002 can close.

UX-008 separately records future File Picker Escape / Unified SAF Navigation work. This release does not request broad filesystem permissions and does not replace Android SAF yet.
