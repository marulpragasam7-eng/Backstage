
@pricebook @stage @smoke
Feature: Pricebook - create editable then stage

  Scenario Outline: Create new pricebook (Editable) then Stage and verify
    Given I login to rSuite with email "<email>" and password "<password>"
    When I create a new editable pricebook named "<name>" for org "<orgFieldName>" using region query "<regionQuery>" and supplier "<supplier>"
    And I set pricebook parameters
      | universalMarkup   | <universalMarkup>   |
      | installation      | <installation>      |
      | labor             | <labor>             |
      | grossToNetPercent | <grossToNetPercent> |
      | grossToNetDollar  | <grossToNetDollar>  |
      | salesTaxRecovery  | <salesTaxRecovery>  |
    And I stage the pricebook adding user query "<userQuery>" selecting "<stagedUser>"
    Then the pricebook should show staged banner

    Examples:
      | email                     | password       | name     | orgFieldName               | regionQuery | supplier         | universalMarkup | installation | labor | grossToNetPercent | grossToNetDollar | salesTaxRecovery | userQuery | stagedUser       |
      | marulpragasam7@gmail.com  | Arulgreat@7    | R77_AutomationTesting_QA  | RbA of Anchorage, AK       | cor         | Coastal Windows  | 12              | 12           | 21    | 11                | 22               | 33               | Arul Pragasam      | Arul Pragasam    |
