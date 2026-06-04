package steps;

import core.PlaywrightManager;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.EntryDoorOffset;
import pages.EntryDoorPage;

public class EntryDoorOffsetValidations {

    private final EntryDoorOffset extOffset = new EntryDoorOffset(PlaywrightManager.page());


    @Then("the user should see the Entry Door Offsets page")
    public void the_user_should_see_the_entry_door_offsets_page() {
        extOffset.exteriorOffset();

    }

    @Then("the user verifies functionality for both {string} and {string} Offsets")
    public void the_user_verifies_functionality_for_both_and_offsets(String string, String string2) {
    }

    @When("the user clicks the accordion dropdown")
    public void the_user_clicks_the_accordion_dropdown() {
    }

    @Then("the created offsets should be displayed")
    public void the_created_offsets_should_be_displayed() {
    }

    @When("the user clicks the {string} button")
    public void the_user_clicks_the_button(String string) {
    }

    @Then("the user should be navigated to the Create Offset page")
    public void the_user_should_be_navigated_to_the_create_offset_page() {
    }

    @When("the user leaves {string} and {string} empty")
    public void the_user_leaves_and_empty(String string, String string2) {
    }

    @When("the user deselects both {string} and {string}")
    public void the_user_deselects_both_and(String string, String string2) {
    }

    @When("the user attempts to create the offset")
    public void the_user_attempts_to_create_the_offset() {
    }

    @Then("the {string} and {string} fields should be marked as required")
    public void the_and_fields_should_be_marked_as_required(String string, String string2) {
    }

    @Then("the user should be required to select at least {string} or {string}")
    public void the_user_should_be_required_to_select_at_least_or(String string, String string2) {
    }

    @When("the user enters alphabetic characters in the {string} field")
    public void the_user_enters_alphabetic_characters_in_the_field(String string) {
    }

    @Then("the {string} field should not accept text input")
    public void the_field_should_not_accept_text_input(String string) {
    }

    @When("the user enters the numeric value {string} in the {string} field")
    public void the_user_enters_the_numeric_value_in_the_field(String string, String string2) {
    }

    @Then("the field should accept the negative value with three decimal places")
    public void the_field_should_accept_the_negative_value_with_three_decimal_places() {
    }

    @Then("the user verifies {string} and {string} selection toggling updates correctly")
    public void the_user_verifies_and_selection_toggling_updates_correctly(String string, String string2) {
    }

    @Then("the offset should not be created")
    public void the_offset_should_not_be_created() {
    }

    @Then("the user should return to the previous page")
    public void the_user_should_return_to_the_previous_page() {
    }

    @When("the user enters valid offset details")
    public void the_user_enters_valid_offset_details() {
    }

    @Then("the offset should be created successfully and duplicated")
    public void the_offset_should_be_created_successfully_and_duplicated() {
    }

    @Then("the offset should be saved and the page should close")
    public void the_offset_should_be_saved_and_the_page_should_close() {
    }

    @When("the user clicks an existing offset to enter Edit mode")
    public void the_user_clicks_an_existing_offset_to_enter_edit_mode() {
    }

    @Then("the user should be navigated to the Edit Offset screen")
    public void the_user_should_be_navigated_to_the_edit_offset_screen() {
    }

    @Then("the offset should be saved and a new creation page should open")
    public void the_offset_should_be_saved_and_a_new_creation_page_should_open() {
    }

    @When("the user returns to the main list")
    public void the_user_returns_to_the_main_list() {
    }

    @When("the user clicks the trash icon for the offset")
    public void the_user_clicks_the_trash_icon_for_the_offset() {
    }

    @Then("the offset should be deleted successfully")
    public void the_offset_should_be_deleted_successfully() {
    }

    @When("the user logs out from the Backstage portal")
    public void the_user_logs_out_from_the_backstage_portal() {
    }

    @Then("the user should be logged out successfully")
    public void the_user_should_be_logged_out_successfully() {
    }


}
