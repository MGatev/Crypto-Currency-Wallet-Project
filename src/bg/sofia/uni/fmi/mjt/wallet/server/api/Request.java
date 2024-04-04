package bg.sofia.uni.fmi.mjt.wallet.server.api;

import java.net.URI;

public interface Request {

    /**
     * Returns the appropriate URI for the request
     */
    URI getUri();
}
