package src.validators;

public interface Validator {
    boolean validate(String input);
    String getErrorMessage();
}