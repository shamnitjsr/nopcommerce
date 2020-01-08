Meta:
@issue OPT1-1491
@tags Product: OM Config, Project:order platform, Program:OM config

Scenario: The service should create return and publish in topic
Meta:
@id OPT1-1491_TC1
@tag Type:acceptance,Type:smoke,Module:MessageStore
@automatedBy BH14322_Mohit


Given the message to be posted is formed
When all the mandatory headers are present and unique
Then  the message is stored in the messagestore table

Examples:
|test_id      |Desc                                                                                                               |
|OPT1-1491_TC1|All the mandatory headers are present and combination of header is unique. |
|OPT1-1491_TC2|CorrelationID is missing in the header when request is posted. |
|OPT1-1491_TC3|MessageId is missing in the header when request is posted.|
|OPT1-1491_TC4|OrderId is missing in the header when request is posted.|
|OPT1-1491_TC5|ClientId is missing in the header when request is posted.|
|OPT1-1491_TC6|Request is posted with duplicate header value. |



