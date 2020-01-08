Meta:
@issue OPT1-1593
@tags Product: Order Platform, Project:order platform, Program:Regression


Scenario: test
Meta:
@id OPT1-1593_TC5.14
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a collect order request
Then publish the collect order request to the topic
Then validate collect order

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC1.1 ||
