package com.hifnawy.compose.notify.notifyer.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

/**
 * A data class representing a notification.
 *
 * @property id [UUID] The unique identifier of the notification.
 * @property title [String] The title of the notification.
 * @property message [String] The message of the notification.
 */
@Serializable
data class Notification(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID = UUID.randomUUID(),
        val title: String = "",
        val message: String = ""
)

/**
 * A custom serializer for [UUID] objects.
 *
 * This serializer is used to serialize and deserialize [UUID] objects to and from JSON.
 *
 * @author Hifnawy
 */
private object UUIDSerializer : KSerializer<UUID> {

    /**
     * The descriptor for the [UUID] type.
     */
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    /**
     * Deserializes a [UUID] from the given [Decoder].
     *
     * @param decoder [Decoder] The [Decoder] to deserialize from.
     * @return [UUID] The deserialized [UUID].
     */
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    /**
     * Serializes the given [UUID] to the given [Encoder].
     *
     * @param encoder [Encoder] The [Encoder] to serialize to.
     * @param value [UUID] The [UUID] to serialize.
     */
    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}
