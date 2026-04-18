package org.zeroagent.common.utils.json;

import java.io.Serial;

public class JSONException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8346298376392591187L;

    public JSONException() {
        super();
    }

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
