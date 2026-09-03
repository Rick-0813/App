package com.example.myapplication

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun companyAverage_usesOnlyThatCompanyRatings() {
        assertEquals(4.5f, calculateAverageRating(listOf(4, 5)), 0.001f)
        assertEquals(2.0f, calculateAverageRating(listOf(2)), 0.001f)
        assertEquals(0f, calculateAverageRating(emptyList()), 0.001f)
    }

    @Test
    fun companyKeys_keepDifferentCompaniesSeparated() {
        assertEquals("company:company a", normalizedCompanyKey(" Company A "))
        assertEquals("company:company a", normalizedCompanyKey("company a"))
        assertNotEquals(normalizedCompanyKey("Company A"), normalizedCompanyKey("Company B"))
    }
}
