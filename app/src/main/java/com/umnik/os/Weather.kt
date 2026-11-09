package com.umnik.os

import android.app.Activity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

/** Погода через бесплатный сервис Open-Meteo (ключ не нужен). */
object Weather {

    class Day(val label: String, val code: Int, val min: Double, val max: Double)

    class Report(
        val city: String,
        val temp: Double,
        val feels: Double,
        val code: Int,
        val wind: Double,
        val humidity: Int,
        val days: List<Day>
    )

    /** Код погоды -> значок и подпись. */
    fun describe(code: Int): Pair<String, String> = when (code) {
        0 -> "☀️" to "Ясно"
        1, 2 -> "🌤" to "Малооблачно"
        3 -> "☁️" to "Облачно"
        45, 48 -> "🌫" to "Туман"
        51, 53, 55, 56, 57 -> "🌦" to "Морось"
        61, 63, 65, 66, 67 -> "🌧" to "Дождь"
        71, 73, 75, 77 -> "🌨" to "Снег"
        80, 81, 82 -> "🌧" to "Ливень"
        85, 86 -> "🌨" to "Снегопад"
        95, 96, 99 -> "⛈" to "Гроза"
        else -> "🌡" to "Погода"
    }

    private fun httpGet(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.setRequestProperty("User-Agent", "UmnikOS/0.2")
        try {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    /** Блокирующий запрос: вызывать только из фонового потока. */
    fun fetch(city: String): Report {
        val q = URLEncoder.encode(city, "UTF-8")
        val geo = JSONObject(
            httpGet("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=1&language=ru&format=json")
        )
        val results = geo.optJSONArray("results")
        if (results == null || results.length() == 0) {
            throw IllegalArgumentException("Город не найден")
        }
        val place = results.getJSONObject(0)
        val lat = place.getDouble("latitude")
        val lon = place.getDouble("longitude")
        val name = place.optString("name", city)

        val data = JSONObject(
            httpGet(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto&forecast_days=5&wind_speed_unit=ms"
            )
        )
        val current = data.getJSONObject("current")
        val daily = data.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val codes = daily.getJSONArray("weather_code")
        val maxs = daily.getJSONArray("temperature_2m_max")
        val mins = daily.getJSONArray("temperature_2m_min")

        val parse = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val show = SimpleDateFormat("EEE, d MMM", Locale("ru"))
        val days = (0 until dates.length()).map { i ->
            val raw = dates.getString(i)
            val label = try {
                show.format(parse.parse(raw)!!)
            } catch (e: Exception) {
                raw
            }
            Day(label, codes.getInt(i), mins.getDouble(i), maxs.getDouble(i))
        }

        return Report(
            name,
            current.getDouble("temperature_2m"),
            current.getDouble("apparent_temperature"),
            current.getInt("weather_code"),
            current.getDouble("wind_speed_10m"),
            current.getInt("relative_humidity_2m"),
            days
        )
    }

    /** Грузит погоду в фоне и возвращает результат в главный поток. */
    fun loadAsync(activity: Activity, city: String, callback: (Report?, String?) -> Unit) {
        Thread {
            try {
                val report = fetch(city)
                activity.runOnUiThread { if (!activity.isFinishing) callback(report, null) }
            } catch (e: IllegalArgumentException) {
                activity.runOnUiThread { if (!activity.isFinishing) callback(null, e.message) }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    if (!activity.isFinishing) callback(null, "Нет интернета или сервис погоды не отвечает")
                }
            }
        }.start()
    }
}
