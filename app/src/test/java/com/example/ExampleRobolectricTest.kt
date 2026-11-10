package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NOVA OFFICE", appName)
  }

  @Test
  fun `verify document types have correct extensions`() {
    val docxType = com.example.data.model.DocumentType.fromExtension("docx")
    assertEquals(com.example.data.model.DocumentType.WRITER, docxType)
    val xlsxType = com.example.data.model.DocumentType.fromExtension("xlsx")
    assertEquals(com.example.data.model.DocumentType.SHEETS, xlsxType)
    val pptxType = com.example.data.model.DocumentType.fromExtension("pptx")
    assertEquals(com.example.data.model.DocumentType.SLIDES, pptxType)
    val pdfType = com.example.data.model.DocumentType.fromExtension("pdf")
    assertEquals(com.example.data.model.DocumentType.PDF, pdfType)
  }
}
