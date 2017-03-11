package org.dragberry.ozo.http;

import java.io.Serializable;

/**
 * Created by Dragun_M on 3/2/2017.
 */

public interface HttpClient {

    interface URL {

//      String ROOT = "http://10.0.2.2:8087/ozo-backend-war";
        String ROOT = "http://192.168.0.104:8080/ozo-backend-war";


        String NEW_USER = "/user/new";
        String GET_ALL_RESULTS = "/results/user/{0}/levels";
        String NEW_RESULT = "/level/result/new";
    }


    boolean isConnected();

    <T, R>  void executeTask(HttpTask<T, R> httpTask);
}
