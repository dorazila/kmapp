package it.kimia.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {
    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    public byte[] renderHtml(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String normalizedXhtml = toXhtml(html);
            
            logger.debug("Rendering PDF with HTML length: {} bytes", normalizedXhtml.length());
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(normalizedXhtml, null);
            builder.toStream(out);
            builder.run();
            
            logger.info("PDF rendered successfully: {} bytes", out.size());
            return out.toByteArray();
        } catch (Exception ex) {
            logger.error("Errore durante il rendering del PDF", ex);
            throw new IllegalStateException("Impossibile generare il PDF: " + ex.getMessage(), ex);
        }
    }

    private String toXhtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings()
            .syntax(Document.OutputSettings.Syntax.xml)
            .escapeMode(Entities.EscapeMode.xhtml)
            .prettyPrint(true)
            .charset("UTF-8");
        return doc.html();
    }
}
