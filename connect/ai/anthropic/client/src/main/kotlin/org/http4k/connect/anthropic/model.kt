package org.http4k.connect.anthropic

import dev.forkhandles.values.LocalDateValue
import dev.forkhandles.values.LocalDateValueFactory
import dev.forkhandles.values.NonBlankStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.Value
import org.http4k.connect.model.ModelName
import org.http4k.connect.model.StopReason
import org.http4k.connect.model.ToolName
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.Polymorphic
import se.ansman.kotshi.PolymorphicLabel
import java.time.LocalDate

class AnthropicIApiKey private constructor(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<AnthropicIApiKey>(::AnthropicIApiKey)
}

class UserId private constructor(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<UserId>(::UserId)
}

class ModelType private constructor(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<ModelType>(::ModelType)
}

class Prompt private constructor(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<Prompt>(::Prompt)
}

class ToolUseId private constructor(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<ToolUseId>(::ToolUseId)
}

@JsonSerializable
@Polymorphic("type")
sealed class ToolChoice {
    @JsonSerializable
    @PolymorphicLabel("auto")
    data class Auto(val disable_parallel_tool_use: Boolean = false) : ToolChoice()

    @JsonSerializable
    @PolymorphicLabel("any")
    data class Any(val disable_parallel_tool_use: Boolean = false) : ToolChoice()

    @JsonSerializable
    @PolymorphicLabel("tool")
    data class Tool(val name: ToolName, val disable_parallel_tool_use: Boolean = false) : ToolChoice()

    @JsonSerializable
    @PolymorphicLabel("none")
    data object None : ToolChoice()
}

enum class SourceType {
    base64
}

class ResponseId private constructor(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<ResponseId>(::ResponseId)
}

val StopReason.Companion.end_turn get() = StopReason.of("end_turn")
val StopReason.Companion.max_tokens get() = StopReason.of("max_tokens")
val StopReason.Companion.stop_sequence get() = StopReason.of("stop_sequence")
val StopReason.Companion.tool_use get() = StopReason.of("tool_use")

class ApiVersion private constructor(value: LocalDate) : LocalDateValue(value), Value<LocalDate> {
    companion object : LocalDateValueFactory<ApiVersion>(::ApiVersion) {
        val _2023_06_01 = ApiVersion.parse("2023-06-01")
    }
}

val ModelName.Companion.CLAUDE_3_7_SONNET get() = ModelName.of("claude-3-7-sonnet-20250219")

val ModelName.Companion.CLAUDE_3_5_SONNET get() = ModelName.of("claude-3-5-sonnet-20240620")
