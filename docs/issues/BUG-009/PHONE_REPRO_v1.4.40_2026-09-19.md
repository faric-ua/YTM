# BUG-009 phone reproduction — v1.4.40 — 2026-09-19

## Summary

Google account-switch flow has a phone-width copy/action layout problem.

## Observation

While changing the connected Google account, the explanatory text and the account
change/select action do not compose cleanly at the tested phone width.

This is tracked separately from BUG-004:
- BUG-004 = authorization state/invalidation/recovery logic;
- BUG-009 = account-switch UI/copy/action fit.

The current observation does not by itself prove that the account-switch operation is
functionally broken.

Status: **OPEN — PHONE REPRO v1.4.40**
