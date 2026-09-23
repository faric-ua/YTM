# URL/Mix Snapshot Flow

```text
User enters YouTube / YTM URL
              |
              v
        Validate URL form
         /               invalid           supported
      |                  |
      v                  v
 clear error       classify source
                  /                       concrete playlist    dynamic Mix/radio
                  \             /
                   v           v
               explicit Resolve
                     |
                     v
           one active remote read
                     |
             +-------+-------+
             |               |
           failure         resolved
             |               |
             v               v
       explicit error      Preview
                           /                         Cancel       Commit
                      |            |
                      v            v
                  no local     stable local
                   change       snapshot
                                   |
                                   v
                       existing Review/Search/
                         YTM Project workflows
```

Lifecycle invariant:

```text
Activity recreation -> restore UI state only
                    -> never auto-Resolve
                    -> never auto-Commit
                    -> never start remote write
```
