package pl.edu.pb.currencymate

data class ExchangeRateResponse(
    val rates: List<ExchangeRate>
)

data class ExchangeRate(
    val currency: String,
    val code: String,
    val mid: Double
)