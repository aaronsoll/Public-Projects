package byow.Core;

/** General exception indicating a BYOW error.  For fatal errors, the
 *  result of .getMessage() is the error message to be printed.
 *  @author P. N. Hilfinger
 */
class BYOWException extends RuntimeException {


    /** A BYOWException with no message. */
    BYOWException() {
        super();
    }

    /** A BYOWException MSG as its message. */
    BYOWException(String msg) {
        super(msg);
    }

}
