
# [PROJECT] — Assistant Workflow

## Startup

Before modifying code:

1. read `START_HERE_ASSISTANT.md`;
2. read `ASSISTANT_CONTEXT_INDEX.md`;
3. read every file listed in the project's context manifest;
4. verify branch/HEAD;
5. inspect exact source;
6. run relevant audits before commit.

## Collaboration loop

Assistant prepares complete work.
User runs one exact command block.
CI produces signed/tested artifacts.
Real-device behavior is verified separately.

## Safety

- preserve historical documentation;
- stage intended paths only;
- stop on unexpected deletions;
- do not confuse static PASS with phone PASS;
- keep secrets out of repository and migration archives;
- record reusable mistakes as workflow lessons.

## Recovery

A new assistant should be able to recover the project from repository files
without access to the old conversation.
