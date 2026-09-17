# Termux — v1.4.18 G01

Package: `YTM_Importer_v1.4.18_G01_ACCOUNT_IMPORT.zip`

Apply sequence:

1. enter repo with `ytm`;
2. verify clean status and pull;
3. unzip package into a temporary directory;
4. copy overlay into repository;
5. remove temporary directory;
6. run `python scripts/apply-v1.4.18.py`;
7. regenerate `FILE_MANIFEST.txt`;
8. run `scripts/v1418-account-library-import-audit.sh`;
9. run release preflight;
10. inspect diff;
11. stage exact intended paths;
12. verify no staged deletions;
13. commit and push;
14. wait for GitHub build;
15. phone-test G01.
