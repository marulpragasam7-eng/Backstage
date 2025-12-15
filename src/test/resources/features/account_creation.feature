@regression@loginWindow @smoke
Feature: Account Management (Creation and Recovery)
  As a user of the application
  I want to manage my account access and credentials
  So that I can successfully log in and use the system

  # --- SCENARIO 1: Account Creation ---
  Scenario Outline: Create a new account successfully
    Given I am on the login page
    When I create a new account with email "<email>" and password "<password>"
      | first    | <first>    |
      | last     |  <last> |
      | phone    | <phone>    |
      | store    | <store>    |
      | role     | <role>     |
    Then I should see the account verification message

    Examples:
      | email  | password       | first | last       | phone       | store     | role         |
      | RANDOM | Arul@1992!Abc  | Arul  | Backstage  | 1234567891  | Corporate | Back Office  |

  # --- SCENARIO 2: Forgot Password (Uses specific tags for filtering) ---
  @forgot @stage @smoke
  Scenario Outline: Submit a forgot password request
    Given I am on the login page for password reset
    When I request a password reset for "<email>"
    Then I should see the password reset submission confirmation

    Examples:
      | email                    |
      | Testing@andersencorp.com |
