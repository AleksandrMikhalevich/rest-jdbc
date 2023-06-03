package by.astontrainee.exceptions;

/**
 * @author Alex Mikhalevich
 * @created 2023-05-31 15:23
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
