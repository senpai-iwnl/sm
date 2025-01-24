package pl.edu.pb.currencymate.api


import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {
    @GET("rates/A/{code}/")
    suspend fun getCurrencyRate(@Path("code") code: String): CurrencyResponse
}

data class CurrencyResponse(
    val rates: List<Rate>
)

data class Rate(
    val mid: Double
)