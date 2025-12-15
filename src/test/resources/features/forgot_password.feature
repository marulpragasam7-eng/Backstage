
@forgot @stage @smoke
Feature: Verify Forgot password
  As a user who cannot remember the password
  I want to request a password reset
  So that I can regain access to my account

  Scenario Outline: Submit a forgot password request
    Given I am on the login page for password reset
    When I request a password reset for "<email>"
    Then I should see the password reset submission confirmation

    Examples:
      | email                    |
      | Testing@andersencorp.com|
