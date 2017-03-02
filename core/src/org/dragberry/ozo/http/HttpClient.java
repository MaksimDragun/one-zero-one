package org.dragberry.ozo.http;

import java.io.Serializable;

/**
 * Created by Dragun_M on 3/2/2017.
 */

public interface HttpClient {

    boolean isConnected();

    void executeTask(HttpTask<?, ?> httpTask);
}
