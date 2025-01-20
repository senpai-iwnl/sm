package pl.edu.pb.currencymate.api


import pl.edu.pb.currencymate.ExchangeRateResponse
import retrofit2.http.GET

interface CurrencyApi {
    @GET("exchangerates/tables/A/?format=json")
    suspend fun getExchangeRates(): List<ExchangeRateResponse>
}