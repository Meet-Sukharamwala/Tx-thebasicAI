package com.example

import com.example.ai.LocalIntentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TxUnitTest {

    @Test
    fun testTimeIntentParsing() {
        val decision = LocalIntentParser.parse("Tx, what time is it?")
        assertEquals("get_current_time", decision.toolName)
    }

    @Test
    fun testAppLaunchIntentParsing() {
        val decision = LocalIntentParser.parse("Hey Tx, open YouTube")
        assertEquals("open_supported_app", decision.toolName)
        assertEquals("youtube", decision.arguments["appName"])
    }

    @Test
    fun testReminderIntentParsing() {
        val decision = LocalIntentParser.parse("Tx, remind me to study Python at 7 PM")
        assertEquals("create_reminder", decision.toolName)
        assertTrue(decision.arguments["title"]?.contains("study Python") == true)
        assertTrue(decision.arguments["timeString"]?.contains("7 PM") == true)
    }

    @Test
    fun testTaskIntentParsing() {
        val decision = LocalIntentParser.parse("Tx, add task to finish project")
        assertEquals("add_task", decision.toolName)
        assertEquals("finish project", decision.arguments["title"])
    }

    @Test
    fun testNoteIntentParsing() {
        val decision = LocalIntentParser.parse("Tx, create a note titled My Ideas content Test note body")
        assertEquals("create_note", decision.toolName)
        assertEquals("My Ideas", decision.arguments["title"])
        assertEquals("Test note body", decision.arguments["content"])
    }

    @Test
    fun testGreetingIntentParsing() {
        val decision = LocalIntentParser.parse("Hey Tx, how are you?")
        assertEquals("answer_general_question", decision.toolName)
        assertTrue(decision.arguments["response"]?.contains("Tx") == true)
    }
}
