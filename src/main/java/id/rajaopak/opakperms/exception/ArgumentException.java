package id.rajaopak.opakperms.exception;

public abstract class ArgumentException extends Exception {

    public static class PastDate extends ArgumentException {

    }

    public static class InvalidDate extends ArgumentException {
        private final String invalidDate;

        public InvalidDate(String invalidDate) {
            this.invalidDate = invalidDate;
        }
    }
}
