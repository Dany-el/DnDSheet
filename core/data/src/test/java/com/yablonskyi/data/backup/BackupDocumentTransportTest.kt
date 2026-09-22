package com.yablonskyi.data.backup

import com.yablonskyi.domain.repository.BackupError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class BackupDocumentTransportTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun givenSource_whenStaged_thenClosesBeforeValidationAndRemovesAfterUse() = runTest {
        var closed = false
        val source = object : ByteArrayInputStream(byteArrayOf(1, 2, 3)) {
            override fun close() { closed = true }
        }
        val transport = BackupDocumentTransport(StandardTestDispatcher(testScheduler))
        val result = transport.withStagedSource(temporary.root, { source }) { file ->
            assertTrue(closed)
            assertArrayEquals(byteArrayOf(1, 2, 3), file.readBytes())
            42
        }
        assertEquals(42, result)
        assertTrue(temporary.root.list()!!.isEmpty())
    }

    @Test fun givenOversizedSource_whenStaged_thenRejectsAndCleansUp() = runTest {
        val transport = BackupDocumentTransport(StandardTestDispatcher(testScheduler), 2)
        try {
            transport.withStagedSource(temporary.root, { ByteArrayInputStream(ByteArray(3)) }) {
                fail("Oversized source reached validation")
            }
            fail("Expected size failure")
        } catch (failure: BackupArchiveException) {
            assertEquals(BackupError.SIZE_LIMIT_EXCEEDED, failure.error)
        }
        assertTrue(temporary.root.list()!!.isEmpty())
    }

    @Test fun givenSourceCloseFailure_whenStaged_thenDoesNotValidateAndCleansUp() = runTest {
        val transport = BackupDocumentTransport(StandardTestDispatcher(testScheduler))
        val failure = IOException("close")
        val source = object : ByteArrayInputStream(byteArrayOf(1)) {
            override fun close() { throw failure }
        }
        try {
            transport.withStagedSource(temporary.root, { source }) { fail("Validation ran") }
            fail("Expected close failure")
        } catch (actual: IOException) { assertEquals(failure.message, actual.message) }
        assertTrue(temporary.root.list()!!.isEmpty())
    }

    @Test fun givenValidationCancellation_whenStaged_thenPropagatesAndCleansUp() = runTest {
        val transport = BackupDocumentTransport(StandardTestDispatcher(testScheduler))
        val failure = CancellationException("cancel")
        try {
            transport.withStagedSource(temporary.root, { ByteArrayInputStream(byteArrayOf(1)) }) {
                throw failure
            }
            fail("Expected cancellation")
        } catch (actual: CancellationException) { assertEquals(failure.message, actual.message) }
        assertTrue(temporary.root.list()!!.isEmpty())
    }

    @Test fun givenArchive_whenWritten_thenClosesOutputAndKeepsSuccessfulDocument() = runTest {
        val archive = temporary.newFile().apply { writeBytes(byteArrayOf(1, 2, 3)) }
        var closed = false
        var removed = false
        val output = object : ByteArrayOutputStream() {
            override fun close() { closed = true }
        }
        BackupDocumentTransport(StandardTestDispatcher(testScheduler)).writeArchive(
            archive, { output }, { removed = true },
        )
        assertArrayEquals(archive.readBytes(), output.toByteArray())
        assertTrue(closed)
        assertFalse(removed)
        assertTrue(archive.exists()) // Archive ownership remains with its caller.
    }

    @Test fun givenOutputCloseFailure_whenWritten_thenRemovesPartialAndPropagates() = runTest {
        val archive = temporary.newFile().apply { writeText("backup") }
        val failure = IOException("close")
        var removed = false
        val output = object : ByteArrayOutputStream() {
            override fun close() { throw failure }
        }
        try {
            BackupDocumentTransport(StandardTestDispatcher(testScheduler)).writeArchive(
                archive, { output }, { removed = true },
            )
            fail("Expected close failure")
        } catch (actual: IOException) { assertEquals(failure.message, actual.message) }
        assertTrue(removed)
    }

    @Test fun givenOpenAndCleanupFailure_whenWritten_thenPreservesOriginalFailure() = runTest {
        val archive = temporary.newFile().apply { writeText("backup") }
        val failure = SecurityException("permission")
        val cleanupFailure = IOException("delete")
        try {
            BackupDocumentTransport(StandardTestDispatcher(testScheduler)).writeArchive(
                archive, { throw failure }, { throw cleanupFailure },
            )
            fail("Expected permission failure")
        } catch (actual: SecurityException) {
            assertEquals(failure.message, actual.message)
            // Coroutine stack-trace recovery may copy the exception and retain it as the cause.
            assertTrue(generateSequence<Throwable>(actual) { it.cause }
                .any { cleanupFailure in it.suppressed })
        }
    }

    @Test fun givenMissingArchive_whenWritten_thenDoesNotOpenOrRemoveDestination() = runTest {
        var opened = false
        var removed = false
        try {
            BackupDocumentTransport(StandardTestDispatcher(testScheduler)).writeArchive(
                temporary.root.resolve("missing.zip"),
                { opened = true; ByteArrayOutputStream() },
                { removed = true },
            )
            fail("Expected missing archive failure")
        } catch (_: IOException) { }
        assertFalse(opened)
        assertFalse(removed)
    }

    @Test fun givenCancellationDuringRead_whenStaged_thenClosesAndRemovesBeforeValidation() = runTest {
        var closed = false
        lateinit var operation: Job
        val source = object : ByteArrayInputStream(ByteArray(20_000)) {
            override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
                operation.cancel()
                return super.read(bytes, offset, length)
            }
            override fun close() { closed = true }
        }
        operation = launch(start = CoroutineStart.LAZY) {
            BackupDocumentTransport(StandardTestDispatcher(testScheduler))
                .withStagedSource(temporary.root, { source }) { fail("Validation ran after cancellation") }
        }
        operation.start()
        operation.join()
        assertTrue(operation.isCancelled)
        assertTrue(closed)
        assertTrue(temporary.root.list()!!.isEmpty())
    }

    @Test fun givenCancellationDuringWrite_whenExported_thenClosesAndRemovesPartial() = runTest {
        val archive = temporary.newFile().apply { writeBytes(ByteArray(20_000)) }
        var closed = false
        var removed = false
        lateinit var operation: Job
        val output = object : ByteArrayOutputStream() {
            override fun write(bytes: ByteArray, offset: Int, length: Int) {
                operation.cancel()
                super.write(bytes, offset, length)
            }
            override fun close() { closed = true }
        }
        operation = launch(start = CoroutineStart.LAZY) {
            BackupDocumentTransport(StandardTestDispatcher(testScheduler))
                .writeArchive(archive, { output }, { removed = true })
        }
        operation.start()
        operation.join()
        assertTrue(operation.isCancelled)
        assertTrue(closed)
        assertTrue(removed)
    }

    @Test fun givenZeroLengthRead_whenStaged_thenMakesProgressAndAcceptsExactLimit() = runTest {
        val source = object : ByteArrayInputStream(byteArrayOf(1, 2, 3)) {
            override fun read(bytes: ByteArray, offset: Int, length: Int): Int = 0
        }
        BackupDocumentTransport(StandardTestDispatcher(testScheduler), 3)
            .withStagedSource(temporary.root, { source }) {
                assertArrayEquals(byteArrayOf(1, 2, 3), it.readBytes())
            }
        assertTrue(temporary.root.list()!!.isEmpty())
    }

    @Test fun givenReadFailure_whenStaged_thenClosesAndCleansUp() = runTest {
        var closed = false
        val source = object : ByteArrayInputStream(byteArrayOf(1)) {
            override fun read(bytes: ByteArray, offset: Int, length: Int): Int = throw IOException("read")
            override fun close() { closed = true }
        }
        try {
            BackupDocumentTransport(StandardTestDispatcher(testScheduler))
                .withStagedSource(temporary.root, { source }) { fail("Validation ran") }
            fail("Expected read failure")
        } catch (actual: IOException) { assertEquals("read", actual.message) }
        assertTrue(closed)
        assertTrue(temporary.root.list()!!.isEmpty())
    }

    @Test fun givenWriteFailure_whenExported_thenClosesAndRemovesPartial() = runTest {
        val archive = temporary.newFile().apply { writeText("backup") }
        var closed = false
        var removed = false
        val output = object : ByteArrayOutputStream() {
            override fun write(bytes: ByteArray, offset: Int, length: Int) { throw IOException("write") }
            override fun close() { closed = true }
        }
        try {
            BackupDocumentTransport(StandardTestDispatcher(testScheduler))
                .writeArchive(archive, { output }, { removed = true })
            fail("Expected write failure")
        } catch (actual: IOException) { assertEquals("write", actual.message) }
        assertTrue(closed)
        assertTrue(removed)
    }
}
