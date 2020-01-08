Meta:
@issue OPT1-1822
@tags Product: Order Platform, Project:order platform, Program:Regression


Scenario: Create order with multiple scenarios
Meta:
@id OPT1-1824_TC
@tag Type:acceptance,Module:OrderCreation
@automatedBy BH14732_Shambhu

Given order enrichment request is created
When Rest call is made to order enrichment

Then orderline is updated with product additional attribute details

Examples:
|test_id         |Desc                         |
|OPT1-1824_TC1|OrderEnrichment with n OrderLine with productAdditionalAttributesDetails from catalog service|







