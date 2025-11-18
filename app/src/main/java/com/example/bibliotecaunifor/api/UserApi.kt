    package com.example.bibliotecaunifor.api

    import com.example.bibliotecaunifor.models.EditUserRequest
    import com.example.bibliotecaunifor.models.UserResponse
    import retrofit2.Call
    import retrofit2.http.Body
    import retrofit2.http.GET
    import retrofit2.http.PATCH

    interface UserApi {

        @PATCH("users")
        fun editUser(
            @Body request: EditUserRequest
        ): Call<UserResponse>

        @GET("users/me")
        fun getMe(): Call<UserResponse>
    }