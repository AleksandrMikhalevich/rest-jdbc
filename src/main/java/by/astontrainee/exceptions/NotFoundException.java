package by.astontrainee.exceptions;

/**
 * @author Alex Mikhalevich
 * @created 2023-05-31 20:27
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
