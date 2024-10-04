[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=opendcs_gherkin-to-kiwi&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=opendcs_gherkin-to-kiwi)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=opendcs_gherkin-to-kiwi&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=opendcs_gherkin-to-kiwi)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=opendcs_gherkin-to-kiwi&metric=coverage)](https://sonarcloud.io/summary/new_code?id=opendcs_gherkin-to-kiwi)


# Purpose

This library is intended to be used in build tools to take manual test cases written in the [Gherkin Syntax](https://cucumber.io/docs/gherkin/)
And render them into the KiwiTCMS, handling cases, plans, component mapping, and attachments.

TODO:
- Better formatting
- Image handling
- Setting up Test Runs
- Rendering test case to something local, like CSV.
- Automated testing, current tests of the ant task assume you've got a throwaway Kiwi instance to use.