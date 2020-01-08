Meta:
@issue OPT1-1593
@tags Product: Order Platform, Project:order platform, Program:Regression


Scenario: Create orders to Released Status
Meta:
@id OPT1-1593_TC1.1
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB for partnerCollectOrder
Then validate from spannerDB for allocateOrder
Then validate from spannerDB for confirmOrder
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC1.1 |Order with 1 Line, 1 qty to Released Status  |
|OPT1-1593_TC1.2 |Order with 1 Line, 2 qty to Released Status  |
|OPT1-1593_TC1.3 |Order with 1 Line, 3 qty to Released Status  |


Scenario: Order with 1 Line, 1 qty to Cancelled Status
Meta:
@id OPT1-1593_TC1.1
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB for partnerCollectOrder
Then validate from spannerDB the order status is Cancelled

Examples:

|test_id         |Desc                         |
|OPT1-1593_TC1.4 | Order with 1 Line, 1 qty to Cancelled Status |


Scenario: Order with 1 Line, duplicate Zola order
Meta:
@id OPT1-1593_TC1.1
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB for partnerCollectOrder

Examples:

|test_id         |Desc                         |
|OPT1-1593_TC1.5 | Order with 1 Line, duplicate Zola order |


Scenario: Order with 2 Lines, Same ProductGroups, Same Locations
Meta:
@id OPT1-1593_TC5.1
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.1 |Order with 2 Lines, Same ProductGroups, Same Locations |


Scenario: Order with 2 Lines, Same ProductGroups, Diff Locations
Meta:
@id OPT1-1593_TC5.2
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.2 |Order with 2 Lines, Same ProductGroups, Diff Locations |


Scenario: Order with 2 Lines, Diff ProductGroups, Same Locations
Meta:
@id OPT1-1593_TC5.3
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.3 |Order with 2 Lines, Diff ProductGroups, Same Locations |


Scenario: Order with 2 Lines, Diff ProductGroups, Diff Locations
Meta:
@id OPT1-1593_TC5.4
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.4 |Order with 2 Lines, Diff ProductGroups, Diff Locations |


Scenario: Order with 3 Lines, All Same ProductGroups, Same Locations
Meta:
@id OPT1-1593_TC5.5
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.5 |Order with 3 Lines, All Same ProductGroups, Same Locations |


Scenario: Order with 3 Lines, All Same ProductGroups, Same Locations (1&2)
Meta:
@id OPT1-1593_TC5.6
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.6 |Order with 3 Lines, All Same ProductGroups, Same Locations (1&2) |


Scenario: Order with 3 Lines, All Same ProductGroups, Diff Locations
Meta:
@id OPT1-1593_TC5.7
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.7 |Order with 3 Lines, All Same ProductGroups, Diff Locations |



Scenario: Order with 3 Lines, Same ProductGroups (1&2), Same Locations
Meta:
@id OPT1-1593_TC5.8
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.8 |Order with 3 Lines, Same ProductGroups (1&2), Same Locations |


Scenario: Order with 3 Lines, Same ProductGroups (1&2), Same Locations (1&3)
Meta:
@id OPT1-1593_TC5.9
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.9 |Order with 3 Lines, Same ProductGroups (1&2), Same Locations (1&3) |


Scenario: Order with 3 Lines, Same ProductGroups (1&2), Same Locations (2&3)
Meta:
@id OPT1-1593_TC5.10
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.10 |Order with 3 Lines, Same ProductGroups (1&2), Same Locations (2&3) |


Scenario: Order with 3 Lines, Same ProductGroups (1&2), All Diff Locations
Meta:
@id OPT1-1593_TC5.11
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.11 |Order with 3 Lines, Same ProductGroups (1&2), All Diff Locations |


Scenario: Order with 3 Lines, All Diff ProductGroups, All Same Locations
Meta:
@id OPT1-1593_TC5.13
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.12 |Order with 3 Lines, All Diff ProductGroups, All Same Locations |


Scenario: Order with 3 Lines, All Diff ProductGroups, Same Locations (1&2)
Meta:
@id OPT1-1593_TC5.13
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.13 |Order with 3 Lines, All Diff ProductGroups, Same Locations (1&2) |


Scenario: Order with 3 Lines, All Diff ProductGroups, All Diff Locations
Meta:
@id OPT1-1593_TC5.14
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC5.14 |Order with 3 Lines, All Diff ProductGroups, All Diff Locations |


Scenario: Order with 1 Line, 1 Release, 1 Shipment, 1 SHPSHPD with 1 Carton, 1 Invoice
Meta:
@id OPT1-1593_TC7.1
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB the order status is Released
Then publish the ship confirm message
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.1 |Order with 1 Line, 1 Release, 1 Shipment, 1 SHPSHPD with 1 Carton, 1 Invoice  |


Scenario: Order with 1 Line, 1 Release, 1 Shipment, 2 SHPSHPD with 1 Carton, 2 Invoices
Meta:
@id OPT1-1593_TC7.2
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB the order status is Released
Then publish the ship confirm message for fulfillment 1
Then publish the ship confirm message for fulfillment 2
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.2 |Order with 1 Line, 1 Release, 1 Shipment, 2 SHPSHPD with 1 Carton, 2 Invoices  |


Scenario: Order with 1 Line, 1 Release, 1 Shipment, 1 SHPSHPD with 2 Cartons each, 1 Invoice
Meta:
@id OPT1-1593_TC7.3
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB the order status is Released
Then publish the ship confirm message
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.3 |Order with 1 Line, 1 Release, 1 Shipment, 1 SHPSHPD with 2 Cartons each, 1 Invoice  |


Scenario: Order with 1 Line, 1 Release, 1 Shipment, 2 SHPSHPD with 2 Cartons each, 2 Invoices
Meta:
@id OPT1-1593_TC7.4
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB the order status is Released
Then publish the ship confirm message for fulfillment 1
Then publish the ship confirm message for fulfillment 2
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.4 |Order with 1 Line, 1 Release, 1 Shipment, 2 SHPSHPD with 2 Cartons each, 2 Invoices  |


Scenario: Order with 2 Lines, 1 Release, 1 Shipment, 1 Carton, 1 Invoice
Meta:
@id OPT1-1593_TC7.5
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB the order status is Released
Then publish the ship confirm message
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.5 |Order with 2 Lines, 1 Release, 1 Shipment, 1 Carton, 1 Invoice  |


Scenario: Order with 2 Lines, 1 Release, 1 Shipment, 1 SHPSHPD with 2 Cartons, 1 Invoice
Meta:
@id OPT1-1593_TC7.6
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given a partner collect order
Then publish the partner collect order message
Then validate from spannerDB the order status is Released
Then publish the ship confirm message
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.6 |Order with 2 Lines, 1 Release, 1 Shipment, 1 SHPSHPD with 2 Cartons, 1 Invoice |


Scenario: Order with 2 Lines, 2 Releases, 2 Shipments, 1 Carton each, 2 Invoices
Meta:
@id OPT1-1593_TC7.7
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder
Then publish the ship confirm message for fulfillment 1
Then publish the ship confirm message for fulfillment 2
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.7 |Order with 2 Lines, 2 Releases, 2 Shipments, 1 Carton each, 2 Invoices|


Scenario: Order with 2 Lines, 2 Releases, 2 Shipments, 2 Cartons each, 2 Invoices
Meta:
@id OPT1-1593_TC7.8
@tag Type:acceptance,Module:All
@automatedBy B005230_Long

Given an order update message
Then publish a message to order post processor
Then validate from spannerDB the order status is Released
Then validate from spannerDB for releaseOrder
Then publish the ship confirm message for fulfillment 1
Then publish the ship confirm message for fulfillment 2
Then validate from spannerDB for invoice

Examples:
|test_id         |Desc                         |
|OPT1-1593_TC7.8 |Order with 2 Lines, 2 Releases, 2 Shipments, 2 Cartons each, 2 Invoices|


