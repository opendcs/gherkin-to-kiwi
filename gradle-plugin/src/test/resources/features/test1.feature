@Kiwi.Plan(Plan1)
@Kiwi.Plan(Plan2)
Feature: Test 1

    Background: Basic Setup
        Given a Kiwi Instance we have permission to upload to
    
    Scenario: Ant task processes build and saves tests to database

        Given url, username, password, and list of feature files
        When The gherkin-kiwi task is executed
        Then The tests are saved.
 
    