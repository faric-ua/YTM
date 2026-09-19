# House Dance Hit 2000 — collection research

Last updated: 2026-09-19

Purpose:
- preserve the whole compilation series research;
- build one YTM-importable playlist per confirmed volume;
- keep the collection as repeatable YTM Importer test data.

## Confirmed Vol.1 source

YouTube title: `House Dance Hit 2000 Vol1 Compilation`
Channel: `HOUSE DANCE JUKEBOX`
URL: https://www.youtube.com/watch?v=NIazEptoogc

The source description contains this 9-track list. Obvious title/artist spelling has
been normalized for YTM search/import while keeping the original order:

1. Billy More - Up & Down
2. Eiffel 65 - Move Your Body
3. The Ones - Flawless
4. Tommy Vee feat. D'Empress - You Make Me Wanna
5. Benjamin Diamond - In Your Arms
6. Eiffel 65 - Too Much of Heaven
7. Benjamin Diamond - Little Scare
8. Milk & Sugar - Higher & Higher
9. Modjo - Lady (Hear Me Tonight)

Companion import fixture:
`House_Dance_Hit_2000_Vol1_YTM.txt`

Recommended playlist name:
`House Dance Hit 2000 - Vol.1`

## Series inventory currently confirmed

- Vol.1 — confirmed; full tracklist recovered.
- Vol.4 — confirmed to exist on the same YouTube channel:
  https://www.youtube.com/watch?v=Df0gY9KJbQE
- Vol.2 — not yet independently confirmed/indexed.
- Vol.3 — not yet independently confirmed/indexed.
- Vol.5+ — not yet confirmed.

This does not prove there were only four volumes. It records only what has been
verified so far. Future research should preserve every discovered volume even if
only one playlist is being built at a time.

## Dating note

The YouTube series appears to be a retrospective compilation of 2000s house/dance,
not a compilation originally issued in the year 2000.

One concrete clue: Tommy Vee feat. D'Empress — `You Make Me Wanna` appears in
official UK charts in October 2003, while it is included in Vol.1.

## YTM Importer regression use

The Vol.1 fixture is now a real regression dataset.

v1.4.40 phone run, 2026-09-19:
- TXT import: 9 tracks loaded;
- Search: 9 new API searches attempted;
- all 9 tracks failed because Google authorization was no longer valid;
- Home Step 2 nevertheless remained green/checked;
- this reproduced BUG-004 on the Search path.

Do not alter this fixture casually; if spelling corrections are made later, record
them here so old QA can still be understood.

## Research rule for future volumes

For each newly confirmed volume:
1. preserve original track order;
2. normalize only obvious artist/title typos;
3. preserve the source URL;
4. distinguish one long compilation video from a real YouTube playlist;
5. add the volume to this inventory even if its playlist will be built later;
6. create a separate one-track-per-line TXT fixture for YTM Importer.
