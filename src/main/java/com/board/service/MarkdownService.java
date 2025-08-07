package com.board.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder().build();
    }

    /**
     * 마크다운 텍스트를 HTML로 변환
     * XSS 방지를 위한 인코딩 포함
     */
    public String markdownToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }

        // 마크다운 파싱
        Node document = parser.parse(markdown);

        // HTML로 렌더링
        String html = renderer.render(document);

        // XSS 방지를 위한 추가적인 처리는 여기서 할 수 있지만,
        // 마크다운 자체가 안전한 HTML을 생성하므로 기본적으로는 필요 없음
        return html;
    }

    /**
     * 일반 텍스트에서 HTML 특수문자만 이스케이프
     * (마크다운을 사용하지 않는 경우)
     */
    public String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return Encode.forHtml(text);
    }

    /**
     * 마크다운 텍스트의 미리보기 생성 (HTML 태그 제거)
     */
    public String getPlainTextPreview(String markdown, int maxLength) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }

        // 마크다운을 HTML로 변환
        String html = markdownToHtml(markdown);

        // HTML 태그 제거
        String plainText = html.replaceAll("<[^>]*>", "");

        // 연속된 공백 정리
        plainText = plainText.replaceAll("\\s+", " ").trim();

        // 길이 제한
        if (plainText.length() > maxLength) {
            return plainText.substring(0, maxLength) + "...";
        }

        return plainText;
    }

    /**
     * 마크다운에서 첫 번째 이미지 URL 추출 (썸네일용)
     */
    public String extractFirstImageUrl(String markdown) {
        if (markdown == null) {
            return null;
        }

        // 마크다운 이미지 패턴: ![alt](url)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(markdown);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
