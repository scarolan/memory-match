package xyz.oddforge.memorymatch

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(context: Context) {

    private val sampleRate = 44100
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<SoundCue, Int>()

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        val cacheDir = context.cacheDir
        for (cue in SoundCue.entries) {
            val pcm = generatePcm(cue)
            val file = File(cacheDir, "sound_${cue.name.lowercase()}.wav")
            writeWav(file, pcm)
            soundIds[cue] = soundPool.load(file.absolutePath, 1)
        }
    }

    fun play(cue: SoundCue) {
        val id = soundIds[cue] ?: return
        val volume = if (cue == SoundCue.MISMATCH) 0.5f else 1.0f
        soundPool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }

    private data class ToneSpec(val freq: Double, val durationMs: Int, val decayRate: Double = 4.0)

    private fun generatePcm(cue: SoundCue): ShortArray = when (cue) {
        SoundCue.FLIP -> tone(880.0, 80, decayRate = 12.0)
        SoundCue.MATCH -> arpeggio(
            listOf(
                ToneSpec(523.25, 120, 4.0),
                ToneSpec(659.25, 120, 4.0),
                ToneSpec(783.99, 180, 4.0)
            )
        )
        SoundCue.MISMATCH -> tone(280.0, 180, decayRate = 6.0)
        SoundCue.GAME_COMPLETE -> arpeggio(
            listOf(
                ToneSpec(523.25, 100, 4.0),
                ToneSpec(659.25, 100, 4.0),
                ToneSpec(783.99, 100, 4.0),
                ToneSpec(1046.50, 300, 3.0)
            )
        )
    }

    private fun tone(freq: Double, durationMs: Int, decayRate: Double = 4.0): ShortArray {
        val numSamples = (sampleRate * durationMs / 1000.0).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val attack = (t / 0.005).coerceAtMost(1.0)
            val decay = exp(-decayRate * t)
            val envelope = attack * decay
            val wave = sin(2 * PI * freq * t) * 0.6 + sin(4 * PI * freq * t) * 0.2
            val sample = (wave * envelope * Short.MAX_VALUE * 0.7).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
            samples[i] = sample
        }
        return samples
    }

    private fun arpeggio(notes: List<ToneSpec>): ShortArray {
        val arrays = notes.map { tone(it.freq, it.durationMs, it.decayRate) }
        val totalLength = arrays.sumOf { it.size }
        val result = ShortArray(totalLength)
        var offset = 0
        for (arr in arrays) {
            System.arraycopy(arr, 0, result, offset, arr.size)
            offset += arr.size
        }
        return result
    }

    private fun writeWav(file: File, pcm: ShortArray) {
        val dataSize = pcm.size * 2
        val totalSize = 44 + dataSize
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))

        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(1)
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2)
        buffer.putShort(2)
        buffer.putShort(16)

        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)

        for (sample in pcm) {
            buffer.putShort(sample)
        }

        file.writeBytes(buffer.array())
    }
}
