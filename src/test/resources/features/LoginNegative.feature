
@login @negative @stage @smoke
Feature: Login - Negative scenarios on RSuite Backstage stage

  Background:
    Given I am on the RSuite Backstage login page

  @required-fields
  Scenario: Required field validation when attempting login with empty inputs
    When I attempt to login with email "" and password ""
    Then I should see field error "The Email field is required."
    And I should see field error "The Password field is required."


  @invalid-email
  Scenario Outline: Invalid email format shows email validation error
    When I attempt to login with email "<email>" and password "somePassword123"
    Then I should see field error "Please enter a valid email address."
    Examples:
      | email                |
      | abc                  |
      | abc@                 |
      | abc@xyz.com              |
      | abc@xyz.             |
      | abc@.com             |
      | abc@xyz..com         |
      | abc@@xyz.com         |
      | sadfsadfsdfsdf       |

  @invalid-creds
  Scenario Outline: Invalid credentials show generic invalid credentials message
    When I attempt to login with email "<email>" and password "<password>"
    Then I should see page error "Invalid account email or password."
    Examples:
      | email                  | password     |
      | Arul@test.com          | wrongPass1!  |
      | unregistered@xyz.com   | dfZDfZDfZDf  |

  @trimming
  Scenario: Email should be trimmed
    When I attempt to login with email "  Arul@test.com  " and password "wrongPass1!"
    Then I should see page error "Invalid account email or password."

  @injection
  Scenario Outline: Malicious input never leaks stack traces or raw errors
    When I attempt to login with email "<email>" and password "<password>"
    Then I should see page error "Please enter a valid email address."
    And I should not see any technical error details on the page
    Examples:
      | email                     | password                  |
      | ' OR '1'='1               | anything                  |
      | <script>alert(1)</script> | <img src=x onerror=alert(1)> |
