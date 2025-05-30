package org.vitrivr.engine.query.model.api.input

import io.javalin.openapi.Discriminator
import io.javalin.openapi.DiscriminatorProperty
import io.javalin.openapi.OneOf
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.util.extension.BufferedImage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * The abstract [InputData], essentially a query's input value.
 */
@Serializable(with = InputDataSerializer::class)
@OneOf(
    discriminator = Discriminator(DiscriminatorProperty("type", type = InputType::class)),
    value = [
        TextInputData::class,
        ImageInputData::class,
        VectorInputData::class,
        RetrievableIdInputData::class,
        BooleanInputData::class,
        NumericInputData::class,
        DateInputData::class,
        DateTimeInputData::class,
    ]
)
sealed class InputData() {
    /**
     * The [InputType] of this [InputType]. Required for polymorphic deserialisation.
     */
    abstract val type: InputType

    /**
     * Converts the given [InputData] to a [ContentElement] if supported.
     *
     * @throws UnsupportedOperationException If there is no way to convert the input to a content
     */
    abstract fun toContent() : ContentElement<*>

    /**
     * Optional comparison to apply.
     *
     * Currently supported comparisons use Kotlin notation:
     * - `<`: less than
     * - `<=`: less or equal than
     * - `==`: equal
     * - `!=`: not equal
     * - `>=`: greater or equal than
     * - `>` : greater than
     * - `~=`: LIKE
     */
    abstract val comparison: String?
}

/**
 * [InputData] for textual input.
 * Can be converted to a [ContentElement], specifically a [TextContent].
 */
@Serializable
data class TextInputData(val data: String, override val comparison: String? = "==") : InputData() {
    override val type = InputType.TEXT
    override fun toContent(): TextContent = InMemoryTextContent(data)
}

/**
 * [InputData] for vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class VectorInputData(val data: List<Float>, override val comparison: String? = "==") : InputData(){
    override val type = InputType.VECTOR
    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from VectorInputData")
    }
}

/**
 * [InputData] for image input in base64 format.
 * Can be converted to a [ContentElement], specifically to a [InMemoryImageContent].
 */
@Serializable
data class ImageInputData(val data: String, override val comparison: String? = "==") : InputData() {
    override val type = InputType.VECTOR
    override fun toContent(): ImageContent = InMemoryImageContent(BufferedImage(data))
}

/**
 * [InputData] for a retrievable id.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class RetrievableIdInputData(val id: String, override val comparison: String? = "==") : InputData() {

    override val type = InputType.ID

    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from RetrievableInputData")
    }

}

/**
 * [InputData] for boolean input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class BooleanInputData(val data: Boolean, override val comparison: String? = "=="): InputData(){
    override val type = InputType.BOOLEAN
    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from BooleanInputData")
    }
}

/**
 * [InputData] for numeric input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class NumericInputData(val data: Double, override val comparison: String? = "==") : InputData(){
    override val type = InputType.NUMERIC
    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from NumericInputData")
    }
}

/**
 * [InputData] for a date.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class DateInputData(val data: String, override val comparison: String? = "==") : InputData() {
    override val type = InputType.DATE
    override fun toContent(): ContentElement<*> {throw UnsupportedOperationException("Cannot derive content from DateInputData")}

    /**
     * Parses the input in YYYY-mm-dd format.
     */
    fun parseDate(): Date {
        val formatter = SimpleDateFormat("YYYY-mm-dd", Locale.ENGLISH)
        return formatter.parse(data)
    }
}

/**
 * [InputData] for a full date-time (ISO-8601).
 * Cannot be converted to a [ContentElement].
 */
@Serializable
data class DateTimeInputData(
    /** ISO-8601 string, e.g. "2025-05-20T19:25:49" **/
    val data: String,
    override val comparison: String? = "=="
) : InputData() {
    override val type = InputType.DATETIME

    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("DateTimeInputData cannot be turned into content")
    }

    /**
     * Parses the stored ISO-8601 date-time string into a [LocalDateTime].
     */
    fun parseDateTime(): LocalDateTime =
        LocalDateTime.parse(data, DateTimeFormatter.ISO_DATE_TIME)
}
