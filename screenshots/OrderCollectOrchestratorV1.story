Meta:
@issue OPT1-1822
@tags Product: Order Platform, Project:order platform, Program:Regression


Scenario: Create order with multiple scenarios
Meta:
@id OPT1-1822_TC
@tag Type:acceptance,Module:OrderCreation
@automatedBy BH14322_Mohit

Given create order message is published to collect Order
Then Order is created with expected values and validated in DB


Examples:
|test_id         |Desc                         |
|OPT1-1822_TC1 |Order with 1 Line and validate product additional attribute detail|
|OPT1-1822_TC2 |Order with 2 Line and validate product additional attribute detail|
|OPT1-1822_TC3 |Order with two line with one invalid item |
|OPT1-1822_TC4 |Order with no sellerOrderId |
|OPT1-1822_TC5 |Order with existing sellerOrderId |
|OPT1-1822_TC6 |Order with 3 line and validate product additional attribute detail |
