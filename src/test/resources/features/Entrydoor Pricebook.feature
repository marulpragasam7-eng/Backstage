@TC_15252 @PricebookRegression @smoke
Feature: Entry Door Pricebook Management

  Background:
    # Managed by CommonSteps.java - handles session reuse or manual login
    #Given I am logged into rSuite
    And I navigate to the rSuite Dashboard

  Scenario: Create a new pricebook and move it through Staged and Live states
    # Managed by ED_PricebookSteps.java
    Given I am on the Entry Door Pricebooks page

    # Creates unique name like "Automation_20260105_1520"
    When I create a new pricebook with a unique name for "Corporate"

    # Moves to Staging and assigns to user
    When I move the pricebook to "Staged" status for user "Arulpragasam M"

    # Verifies in the Staging section (Expanded automatically by page object)
    And I verify the pricebook is visible under the "Staged PRICEBOOKS" section

    # Verifies in the Staged (Locked) section
    Then I verify the pricebook is visible under the "STAGED PRICEBOOKS (Locked)" section

    # Moves to Live status
   # When I move the pricebook to "Live" status

    # Final verification in the Live section
    #Then I verify the pricebook is visible under the "LIVE PRICEBOOKS (Locked)" section