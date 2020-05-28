package com.example.pingoapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {


@Headers({
        "Context-Type:application/json",
        "Authorization:key=AAAAUJilVSA:APA91bFeWQ0f8XbIx4yExo7lk0ldtPmg8Q76NRial6T-yQwOZJ5AjQLX8UoZjr1kesfR92Z0Q8K0FZ5EF9qQRuUchrbi3Rl_sW5JmMR3uDEm1oY_JsFb682RwE2j8V_cr9TqBiMO1J9O"
})

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);


}
