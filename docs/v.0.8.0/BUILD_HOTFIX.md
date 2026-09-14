# YTM Importer v0.8.0 — build hotfix

## Причина
GitHub Actions падав у `android-actions/setup-android@v3`:

`Warning: Failed to find package 'tools'`

## Виправлення
Workflow більше не запускає `setup-android@v3`.
Замість цього він використовує Android SDK, уже встановлений на GitHub-hosted Ubuntu runner, знаходить `sdkmanager` і встановлює:

- `platforms;android-36`
- `build-tools;35.0.0`

Версія Android-застосунку залишається **0.8.0**, бо код програми не змінювався — змінено лише CI/build workflow.
