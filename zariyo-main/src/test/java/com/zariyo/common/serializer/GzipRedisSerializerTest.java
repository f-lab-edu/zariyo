package com.zariyo.common.serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zariyo.concert.application.dto.ConcertDetailDTO;
import com.zariyo.concert.application.dto.ConcertFileInfo;
import com.zariyo.concert.application.dto.ScheduleInfo;
import com.zariyo.concert.application.dto.SeatPriceInfo;
import com.zariyo.concert.application.serializer.GzipRedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GzipRedisSerializer 압축 테스트")
class GzipRedisSerializerTest {

    private static final Logger log = Logger.getLogger(GzipRedisSerializerTest.class.getName());

    private GzipRedisSerializer<ConcertDetailDTO> serializer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        serializer = new GzipRedisSerializer<>(objectMapper, new TypeReference<ConcertDetailDTO>() {});
    }

    @Test
    @DisplayName("작은 객체(2KB 미만)는 압축 X")
    void shouldNotCompressSmallObject() {
        // Given: 작은 객체 생성
        ConcertDetailDTO smallDto = ConcertDetailDTO.builder()
                .concertId(1L)
                .title("작은 콘서트")
                .categoryName("클래식")
                .hallName("작은홀")
                .ageLimit("전체관람가")
                .description("짧은 설명")
                .runningTime(90)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .status("ACTIVE")
                .concertFiles(List.of())
                .schedules(List.of())
                .seatPrices(List.of())
                .build();

        // When: 직렬화
        byte[] serialized = serializer.serialize(smallDto);

        // Then: 압축되지 않음 (GZIP 매직 바이트가 없음)
        assertThat(serialized).isNotNull();
        assertThat(isGzipCompressed(serialized)).isFalse();
        
        // 역직렬화 확인
        ConcertDetailDTO deserialized = serializer.deserialize(serialized);
        assertThat(deserialized.getTitle()).isEqualTo("작은 콘서트");

        log.info("작은 객체 크기: " + serialized.length + " bytes");
    }

    @Test
    @DisplayName("큰 객체(2KB 이상)는 압축한다")
    void shouldCompressLargeObject() throws Exception {
        // Given: 큰 객체 생성 (2KB 이상이 되도록)
        List<ConcertFileInfo> largeFileList = Arrays.asList(
                ConcertFileInfo.builder().fileUrl("https://example.com/long/path/to/concert/files/poster1.jpg").fileType("IMAGE").originalFileName("original_poster1.jpg_with_very_long_name_for_testing_purposes.jpg").build(),
                ConcertFileInfo.builder().fileUrl("https://example.com/long/path/to/concert/files/poster2.jpg").fileType("IMAGE").originalFileName("original_poster2.jpg_with_very_long_name_for_testing_purposes.jpg").build(),
                ConcertFileInfo.builder().fileUrl("https://example.com/long/path/to/concert/files/detail1.jpg").fileType("IMAGE").originalFileName("original_detail1.jpg_with_very_long_name_for_testing_purposes.jpg").build(),
                ConcertFileInfo.builder().fileUrl("https://example.com/long/path/to/concert/files/detail2.jpg").fileType("IMAGE").originalFileName("original_detail2.jpg_with_very_long_name_for_testing_purposes.jpg").build(),
                ConcertFileInfo.builder().fileUrl("https://example.com/long/path/to/concert/files/detail3.jpg").fileType("IMAGE").originalFileName("original_detail3.jpg_with_very_long_name_for_testing_purposes.jpg").build()
        );

        List<ScheduleInfo> largeScheduleList = Arrays.asList(
                ScheduleInfo.builder().scheduleId(1L).scheduleDateTime(LocalDateTime.now()).build(),
                ScheduleInfo.builder().scheduleId(2L).scheduleDateTime(LocalDateTime.now().plusDays(1)).build(),
                ScheduleInfo.builder().scheduleId(3L).scheduleDateTime(LocalDateTime.now().plusDays(2)).build(),
                ScheduleInfo.builder().scheduleId(4L).scheduleDateTime(LocalDateTime.now().plusDays(3)).build(),
                ScheduleInfo.builder().scheduleId(5L).scheduleDateTime(LocalDateTime.now().plusDays(4)).build()
        );

        List<SeatPriceInfo> largePriceList = Arrays.asList(
                SeatPriceInfo.builder().seatGrade("VIP").price(new BigDecimal("150000")).build(),
                SeatPriceInfo.builder().seatGrade("R석").price(new BigDecimal("120000")).build(),
                SeatPriceInfo.builder().seatGrade("S석").price(new BigDecimal("100000")).build(),
                SeatPriceInfo.builder().seatGrade("A석").price(new BigDecimal("80000")).build(),
                SeatPriceInfo.builder().seatGrade("B석").price(new BigDecimal("60000")).build()
        );

        ConcertDetailDTO largeDto = ConcertDetailDTO.builder()
                .concertId(1L)
                .title("매우 긴 제목을 가진 대형 콘서트 - 이것은 테스트를 위한 매우 긴 제목입니다. 더 많은 텍스트를 추가하여 객체 크기를 늘려보겠습니다.")
                .categoryName("클래식")
                .hallName("대형 콘서트홀")
                .ageLimit("전체관람가")
                .description("매우 긴 설명입니다. ".repeat(100)) // 긴 설명으로 크기 증가
                .runningTime(180)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("ACTIVE")
                .concertFiles(largeFileList)
                .schedules(largeScheduleList)
                .seatPrices(largePriceList)
                .build();

        // When: 직렬화
        byte[] serialized = serializer.serialize(largeDto);

        // Then: 압축됨 (GZIP 매직 바이트 확인)
        assertThat(serialized).isNotNull();
        assertThat(isGzipCompressed(serialized)).isTrue();
        
        // 역직렬화 확인
        ConcertDetailDTO deserialized = serializer.deserialize(serialized);
        assertThat(deserialized.getTitle()).contains("매우 긴 제목을 가진 대형 콘서트");
        assertThat(deserialized.getConcertFiles()).hasSize(5);
        assertThat(deserialized.getSchedules()).hasSize(5);
        assertThat(deserialized.getSeatPrices()).hasSize(5);
        
        // 압축 효과 확인
        byte[] uncompressed = objectMapper.writeValueAsBytes(largeDto);
        double compressionRatio = (double) serialized.length / uncompressed.length;

        log.info("원본 크기: " + uncompressed.length + " bytes");
        log.info("압축 크기: " + serialized.length + " bytes");
        log.info("압축률: " + String.format("%.2f%%", compressionRatio * 100));
        log.info("압축 효과: " + String.format("%.2f%%", (1 - compressionRatio) * 100) + " 절약");

        assertThat(compressionRatio).isLessThan(0.9);
    }

    @Test
    @DisplayName("압축된 데이터와 비압축 데이터 모두 정상 역직렬화")
    void shouldDeserializeBothCompressedAndUncompressed() throws Exception {
        // Given: 작은 객체와 큰 객체
        ConcertDetailDTO smallDto = ConcertDetailDTO.builder()
                .concertId(1L)
                .title("작은 콘서트")
                .build();

        ConcertDetailDTO largeDto = ConcertDetailDTO.builder()
                .concertId(2L)
                .title("큰 콘서트")
                .description("매우 긴 설명입니다. ".repeat(200))
                .build();

        // When: 직렬화
        byte[] smallSerialized = serializer.serialize(smallDto);
        byte[] largeSerialized = serializer.serialize(largeDto);

        // Then: 모두 정상 역직렬화
        ConcertDetailDTO smallDeserialized = serializer.deserialize(smallSerialized);
        ConcertDetailDTO largeDeserialized = serializer.deserialize(largeSerialized);

        assertThat(smallDeserialized.getTitle()).isEqualTo("작은 콘서트");
        assertThat(largeDeserialized.getTitle()).isEqualTo("큰 콘서트");
        
        // 압축 상태 확인
        assertThat(isGzipCompressed(smallSerialized)).isFalse();
        assertThat(isGzipCompressed(largeSerialized)).isTrue();
    }

    private boolean isGzipCompressed(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }
        return bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC & 0xFF) && 
               bytes[1] == (byte) ((GZIPInputStream.GZIP_MAGIC >> 8) & 0xFF);
    }
}
