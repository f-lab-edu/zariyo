-- 카테고리 데이터 (기존 3개 유지)
INSERT INTO categories (category_name) VALUES
('콘서트'),
('뮤지컬'),
('연극');

-- 공연장 데이터 (3개 공연장만 필요)
INSERT INTO concert_halls (hall_name, address) VALUES
('올림픽공원 KSPO DOME', '서울특별시 송파구 올림픽로 424'),
('블루스퀘어 마스터카드홀', '서울특별시 용산구 이태원로 294'),
('코엑스 오디토리움', '서울특별시 강남구 영동대로 513');

-- 홀 좌석 데이터 대량 생성 (각 공연장마다 1000개씩 총 3,000개 좌석)
INSERT INTO hall_seats (hall_id, seat_grade, block, seat_row, seat_number) 
SELECT 
    hall_id,
    CASE 
        WHEN seat_num <= 200 THEN 'VIP'
        WHEN seat_num <= 600 THEN 'R석'
        ELSE 'S석'
    END as seat_grade,
    CONCAT(
        CASE 
            WHEN seat_num <= 250 THEN 'A'
            WHEN seat_num <= 500 THEN 'B'
            WHEN seat_num <= 750 THEN 'C'
            ELSE 'D'
        END, '블록'
    ) as block,
    CONCAT(CEILING(((seat_num - 1) % 50 + 1) / 10), '열') as seat_row,
    CONCAT(((seat_num - 1) % 10) + 1, '번') as seat_number
FROM (
    SELECT hall_id, @row_number := @row_number + 1 AS seat_num
    FROM (
        SELECT 1 as hall_id UNION ALL SELECT 2 UNION ALL SELECT 3
    ) halls
    CROSS JOIN (
        SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) t1
    CROSS JOIN (
        SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) t2
    CROSS JOIN (
        SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) t3
    CROSS JOIN (SELECT @row_number := 0) r
    WHERE @row_number < 1000
) seats;

-- 공연 데이터 (테스트에서 기대하는 3개 공연만)
INSERT INTO concerts (title, category_id, hall_id, age_limit, description, running_time, reservation_count, start_date, end_date, status, created_at) VALUES
('아이유 콘서트 [The Golden Hour]', 1, 1, '전체관람가', '아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트. 새로운 앨범의 감동을 라이브로 만나보세요', 150, 8500, DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 40 DAY), 'ACTIVE', '2024-01-03 10:00:00'),
('BTS 월드투어 [Yet To Come]', 1, 2, '전체관람가', '방탄소년단의 글로벌 월드투어 서울 공연', 180, 12000, DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 35 DAY), 'ACTIVE', '2024-01-02 10:00:00'),
('BLACKPINK 월드투어 [Born Pink]', 1, 3, '전체관람가', '블랙핑크의 강렬한 퍼포먼스와 히트곡들을 한자리에서', 160, 15000, DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 50 DAY), 'ACTIVE', '2024-01-01 10:00:00');

-- 좌석 가격 데이터 (3개 공연에 대해)
INSERT INTO seat_prices (concert_id, seat_grade, price) VALUES
-- 아이유 콘서트
(1, 'VIP', 220000.00), (1, 'R석', 170000.00), (1, 'S석', 130000.00),
-- BTS 콘서트
(2, 'VIP', 330000.00), (2, 'R석', 250000.00), (2, 'S석', 180000.00),
-- BLACKPINK 콘서트
(3, 'VIP', 290000.00), (3, 'R석', 220000.00), (3, 'S석', 160000.00);

-- 스케줄 데이터 (각 공연마다 20개 회차씩 총 60개 스케줄)
INSERT INTO schedules (concert_id, schedule_datetime) 
SELECT 
    concert_id,
    DATE_ADD(start_date, INTERVAL schedule_offset DAY) + 
    INTERVAL (CASE WHEN schedule_offset % 2 = 0 THEN 14 ELSE 19 END) HOUR + 
    INTERVAL 30 MINUTE
FROM concerts c
CROSS JOIN (
    SELECT 0 as schedule_offset UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
    UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
) s
WHERE c.concert_id <= 3;

-- 스케줄 좌석 데이터 (첫 20개 스케줄에 대해 대량 생성 - 각각 1000개씩 총 20,000개)
INSERT INTO schedule_seats (schedule_id, hall_seat_id, seat_grade, price, status)
SELECT 
    s.schedule_id,
    hs.hall_seat_id,
    hs.seat_grade,
    sp.price,
    CASE 
        WHEN (hs.hall_seat_id % 10) < 7 THEN 'AVAILABLE'
        WHEN (hs.hall_seat_id % 10) < 9 THEN 'PENDING'
        ELSE 'RESERVED'
    END as status
FROM schedules s
JOIN concerts c ON s.concert_id = c.concert_id
JOIN hall_seats hs ON hs.hall_id = c.hall_id
JOIN seat_prices sp ON sp.concert_id = c.concert_id AND sp.seat_grade = hs.seat_grade
WHERE s.schedule_id <= 20;

-- 공연 파일 데이터 (3개 공연에 대해 포스터와 설명 이미지)
INSERT INTO concert_files (concert_id, original_file_name, changed_file_name, file_url, file_type) VALUES
-- 아이유 콘서트
(1, 'iu_golden_hour_poster.jpg', 'uuid_iu_golden_hour_poster.jpg', 'https://cdn.zariyo.com/posters/iu_golden_hour_poster.jpg', 'POSTER'),
(1, 'iu_golden_hour_description.jpg', 'uuid_iu_golden_hour_description.jpg', 'https://cdn.zariyo.com/descriptions/iu_golden_hour_description.jpg', 'DESCRIPTION'),
-- BTS 콘서트
(2, 'bts_yet_to_come_poster.jpg', 'uuid_bts_yet_to_come_poster.jpg', 'https://cdn.zariyo.com/posters/bts_yet_to_come_poster.jpg', 'POSTER'),
(2, 'bts_yet_to_come_description.jpg', 'uuid_bts_yet_to_come_description.jpg', 'https://cdn.zariyo.com/descriptions/bts_yet_to_come_description.jpg', 'DESCRIPTION'),
-- BLACKPINK 콘서트
(3, 'blackpink_born_pink_poster.jpg', 'uuid_blackpink_born_pink_poster.jpg', 'https://cdn.zariyo.com/posters/blackpink_born_pink_poster.jpg', 'POSTER'),
(3, 'blackpink_born_pink_description.jpg', 'uuid_blackpink_born_pink_description.jpg', 'https://cdn.zariyo.com/descriptions/blackpink_born_pink_description.jpg', 'DESCRIPTION');
