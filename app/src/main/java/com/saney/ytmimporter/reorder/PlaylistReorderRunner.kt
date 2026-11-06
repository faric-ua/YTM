package com.saney.ytmimporter.reorder

import android.os.Handler
import android.os.Looper
import com.saney.ytmimporter.library.PlaylistReorderPolicy
import com.saney.ytmimporter.util.ErrorMessages
import com.saney.ytmimporter.youtube.YouTubeApi
import com.saney.ytmimporter.youtube.YouTubeApiException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

/**
 * Batch 20: «Вирівняти порядок». Process-local owner of one read + plan +
 * move run, so a rotation or a recreated Library screen finds it where it
 * was (same pattern as DestinationRemoteOperations). Nothing is moved until
 * the owner confirms the plan.
 */
object PlaylistReorderRunner {
    enum class Phase { IDLE, READING, PLANNED, MOVING, DONE, ERROR }

    data class State(
        val phase: Phase = Phase.IDLE,
        val playlistId: String = "",
        val playlistTitle: String = "",
        val referenceIdentity: String = "",
        val referenceTitle: String = "",
        val plan: PlaylistReorderPolicy.Plan? = null,
        val moved: Int = 0,
        val message: String = "",
        val serial: Long = 0L
    ) {
        val busy: Boolean get() = phase == Phase.READING || phase == Phase.MOVING
    }

    @Volatile
    var state: State = State()
        private set

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<(State) -> Unit>()

    fun addListener(listener: (State) -> Unit) {
        listeners += listener
    }

    fun removeListener(listener: (State) -> Unit) {
        listeners -= listener
    }

    @Synchronized
    private fun publish(next: State) {
        val stamped = next.copy(serial = state.serial + 1)
        state = stamped
        mainHandler.post { listeners.forEach { it(stamped) } }
    }

    /** Reads the YouTube order and makes the plan. Nothing is changed. */
    @Synchronized
    fun read(
        api: YouTubeApi,
        token: String,
        playlistId: String,
        playlistTitle: String,
        referenceIdentity: String,
        referenceTitle: String,
        reference: List<String?>,
        onListRequest: () -> Unit
    ): Boolean {
        if (state.busy) return false
        publish(State(Phase.READING, playlistId, playlistTitle, referenceIdentity, referenceTitle,
            message = "Читаю порядок пісень на YouTube…"))
        executor.execute {
            runCatching { api.listPlaylistOrder(token, playlistId, onListRequest) }
                .onSuccess { items ->
                    publish(state.copy(phase = Phase.PLANNED, message = "",
                        plan = PlaylistReorderPolicy.plan(reference, items)))
                }
                .onFailure { error ->
                    publish(state.copy(phase = Phase.ERROR,
                        message = ErrorMessages.userMessage(error, "Не вдалося прочитати плейліст")))
                }
        }
        return true
    }

    /** Moves the songs of the confirmed plan, one by one. */
    @Synchronized
    fun move(api: YouTubeApi, token: String, onMove: () -> Unit): Boolean {
        val plan = state.plan ?: return false
        if (state.phase != Phase.PLANNED || plan.inOrder) return false
        val playlistId = state.playlistId
        publish(state.copy(phase = Phase.MOVING, moved = 0, message = ""))
        executor.execute {
            var moved = 0
            try {
                plan.moves.forEach { move ->
                    onMove()
                    api.movePlaylistItem(token, playlistId, move.item, move.position)
                    moved += 1
                    publish(state.copy(moved = moved))
                }
                publish(state.copy(phase = Phase.DONE, moved = moved,
                    message = "Порядок вирівняно: переміщено ${countWord(moved)} ✓"))
            } catch (error: Exception) {
                publish(state.copy(phase = Phase.ERROR, moved = moved,
                    message = failure(error, moved, plan.moves.size)))
            }
        }
        return true
    }

    /** Forget a finished / cancelled run (never one that is running). */
    @Synchronized
    fun clear() {
        if (!state.busy) publish(State())
    }

    internal fun failure(error: Exception, moved: Int, total: Int): String {
        val api = error as? YouTubeApiException
        val done = "Переміщено $moved з $total."
        return when {
            api?.reason.orEmpty() in PlaylistReorderPolicy.positionRefusedReasons ->
                "YouTube не дає переставляти пісні в цьому плейлісті: у YouTube Music " +
                    "він сортується не «вручну». Відкрийте плейліст у YouTube Music → " +
                    "сортування → «Вручну» і спробуйте ще раз. $done"
            api?.isQuotaError == true ->
                "Квота YouTube на сьогодні закінчилась. $done Решту — завтра: " +
                    "натисніть «Вирівняти порядок» ще раз, план складеться заново."
            else -> ErrorMessages.userMessage(error, "Не вдалося перемістити пісню") + " $done"
        }
    }

    fun countWord(n: Int): String {
        val mod10 = n % 10
        val mod100 = n % 100
        val word = when {
            mod10 == 1 && mod100 != 11 -> "пісню"
            mod10 in 2..4 && mod100 !in 12..14 -> "пісні"
            else -> "пісень"
        }
        return "$n $word"
    }
}
