package com.reverse.payroll.internal.infrastructure;

import com.lowagie.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfGenerator {

    private final SpringTemplateEngine templateEngine;

    public byte[] generatePdfFromHtml(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        String htmlContent = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // 한글 폰트 설정
            try {
                ITextFontResolver fontResolver = renderer.getFontResolver();
                ClassPathResource fontResource = new ClassPathResource("fonts/nanum.ttf");
                if (fontResource.exists()) {
                    String fontPath = fontResource.getURL().toString();
                    fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    log.info("PDF Font registered: NanumGothic from {}", fontPath);
                } else {
                    throw new IllegalStateException(
                            "PDF 한글 폰트 파일을 찾을 수 없습니다. (classpath:fonts/nanum.ttf)");
                }
            } catch (Exception e) {
                if (e instanceof IllegalStateException) throw (IllegalStateException) e;
                throw new IllegalStateException("PDF 한글 폰트 등록에 실패했습니다.", e);
            }

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error during PDF generation", e);
            throw new RuntimeException("급여 명세서 PDF 생성 중 오류가 발생했습니다.", e);
        }
    }
}
