package com.reverse.payroll.internal.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

            // 한글 폰트 설정 (폰트 파일이 실존해야 함)
            // 실제 운영시에는 /resources/fonts/NanumGothic.ttf 와 같이 폰트 파일을 포함시켜야 합니다.
            try {
                ITextFontResolver fontResolver = renderer.getFontResolver();
                String fontPath = "fonts/NanumGothic.ttf"; // resources 기준 경로 예시
                // fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                log.info(
                        "PDF Font Resolver initialized. Note: Korean support requires .ttf font registration.");
            } catch (Exception e) {
                log.warn("Failed to register custom font for PDF: {}", e.getMessage());
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
