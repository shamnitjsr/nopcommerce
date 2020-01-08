Meta:
@issue OPPT1-1249
@tags Product: Order Platform, Project:order platform, Program:Invoice

Scenario: Invoice: invoiceListener shipment data with line items and cartons 
Meta:
@id OPT1-1267_TC2.1
@tag Type:acceptance,Module:InvoiceListener
@automatedBy BH13712_Manish
Given publish the shipment confimration onsucess msg
Then validate the shipment confimration onsucess msg with spannerDB

Examples:
|test_id        |Desc                         |
|OPT1-1267_TC1.7|Invoice: create invoice with 2 invoiceLine Items|