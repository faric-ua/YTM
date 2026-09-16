# v1.4.13 — Search flow before / after

## Before

```text
MainActivity
├── calculate plan
├── inspect SearchCache
├── record search quota
├── call YouTubeApi.search
├── handle quota block
├── choose best candidate
├── mutate Track states
└── render UI
```

## After

```text
MainActivity
├── show plan
├── authorize
├── launch executor
└── render callbacks

SearchCoordinator
├── calculate plan
├── inspect/write SearchCache
├── account quota/cache hits
├── call YouTubeApi.search
├── handle quota block
├── choose best candidate
└── mutate Track states
```
