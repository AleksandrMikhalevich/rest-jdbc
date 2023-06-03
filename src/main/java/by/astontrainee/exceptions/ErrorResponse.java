package by.astontrainee.exceptions;

/**
 * @author Alex Mikhalevich
 */

public class ErrorResponse extends RuntimeException {

    public ErrorResponse (String message) {
        super(message);
    }
}
