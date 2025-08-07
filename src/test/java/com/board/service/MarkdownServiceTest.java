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
    @DisplayName("마크다운의 플레인 텍스트 미리보기를 생성할 수 있다")
    void getPlainTextPreview() {
        // given
        String markdown = "# 제목\n\n**굵은 글씨**와 *기울임*\n\n- 리스트 항목 1\n- 리스트 항목 2";
        int maxLength = 20;

        // when
        String preview = markdownService.getPlainTextPreview(markdown, maxLength);

        // then
        assertThat(preview).doesNotContain("<h1>");
        assertThat(preview).doesNotContain("**");
        assertThat(preview).doesNotContain("*");
        assertThat(preview).hasSizeLessThanOrEqualTo(maxLength + 3); // "..." 포함
        assertThat(preview).contains("제목");
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
}
