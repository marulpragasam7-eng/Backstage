@EntrydoorManagement @ED_Pricebook @smoke
Feature: Wholesale pricebook upload in Backstage
  As a Backstage user
  I want to upload a wholesale pricebook via Entry Door Management
  So that the system processes it successfully

  Background:
    #Given I am logged into rSuite
    And I navigate to the rSuite Dashboard

  @EntrydoorManagement
  Scenario Outline: Upload wholesale pricebook and verify processing
    And I open Manage rSuite
    And I go to Entry Door Management
    And I open Wholesale Upload
    And I import the wholesale pricebook from "<FilePath>"
    #And I verify Successfully Uploaded for Processing "<message>"
    Then I verify the wholesale pricebook is processed for "<FilePath>"
    Examples:
      |message| FilePath                               |
      |Successfully Uploaded for Processing.| src/test/resources/data/Wholesale Entry Door v2026.1_2025-11-12_2025-11-12.xlsx |
