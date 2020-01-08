Meta:
@issue OPT1-1267
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
|OPT1-1267_TC2.1|InvoiceListener: one shipmentLine Item shipped|
|OPT1-1267_TC2.2|InvoiceListener: two shipmentLine Items shipped|
|OPT1-1267_TC2.3|InvoiceListener: one shipmentLine Items shipped and one shipmentLine Item not shipped|
|OPT1-1267_TC2.4|InvoiceListener: two shipmentLine Items shipped and one shipmentLine Item not shipped|
|OPT1-1267_TC2.5|InvoiceListener: shipmentLine Item shipped with full quantity|                                                      
|OPT1-1267_TC2.6|InvoiceListener: shipmentLine Item shipped with partial quantity|                                                       
|OPT1-1267_TC2.7|InvoiceListener: shipmentLine Item shipped with zero quantity|
|OPT1-1267_TC2.9|InvoiceListener: shipmentLine Items shipped with two carton data|
|OPT1-1267_TC2.10|InvoiceListener: shipmentLine Items shipped with three carton data|
|OPT1-1267_TC2.11|InvoiceListener: shipmentLine Items shipped with four carton data|

