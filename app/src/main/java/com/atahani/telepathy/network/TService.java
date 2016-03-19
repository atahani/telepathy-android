package com.atahani.telepathy.network;

import com.atahani.telepathy.model.AuthorizeResponseModel;
import com.atahani.telepathy.model.CheckUsernameRequestModel;
import com.atahani.telepathy.model.ClassifyMessageModel;
import com.atahani.telepathy.model.MessageModel;
import com.atahani.telepathy.model.MessagesModel;
import com.atahani.telepathy.model.PublishTelepathyRequestModel;
import com.atahani.telepathy.model.PublishTelepathyResponse;
import com.atahani.telepathy.model.RefreshTokenRequestModel;
import com.atahani.telepathy.model.RegisterDeviceRequestModel;
import com.atahani.telepathy.model.SignInRequestModel;
import com.atahani.telepathy.model.TOperationResultModel;
import com.atahani.telepathy.model.TelepathiesModel;
import com.atahani.telepathy.model.TelepathyModel;
import com.atahani.telepathy.model.TokenModel;
import com.atahani.telepathy.model.UpdateProfileRequestModel;
import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.model.UserProfileModel;
import com.atahani.telepathy.model.UsernameCheckingResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.*;
import retrofit.mime.TypedFile;

import java.util.List;

/**
 * Telepathy API End point
 */
public interface TService {

    @POST("/signin")
    void signIn(@Body SignInRequestModel signInRequestModel, Callback<AuthorizeResponseModel> responseModelCallback);

    @POST("/oauth/refreshtoken")
    TokenModel refreshToken(@Body RefreshTokenRequestModel refreshTokenRequestModel) throws RetrofitError;

    @POST("/register/device")
    void registerDevice(@Body RegisterDeviceRequestModel registerDeviceRequestModel, Callback<TOperationResultModel> responseModelCallback);

    @POST("/user/username/check")
    void checkUsernameAvailability(@Body CheckUsernameRequestModel checkUsernameRequestModel, Callback<UsernameCheckingResponse> responseModelCallback);

    @GET("/user/profile")
    void getUserProfile(Callback<UserProfileModel> responseModelCallback);

    @POST("/user/profile")
    void updateProfile(@Body UpdateProfileRequestModel updateProfileRequestModel, Callback<UserProfileModel> responseModelCallback);

    @DELETE("/user/account")
    void deleteUserAccount(Callback<TOperationResultModel> responseModelCallback);

    @Multipart
    @POST("/user/profile/image")
    void updateImageProfile(@Part("Photo") TypedFile file, Callback<UserProfileModel> responseModelCallback);

    @DELETE("/user/profile/image")
    void removeImageProfile(Callback<UserProfileModel> callback);

    @DELETE("/user/app/{app_id}")
    void terminateApp(@Path("app_id") String app_id, Callback<TOperationResultModel> responseModelCallback);

    @GET("/user/search")
    void searchInUsers(@Query("q") String query, Callback<List<UserModel>> responseModelCallback);

    @GET("/user/{userid}")
    void findUserWithUserId(@Path("userid") String userId, Callback<UserModel> responseModelCallback);

    @GET("/friends")
    void getListOfFriends(@Query("q") String query, Callback<List<UserModel>> responseModelCallback);

    @POST("/friends/{user_id}")
    void addNewFriend(@Path("user_id") String user_id, Callback<UserModel> responseModelCallback);

    @GET("/friends/{user_id}")
    void getFriend(@Path("user_id") String user_id, Callback<UserModel> responseModelCallback);

    @DELETE("/friends/{user_id}")
    void removeFriend(@Path("user_id") String user_id, Callback<TOperationResultModel> responseModelCallback);

    @POST("/telepathy/")
    void publishTelepathy(@Body PublishTelepathyRequestModel publishTelepathyRequestModel, Callback<PublishTelepathyResponse> responseModelCallback);

    @GET("/telepathy")
    void getTelepathies(@Query("per_page") int perPage, @Query("page") int pageNumber, @Query("to_user_id") String toUserId, @Query("q") String searchQuery, Callback<TelepathiesModel> responseModelCallback);

    @GET("/telepathy/{telepathy_id}")
    void getTelepathyById(@Path("telepathy_id") String telepathyId, Callback<TelepathyModel> responseModelCallback);

    @DELETE("/telepathy/{telepathy_id}")
    void disappearTelepathy(@Path("telepathy_id") String telepathy_id, Callback<TOperationResultModel> responseModelCallback);

    @GET("/message")
    void getMessages(@Query("with_user_id") String withUserId, @Query("per_page") int perPage, @Query("page") int pageNumber, Callback<MessagesModel> responseModelCallback);

    @DELETE("/message")
    void deleteUserMessages(@Query("with_user_id") String withUserId, Callback<TOperationResultModel> responseModelCallback);

    @GET("/message/classify")
    void getClassifyMessages(Callback<List<ClassifyMessageModel>> responseModelCallback);

    @GET("/message/{message_id}")
    void getMessageById(@Path("message_id") String messageId, Callback<MessageModel> responseModelCallback);

    @DELETE("/message/{message_id}")
    void deleteMessageById(@Path("message_id") String messageId, Callback<TOperationResultModel> responseModelCallback);

    @PATCH("/message/read")
    void patchOneMessageAsRead(@Query("matched_with_telepathy_id") String matchedWithTelepathyId, @Query("from_user_id") String from_user_id, Callback<TOperationResultModel> responseModelCallback);

    @PATCH("/message/read")
    void patchAllUserMessageAsRead(@Query("from_user_id") String from_user_id, Callback<TOperationResultModel> responseModelCallback);

    @PATCH("/message/receive")
    void patchMessageAsReceive(@Query("matched_with_telepathy_id") String matchedWithTelepathyId, @Query("from_user_id") String from_user_id, Callback<TOperationResultModel> responseModelCallback);

    @POST("/chat.postMessage")
    void sendLogsToChannelViaBot(@Query("token") String token, @Query("channel") String channel, @Query("text") String text, Callback<Response> responseModelCallback);


}
