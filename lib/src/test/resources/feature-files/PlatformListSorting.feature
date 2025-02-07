@Kiwi.Plan(CheckDecodesPlatformList)
@Kiwi.Priority(P2)
Feature: Platform List Tab

    Background: Basic Setup
        Given A database with many platforms available.
    
    Scenario Outline: Sorting by column open by <method>

        Given The list is sorted by any column
        When The user <method>
        Then The selected platform is opened        
  
    Examples:
        | method                       |
        |double clicks a row           |
        |selects a row and clicks open |
        

    Scenario Outline: Filter by <filter> open by <method>

        Given The user filters the platform list by <filter> selection
        When The user <method>
        Then The correct platform is opened

    Examples:
        | filter     | method                       |
        |platform    |double clicks a row           |
        |Agency      |double clicks a row           |
        |Transport-ID|double clicks a row           |
        |Config      |double clicks a row           |
        |platform    |selects a row and clicks open |
        |Agency      |selects a row and clicks open |
        |Transport-ID|selects a row and clicks open |
        |Config      |selects a row and clicks open |