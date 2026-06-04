@tc_15254
Feature: Entry Door Offsets Management in Backstage Portal

  Background:
    And I navigate to the rSuite Dashboard
    And I open Manage rSuite
    And I go to Entry Door Management
  Scenario: End-to-End Validation of Entry Door Offsets Lifecycle
    # --- Navigation & UI Verification ---
    Then the user should see the Entry Door Offsets page
    And the user verifies functionality for both "Interior" and "Exterior" Offsets
    When the user clicks the accordion dropdown
    Then the created offsets should be displayed

    # --- Create Page & Mandatory Field Validation ---
    When the user clicks the "Add New Offset" button
    Then the user should be navigated to the Create Offset page
    When the user leaves "Name" and "Offset Value" empty
    And the user deselects both "Width" and "Height"
    And the user attempts to create the offset
    Then the "Name" and "Offset Value" fields should be marked as required
    And the user should be required to select at least "Width" or "Height"

    # --- Data Input Constraints ---
    When the user enters alphabetic characters in the "Offset Value" field
    Then the "Offset Value" field should not accept text input
    When the user enters the numeric value "-5.123" in the "Offset Value" field
    Then the field should accept the negative value with three decimal places
    And the user verifies "Width" and "Height" selection toggling updates correctly

    # --- Save & Duplicate Actions ---
    When the user clicks the "Cancel" button
    Then the offset should not be created
    And the user should return to the previous page
    When the user enters valid offset details
    And the user clicks the "Create & Start Copy" button
    Then the offset should be created successfully and duplicated
    When the user clicks the "Save & Close" button
    Then the offset should be saved and the page should close

    # --- Edit & Delete Actions ---
    When the user clicks an existing offset to enter Edit mode
    Then the user should be navigated to the Edit Offset screen
    When the user clicks the "Save & Create New" button
    Then the offset should be saved and a new creation page should open
    When the user returns to the main list
    And the user clicks the trash icon for the offset
    Then the offset should be deleted successfully

    # --- Session Termination ---
    When the user logs out from the Backstage portal
    Then the user should be logged out successfully


