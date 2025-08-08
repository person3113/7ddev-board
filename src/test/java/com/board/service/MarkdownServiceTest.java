package com.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MarkdownServiceTest {

    @Autowired
    private MarkdownService markdownService;

    @Test
    @DisplayName("마크다운을 HTML로 변환할 수 있다")
    void markdownToHtml() {
        // given
        String markdown = "# 제목\n\n**굵은 글씨**와 *기울임*\n\n- 리스트 항목 1\n- 리스트 항목 2";

        // when
        String html = markdownService.markdownToHtml(markdown);

        // then
        assertThat(html).contains("<h1>제목</h1>");
        assertThat(html).contains("<strong>굵은 글씨</strong>");
        assertThat(html).contains("<em>기울임</em>");
        assertThat(html).contains("<ul>");
        assertThat(html).contains("<li>리스트 항목 1</li>");
    }

    @Test
    @DisplayName("빈 마크다운은 빈 문자열을 반환한다")
    void emptyMarkdown() {
        // when
        String html = markdownService.markdownToHtml("");

        // then
        assertThat(html).isEmpty();
    }

    @Test
    @DisplayName("null 마크다운은 빈 문자열을 반환한다")
    void nullMarkdown() {
        // when
        String html = markdownService.markdownToHtml(null);

        // then
        assertThat(html).isEmpty();
    }

    @Test
    @DisplayName("HTML 특수문자를 이스케이프할 수 있다")
    void escapeHtml() {
        // given
        String text = "<script>alert('XSS')</script>";

        // when
        String escaped = markdownService.escapeHtml(text);

        // then
        assertThat(escaped).doesNotContain("<script>");
        assertThat(escaped).contains("&lt;script&gt;");
    }

    @Test
    @DisplayName("마크다운에서 첫 번째 이미지 URL을 추출할 수 있다")
    void extractFirstImageUrl() {
        // given
        String markdown = "# 제목\n\n![이미지](https://example.com/image.jpg)\n\n![다른 이미지](https://example.com/image2.jpg)";

        // when
        String imageUrl = markdownService.extractFirstImageUrl(markdown);

        // then
        assertThat(imageUrl).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("이미지가 없는 마크다운에서는 null을 반환한다")
    void extractImageUrlFromMarkdownWithoutImage() {
        // given
        String markdown = "# 제목\n\n**굵은 글씨**만 있는 텍스트";

        // when
        String imageUrl = markdownService.extractFirstImageUrl(markdown);

        // then
        assertThat(imageUrl).isNull();
    }

    @Test
    @DisplayName("일반 텍스트의 줄바꿈을 HTML로 변환할 수 있다")
    void convertPlainTextToHtml_withLineBreaks() {
        // given
        String plainText = "# test\n\n1. dsdf\n2. sdfsdsf\n\ndsdd";

        // when
        String html = markdownService.convertPlainTextToHtml(plainText);

        // then
        assertThat(html).isEqualTo("# test<br><br>1. dsdf<br>2. sdfsdsf<br><br>dsdd");
        assertThat(html).contains("<br>");
        assertThat(html).doesNotContain("\n"); // 줄바꿈 문자가 모두 <br>로 변환되어야 함
    }

    @Test
    @DisplayName("일반 텍스트의 HTML 특수문자를 이스케이프한다")
    void convertPlainTextToHtml_withSpecialCharacters() {
        // given
        String plainText = "제목 <script>alert('xss')</script>\n내용 & 기타 \"따옴표\"";

        // when
        String html = markdownService.convertPlainTextToHtml(plainText);

        // then
        assertThat(html).contains("&lt;script&gt;");
        assertThat(html).contains("&amp;");
        assertThat(html).contains("&quot;");
        assertThat(html).contains("<br>");
        assertThat(html).doesNotContain("<script>"); // XSS 방지
    }

    @Test
    @DisplayName("빈 일반 텍스트는 빈 문자열을 반환한다")
    void convertPlainTextToHtml_empty() {
        // when
        String html = markdownService.convertPlainTextToHtml("");

        // then
        assertThat(html).isEmpty();
    }

    @Test
    @DisplayName("null 일반 텍스트는 빈 문자열을 반환한다")
    void convertPlainTextToHtml_null() {
        // when
        String html = markdownService.convertPlainTextToHtml(null);

        // then
        assertThat(html).isEmpty();
    }

    @Test
    @DisplayName("Windows 스타일 줄바꿈(\\r\\n)도 처리할 수 있다")
    void convertPlainTextToHtml_windowsLineBreaks() {
        // given
        String plainText = "첫 번째 줄\r\n두 번째 줄\r\n세 번째 줄";

        // when
        String html = markdownService.convertPlainTextToHtml(plainText);

        // then
        assertThat(html).isEqualTo("첫 번째 줄<br>두 번째 줄<br>세 번째 줄");
        assertThat(html).doesNotContain("\r\n");
        assertThat(html).doesNotContain("\n");
    }
}
