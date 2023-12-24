package tictactoe.shell.inputs

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tictactoe.core.Outputs

@Tag("integration")
@WebMvcTest(StartGameHttpResource::class)
class StartGameHttpResourceTest(@Autowired private val mvc: MockMvc) {

    @MockkBean
    private lateinit var gameRepository: Outputs.GameRepository

    @MockkBean
    private lateinit var writeLog: Outputs.WriteLogs

    @Test
    fun `should start a game`() {
        every { gameRepository.save(any()) } just runs
        every { writeLog(any()) } just runs

        val response = mvc.perform(post("/games"))

        response
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
    }
}
