package com.btb.beatthebox.data.remote

import com.btb.beatthebox.data.model.login.LogoutRequest
import com.btb.beatthebox.data.model.login.RefreshTokenRequest
import com.btb.beatthebox.data.model.login.RefreshTokenResponse
import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.CheckoutRequest
import com.btb.beatthebox.data.model.ConfirmedBookingResponse
import com.btb.beatthebox.data.model.Contact
import com.btb.beatthebox.data.model.Faq
import com.btb.beatthebox.data.model.Note
import com.btb.beatthebox.data.model.PaymentVerifyResponse
import com.btb.beatthebox.data.model.RescheduleRequest
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.data.model.SlotBookingResponse
import com.btb.beatthebox.data.model.User
import com.btb.beatthebox.data.model.UserGamesResponse
import com.btb.beatthebox.data.model.Venue
import com.btb.beatthebox.data.model.home.HomeResponse
import com.btb.beatthebox.data.model.home.UpcomingGame
import com.btb.beatthebox.data.model.login.CompleteProfileRequest
import com.btb.beatthebox.data.model.login.GoogleLoginRequest
import com.btb.beatthebox.data.model.login.GoogleLoginResponse
import com.btb.beatthebox.data.model.CheckoutUiModel
import com.btb.beatthebox.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("slots")
    suspend fun getSlots(
        @Query("box") box: String,
        @Query("date") date: String,            // format: yyyy-MM-dd
    ): Response<ApiResponse<List<Slot>>>

    @GET("venue")
    suspend fun getVenue(): Response<ApiResponse<Venue>>

    @GET("contact")
    suspend fun getContacts(): Response<ApiResponse<List<Contact>>>

    @POST("auth/validate")
    suspend fun validateToken(
        @Header("Authorization") bearer: String
    ): Response<ApiResponse<User>>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body body: RefreshTokenRequest
    ): Response<ApiResponse<RefreshTokenResponse>>

    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body body: GoogleLoginRequest
    ): Response<ApiResponse<GoogleLoginResponse>>

    @POST("auth/complete")
    suspend fun completeProfile(
        @Body body: CompleteProfileRequest
    ): Response<ApiResponse<User>>

    @POST("auth/logout")
    suspend fun logout(
        @Body body: LogoutRequest
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "auth/me", hasBody = true)
    suspend fun delete(
        @Body body: LogoutRequest
    ): Response<Unit>

    @GET("costs/{sport}")
    suspend fun getCostBySport(
        @Path("sport") sport: String
    ): Response<ApiResponse<AllSportDetail>>

    @GET("notes/offer-text")
    @Headers("X-Local-Cache: true")
    suspend fun getOfferText(): Response<ApiResponse<String>>

    @GET("sports/all")
    suspend fun getAllSportDetail(): Response<ApiResponse<List<AllSportDetail>>>

    @GET("faqs")
    suspend fun getFaqs(): Response<ApiResponse<List<Faq>>>

    @GET("home")
    suspend fun getHome(): Response<ApiResponse<HomeResponse>>

    @GET("users/me/bookings/next")
    suspend fun getNextBookingForUser(): Response<ApiResponse<UpcomingGame?>>

    @GET("slots/{booking_id}")
    suspend fun getBookingById(
        @Path("booking_id") bookingId: String
    ): Response<ApiResponse<ConfirmedBookingResponse>>

    @GET("notes/slot-selection")
    suspend fun getSlotSelectionNotes(): Response<ApiResponse<List<Note>>>

    @GET("notes/reschedule-policies")
    suspend fun getReschedulePolicies(): Response<ApiResponse<List<Note>>>

    @POST("slots/validate-slot")
    suspend fun verifySlot(
        @Body body: CheckoutRequest
    ): Response<ApiResponse<CheckoutUiModel>>

    @POST("slots/book")
    suspend fun bookSlot(
        @Body body: CheckoutRequest
    ): Response<ApiResponse<SlotBookingResponse>>

    @GET("payments/verify/{orderId}")
    suspend fun verifyPayment(
        @Path("orderId") orderId: String
    ): Response<ApiResponse<PaymentVerifyResponse>>

    @GET("slots/mygames")
    suspend fun getUserBookings(
        @Query("filter") filter: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<UserGamesResponse>>

    @POST("slots/reschedule")
    suspend fun rescheduleSlot(
        @Body body: RescheduleRequest
    ): Response<ApiResponse<String>>
}