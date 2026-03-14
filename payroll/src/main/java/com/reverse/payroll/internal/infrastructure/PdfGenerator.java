package com.reverse.payroll.internal.infrastructure;

import com.lowagie.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
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
    private static final List<String> KOREAN_FONT_CANDIDATES =
            List.of("fonts/NotoSansKR-Regular.ttf", "fonts/nanum.ttf");

    public byte[] generatePdfFromHtml(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        String htmlContent = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // 한글 폰트 설정
            try {
                registerKoreanFont(renderer.getFontResolver());
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

    private void registerKoreanFont(ITextFontResolver fontResolver) {
        IllegalStateException lastFailure = null;

        for (String fontCandidate : KOREAN_FONT_CANDIDATES) {
            try {
                ClassPathResource fontResource = new ClassPathResource(fontCandidate);
                if (!fontResource.exists()) {
                    continue;
                }

                String fontPath = fontResource.getURL().toString();
                fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                log.info("PDF Font registered from {}", fontPath);
                return;
            } catch (Exception e) {
                log.warn("Failed to register PDF font candidate: {}", fontCandidate, e);
                lastFailure =
                        new IllegalStateException(
                                "PDF 한글 폰트 등록에 실패했습니다. (candidate=" + fontCandidate + ")", e);
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
        }

        throw new IllegalStateException(
                "PDF 한글 폰트 파일을 찾을 수 없습니다. (candidates=" + KOREAN_FONT_CANDIDATES + ")");
    }
}
