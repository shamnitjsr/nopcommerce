Meta:
@issue OPPT1-1330
@tags Product: Order Platform, Project:order platform, Program:UpdateInvoice

Scenario: Invoice: create invoice with invoiceId as blank 
Meta:
@id OPT1-1267_TC1.1
@tag Type:acceptance,Module:InvoiceListener
@automatedBy BH12946_Manish
Given create invoice
Then validate using invoice message
Then publish the invoice update msg
Then validate the invoice update msg with spannerDB

Examples:
|test_id        |Desc                         |
|OPT1-1267_TC1.1 |Invoice: create invoice with invoiceId as blank 	   |
|OPT1-1267_TC1.2|Invoice: create invoice with invoice id having some value|
|OPT1-1267_TC1.3|Invoice: create invoice without invoice id attribute|
|OPT1-1267_TC1.4|Invoice: create invoice with non-existing values in orderLine table|
|OPT1-1267_TC1.5|Invoice: create invoice with existing values in orderLine table|
|OPT1-1267_TC1.6|Invoice: create invoice with 1 invoiceLine Items|