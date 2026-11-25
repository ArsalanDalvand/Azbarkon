package com.arsalandalvand.azbarkon

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object PoemImporter {

    fun loadFromAssets(context: Context): List<Beyt> {
        val poems = mutableListOf<Beyt>()
        try {
            val inputStream = context.assets.open("sady1.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.contains("|")) {
                        val parts = line.split("|")
                        if (parts.size >= 2) {
                            val m1 = parts[0].trim()
                            val m2 = parts[1].trim()

                            // ساخت یک بیت پیشنهاد (بدون ذخیره در دیتابیس، فقط در رم)
                            poems.add(
                                Beyt(
                                    beyt1 = m1, beyt2 = m2,
                                    startT = "08:00", endT = "22:00",
                                    startD = "1402/01/01", endD = "1402/12/29",
                                    weekDays = "1111111", repeat = "1", sh = "1", kind = 1
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return poems
    }
}